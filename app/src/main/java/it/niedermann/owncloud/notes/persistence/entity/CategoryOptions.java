/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.io.Serializable;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Entity(
        primaryKeys = {
                "accountId",
                "category"
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                ),
//                Not possible with SQLite because parent column is not unique
//                @ForeignKey(
//                        entity = Note.class,
//                        parentColumns = {"accountId", "category"},
//                        childColumns = {"accountId", "category"},
//                        onDelete = ForeignKey.CASCADE
//                )
        },
        indices = {
                @Index(name = "IDX_CATEGORIYOPTIONS_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_CATEGORIYOPTIONS_CATEGORY", value = "category"),
                @Index(name = "IDX_CATEGORIYOPTIONS_SORTING_METHOD", value = "sortingMethod"),
                @Index(name = "IDX_UNIQUE_CATEGORYOPTIONS_ACCOUNT_CATEGORY", value = {"accountId", "category"}, unique = true)
        }
)
public class CategoryOptions implements Serializable {
    private long accountId;
    @NonNull
    private String category = "";
    @Nullable
    private CategorySortingMethod sortingMethod;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    @Nullable
    public CategorySortingMethod getSortingMethod() {
        return sortingMethod;
    }

    public void setSortingMethod(@Nullable CategorySortingMethod sortingMethod) {
        this.sortingMethod = sortingMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryOptions that)) return false;

        if (accountId != that.accountId) return false;
        if (!category.equals(that.category)) return false;
        return sortingMethod == that.sortingMethod;
    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + category.hashCode();
        result = 31 * result + (sortingMethod != null ? sortingMethod.hashCode() : 0);
        return result;
    }
}