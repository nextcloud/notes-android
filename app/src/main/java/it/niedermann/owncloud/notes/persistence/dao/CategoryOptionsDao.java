/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.CategoryOptions;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Dao
public interface CategoryOptionsDao {

    @Insert
    void addCategoryOptions(CategoryOptions entity);

    /**
     * This method is used to modify the sorting method for one category by title.
     * The user can determine use which sorting method to show the notes for a category.
     * When the user changes the sorting method, this method should be called.
     *
     * @param accountId     The user accountID
     * @param category      The category
     * @param sortingMethod The sorting method in {@link CategorySortingMethod} enum format
     */
    @Query("UPDATE CategoryOptions SET sortingMethod = :sortingMethod WHERE category = :category AND accountId = :accountId")
    int modifyCategoryOrder(long accountId, String category, CategorySortingMethod sortingMethod);

    /**
     * This function is used to get the sorting method of a category by title.
     * The sorting method of the category can be used to decide
     * to use which sorting method to show the notes for each categories.
     *
     * @param category The category
     * @return The sorting method in {@link CategorySortingMethod} enum format
     */
    @Query("SELECT sortingMethod FROM CategoryOptions WHERE accountId = :accountId AND category = :category")
    LiveData<CategorySortingMethod> getCategoryOrder(long accountId, String category);
}
