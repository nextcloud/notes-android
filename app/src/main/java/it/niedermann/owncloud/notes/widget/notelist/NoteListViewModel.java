/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.notelist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesRepository;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;

public class NoteListViewModel extends AndroidViewModel {

    private static final String TAG = NoteListViewModel.class.getSimpleName();

    @NonNull
    private final NotesRepository repo;

    public NoteListViewModel(@NonNull Application application) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
    }

    public LiveData<List<NavigationItem>> getAdapterCategories(Long accountId) {
        return distinctUntilChanged(
                switchMap(distinctUntilChanged(repo.count$(accountId)), (count) -> {
                    Log.v(TAG, "[getAdapterCategories] countLiveData: " + count);
                    return switchMap(distinctUntilChanged(repo.countFavorites$(accountId)), (favoritesCount) -> {
                        Log.v(TAG, "[getAdapterCategories] getFavoritesCountLiveData: " + favoritesCount);
                        return map(distinctUntilChanged(repo.getCategories$(accountId)), fromDatabase -> {
                            final var categories = convertToCategoryNavigationItem(getApplication(), fromDatabase);

                            final var items = new ArrayList<NavigationItem>(fromDatabase.size() + 3);
                            items.add(new NavigationItem(MainActivity.ADAPTER_KEY_RECENT, getApplication().getString(R.string.label_all_notes), count, R.drawable.ic_access_time_grey600_24dp, RECENT));
                            items.add(new NavigationItem(MainActivity.ADAPTER_KEY_STARRED, getApplication().getString(R.string.label_favorites), favoritesCount, R.drawable.ic_star_yellow_24dp, FAVORITES));

                            if (categories.size() > 2 && categories.get(2).label.isEmpty()) {
                                items.add(new NavigationItem(MainActivity.ADAPTER_KEY_UNCATEGORIZED, "", null, NavigationAdapter.ICON_NOFOLDER));
                            }

                            for (final var item : categories) {
                                final int slashIndex = item.label.indexOf('/');

                                item.label = slashIndex < 0 ? item.label : item.label.substring(0, slashIndex);
                                item.id = "category:" + item.label;
                                items.add(item);
                            }
                            return items;
                        });
                    });
                })
        );
    }
}
