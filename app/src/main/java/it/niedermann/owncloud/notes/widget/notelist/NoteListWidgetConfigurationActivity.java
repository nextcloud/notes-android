package it.niedermann.owncloud.notes.widget.notelist;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_STARRED;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;


public class NoteListWidgetConfigurationActivity extends LockedActivity {
    private static final String TAG = Activity.class.getSimpleName();

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Account localAccount = null;

    private NavigationAdapter adapterCategories;
    private NotesDatabase db = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_note_list_configuration);

        db = NotesDatabase.getInstance(this);
        try {
            this.localAccount = db.getAccountDao().getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(this).name);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show();
            // TODO Present user with app login screen
            Log.w(TAG, "onCreate: user not logged in");
            finish();
            return;
        }
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "INVALID_APPWIDGET_ID");
            finish();
        }

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        adapterCategories = new NavigationAdapter(this, new NavigationClickListener() {
            @Override
            public void onItemClick(NavigationItem item) {
                NotesListWidgetData data = new NotesListWidgetData();

                data.setId(appWidgetId);
                if (item.type != null) {
                    switch (item.type) {
                        case RECENT: {
                            data.setMode(MODE_DISPLAY_ALL);
                            break;
                        }
                        case FAVORITES: {
                            data.setMode(MODE_DISPLAY_STARRED);
                            break;
                        }
                        case UNCATEGORIZED:
                        default: {
                            if (item.getClass() == NavigationItem.CategoryNavigationItem.class) {
                                data.setMode(MODE_DISPLAY_CATEGORY);
                                data.setCategoryId(((NavigationItem.CategoryNavigationItem) item).categoryId);
                            } else {
                                data.setMode(MODE_DISPLAY_ALL);
                                Log.e(TAG, "Unknown item navigation type. Fallback to show " + RECENT);
                            }
                        }
                    }
                } else {
                    data.setMode(MODE_DISPLAY_ALL);
                    Log.e(TAG, "Unknown item navigation type. Fallback to show " + RECENT);
                }

                data.setAccountId(localAccount.getId());
                data.setThemeMode(NotesApplication.getAppTheme(getApplicationContext()).getModeId());

                db.getWidgetNotesListDao().createOrUpdateNoteListWidgetData(data);

                Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                        getApplicationContext(), NoteListWidget.class);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, updateIntent);
                getApplicationContext().sendBroadcast(updateIntent);
                finish();
            }

            public void onIconClick(NavigationItem item) {
                onItemClick(item);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterCategories);
        distinctUntilChanged(
                map(db.getCategoryDao().getCategoriesLiveData(localAccount.getId()), fromDatabase -> {
                    List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(NoteListWidgetConfigurationActivity.this, fromDatabase);

                    ArrayList<NavigationItem> items = new ArrayList<>(fromDatabase.size() + 3);
                    items.add(new NavigationItem(MainActivity.ADAPTER_KEY_RECENT, getString(R.string.label_all_notes), db.getNoteDao().count(localAccount.getId()), R.drawable.ic_access_time_grey600_24dp, RECENT));
                    items.add(new NavigationItem(MainActivity.ADAPTER_KEY_STARRED, getString(R.string.label_favorites), db.getNoteDao().getFavoritesCount(localAccount.getId()), R.drawable.ic_star_yellow_24dp, FAVORITES));

                    if (categories.size() > 2 && categories.get(2).label.isEmpty()) {
                        items.add(new NavigationItem(MainActivity.ADAPTER_KEY_UNCATEGORIZED, getString(R.string.action_uncategorized), null, NavigationAdapter.ICON_NOFOLDER));
                    }

                    for (NavigationItem item : categories) {
                        int slashIndex = item.label.indexOf('/');

                        item.label = slashIndex < 0 ? item.label : item.label.substring(0, slashIndex);
                        item.id = "category:" + item.label;
                        items.add(item);
                    }
                    return items;
                })).observe(this, (navigationItems) -> adapterCategories.setItems(navigationItems));
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
    }
}
