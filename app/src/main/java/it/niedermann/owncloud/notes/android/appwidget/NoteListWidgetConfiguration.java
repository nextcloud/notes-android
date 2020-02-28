package it.niedermann.owncloud.notes.android.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.NotesListViewActivity;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.util.Notes;

public class NoteListWidgetConfiguration extends AppCompatActivity {
    private static final String TAG = Activity.class.getSimpleName();

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


    LocalAccount localAccount = null;

    private NavigationAdapter adapterCategories;
    private NavigationAdapter.NavigationItem itemRecent, itemFavorites;
    private NotesDatabase db = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_note_list_configuration);

        db = NotesDatabase.getInstance(this);
        try {
            this.localAccount = db.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(this).name);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show();
            // TODO Present user with app login screen
            Log.w(TAG, "onCreate: user not logged in");
            finish();
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

        itemRecent = new NavigationAdapter.NavigationItem(NotesListViewActivity.ADAPTER_KEY_RECENT,
                                                            getString(R.string.label_all_notes),
                                                            null,
                                                            R.drawable.ic_access_time_grey600_24dp);
        itemFavorites = new NavigationAdapter.NavigationItem(NotesListViewActivity.ADAPTER_KEY_STARRED,
                                                            getString(R.string.label_favorites),
                                                            null,
                                                            R.drawable.ic_star_yellow_24dp);
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        adapterCategories = new NavigationAdapter(new NavigationAdapter.ClickListener() {
            @Override
            public void onItemClick(NavigationAdapter.NavigationItem item) {
                SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

                if (itemRecent.equals(item)) {
                    sp.putInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, NoteListWidget.NLW_DISPLAY_ALL);
                } else if (itemFavorites.equals(item)) {
                    sp.putInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, NoteListWidget.NLW_DISPLAY_STARRED);
                } else {
                    String category = "";
                    if (!item.label.equals(getString(R.string.action_uncategorized))) {
                        category = item.label;
                    }
                    sp.putInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, NoteListWidget.NLW_DISPLAY_CATEGORY);
                    sp.putString(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, category);
                }

                sp.putLong(NoteListWidget.ACCOUNT_ID_KEY + appWidgetId, localAccount.getId());
                sp.putBoolean(NoteListWidget.DARK_THEME_KEY + appWidgetId, Notes.getAppTheme(getApplicationContext()));
                sp.apply();

                Intent updateIntent = new Intent(   AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                                                    getApplicationContext(), NoteListWidget.class);
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, updateIntent);
                getApplicationContext().sendBroadcast(updateIntent);
                finish();
            }

            public void onIconClick(NavigationAdapter.NavigationItem item) {
                onItemClick(item);
            }
        });

        recyclerView = findViewById(R.id.nlw_config_recyclerv);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterCategories);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadCategoryListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class LoadCategoryListTask extends AsyncTask<Void, Void, List<NavigationAdapter.NavigationItem>> {
        @Override
        protected List<NavigationAdapter.NavigationItem> doInBackground(Void... voids) {
            if (localAccount == null) {
                return new ArrayList<>();
            }
            NavigationAdapter.NavigationItem itemUncategorized;
            List<NavigationAdapter.NavigationItem> categories = db.getCategories(localAccount.getId());

            if (!categories.isEmpty() && categories.get(0).label.isEmpty()) {
                itemUncategorized = categories.get(0);
                itemUncategorized.label = getString(R.string.action_uncategorized);
                itemUncategorized.icon = NavigationAdapter.ICON_NOFOLDER;
            }

            Map<String, Integer> favorites = db.getFavoritesCount(localAccount.getId());
            int numFavorites = favorites.containsKey("1") ? favorites.get("1") : 0;
            int numNonFavorites = favorites.containsKey("0") ? favorites.get("0") : 0;
            itemFavorites.count = numFavorites;
            itemRecent.count = numFavorites + numNonFavorites;

            ArrayList<NavigationAdapter.NavigationItem> items = new ArrayList<>();
            items.add(itemRecent);
            items.add(itemFavorites);

            for (NavigationAdapter.NavigationItem item : categories) {
                int slashIndex = item.label.indexOf('/');

                item.label = slashIndex < 0 ? item.label : item.label.substring(0, slashIndex);
                item.id = "category:" + item.label;
                items.add(item);
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<NavigationAdapter.NavigationItem> items) {
            adapterCategories.setItems(items);
        }
    }
}
