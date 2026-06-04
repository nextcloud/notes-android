/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist;

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
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityInteractiveWidgetConfigurationBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.widget.notelist.NoteListViewModel;

import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_STARRED;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;

public class InteractiveNoteListWidgetConfigurationActivity extends LockedActivity {
    private static final String TAG = InteractiveNoteListWidgetConfigurationActivity.class.getSimpleName();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Account localAccount = null;

    private ActivityInteractiveWidgetConfigurationBinding binding;
    private NoteListViewModel viewModel;
    private NavigationAdapter adapterCategories;
    private NotesRepository repo = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        repo = NotesRepository.getInstance(this);
        readAppWidgetId();

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "INVALID_APPWIDGET_ID");
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(NoteListViewModel.class);
        binding = ActivityInteractiveWidgetConfigurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        restorePreferences();

        adapterCategories = new NavigationAdapter(this, createNavigationClickListener());
        binding.recyclerView.setAdapter(adapterCategories);

        loadAccount();
    }

    private void readAppWidgetId() {
        final var args = getIntent().getExtras();
        if (args != null) {
            appWidgetId = args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    private void restorePreferences() {
        binding.favoritesFirstCheckbox.setChecked(InteractiveWidgetPreferences.isFavoritesFirst(this, appWidgetId));
        if (InteractiveWidgetPreferences.getSortOrder(this, appWidgetId) == WidgetSortOrder.OLDEST_FIRST) {
            binding.sortOldestFirst.setChecked(true);
        } else {
            binding.sortNewestFirst.setChecked(true);
        }
    }

    private NavigationClickListener createNavigationClickListener() {
        return new NavigationClickListener() {
            @Override
            public void onItemClick(NavigationItem item) {
                persistSelection(item);
            }

            @Override
            public void onIconClick(NavigationItem item) {
                onItemClick(item);
            }
        };
    }

    private void persistSelection(NavigationItem item) {
        if (localAccount == null) {
            Log.w(TAG, "No account loaded; ignoring selection");
            return;
        }

        final boolean favoritesFirst = binding.favoritesFirstCheckbox.isChecked();
        final WidgetSortOrder sortOrder = binding.sortOldestFirst.isChecked()
                ? WidgetSortOrder.OLDEST_FIRST
                : WidgetSortOrder.NEWEST_FIRST;

        final var data = new NotesListWidgetData();
        data.setId(appWidgetId);
        applyNavigationMode(data, item);
        data.setAccountId(localAccount.getId());
        data.setThemeMode(NotesApplication.getAppTheme(getApplicationContext()).getModeId());

        executor.submit(() -> {
            repo.createOrUpdateNoteListWidgetData(data);
            InteractiveWidgetPreferences.save(getApplicationContext(), appWidgetId, favoritesFirst, sortOrder);

            final var updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, getApplicationContext(), InteractiveNoteListWidget.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, updateIntent);
            getApplicationContext().sendBroadcast(updateIntent);
            finish();
        });
    }

    private void applyNavigationMode(NotesListWidgetData data, NavigationItem item) {
        if (item.type == null) {
            data.setMode(MODE_DISPLAY_ALL);
            Log.e(TAG, "Unknown item navigation type. Fallback to show " + RECENT);
            return;
        }

        switch (item.type) {
            case RECENT:
                data.setMode(MODE_DISPLAY_ALL);
                break;
            case FAVORITES:
                data.setMode(MODE_DISPLAY_STARRED);
                break;
            case UNCATEGORIZED:
                data.setMode(MODE_DISPLAY_CATEGORY);
                data.setCategory(null);
                break;
            case DEFAULT_CATEGORY:
            default:
                if (item.getClass() == NavigationItem.CategoryNavigationItem.class) {
                    data.setMode(MODE_DISPLAY_CATEGORY);
                    data.setCategory(((NavigationItem.CategoryNavigationItem) item).category);
                } else {
                    data.setMode(MODE_DISPLAY_ALL);
                    Log.e(TAG, "Unknown item navigation type. Fallback to show " + RECENT);
                }
                break;
        }
    }

    private void loadAccount() {
        executor.submit(() -> {
            try {
                this.localAccount = repo.getAccountByName(SingleAccountHelper.getCurrentSingleSignOnAccount(this).name);
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                Log.w(TAG, "Account not found", e);
                Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show();
                Log.w(TAG, "onCreate: user not logged in");
                finish();
                return;
            }
            runOnUiThread(() -> viewModel.getAdapterCategories(localAccount.getId()).observe(this, (navigationItems) -> adapterCategories.setItems(navigationItems)));
        });
    }

    @Override
    public void applyBrand(int color) {
        if (binding == null) {
            return;
        }

        final var util = BrandingUtil.of(color, this);
        util.platform.themeCheckbox(binding.favoritesFirstCheckbox);
        util.platform.themeRadioButton(binding.sortNewestFirst);
        util.platform.themeRadioButton(binding.sortOldestFirst);
    }
}
