/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.notelist;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ActivityNoteListConfigurationBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;

import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_STARRED;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;

public class NoteListWidgetConfigurationActivity extends LockedActivity {
    private static final String TAG = Activity.class.getSimpleName();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Account localAccount = null;

    private ActivityNoteListConfigurationBinding binding;
    private NoteListViewModel viewModel;
    private NavigationAdapter adapterCategories;
    private NotesRepository repo = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        repo = NotesRepository.getInstance(this);
        final var args = getIntent().getExtras();

        if (args != null) {
            appWidgetId = args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "INVALID_APPWIDGET_ID");
            finish();
        }

        viewModel = new ViewModelProvider(this).get(NoteListViewModel.class);
        binding = ActivityNoteListConfigurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapterCategories = new NavigationAdapter(this, new NavigationClickListener() {
            @Override
            public void onItemClick(NavigationItem item) {
                final var data = new NotesListWidgetData();

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
                        case UNCATEGORIZED: {
                            data.setMode(MODE_DISPLAY_CATEGORY);
                            data.setCategory(null);
                        }
                        case DEFAULT_CATEGORY:
                        default: {
                            if (item.getClass() == NavigationItem.CategoryNavigationItem.class) {
                                data.setMode(MODE_DISPLAY_CATEGORY);
                                data.setCategory(((NavigationItem.CategoryNavigationItem) item).category);
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

                executor.submit(() -> {
                    repo.createOrUpdateNoteListWidgetData(data);

                    final var updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, getApplicationContext(), NoteListWidget.class)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, updateIntent);
                    getApplicationContext().sendBroadcast(updateIntent);
                    finish();
                });
            }

            public void onIconClick(NavigationItem item) {
                onItemClick(item);
            }
        });

        binding.recyclerView.setAdapter(adapterCategories);

        executor.submit(() -> {
            try {
                this.localAccount = repo.getAccountByName(SingleAccountHelper.getCurrentSingleSignOnAccount(this).name);
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show();
                // TODO Present user with app login screen
                Log.w(TAG, "onCreate: user not logged in");
                finish();
            }
            runOnUiThread(() -> viewModel.getAdapterCategories(localAccount.getId()).observe(this, (navigationItems) -> adapterCategories.setItems(navigationItems)));
        });
    }

    @Override
    public void applyBrand(int color) {
        // Nothing to do...
    }
}
