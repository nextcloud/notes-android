package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_CATEGORIES_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_CATEGORIES_ID", value = "id"),
                @Index(name = "IDX_CATEGORIES_SORTING_METHOD", value = "sortingMethod"),
                @Index(name = "IDX_CATEGORIES_TITLE", value = "title"),
                @Index(name = "IDX_UNIQUE_ACCOUNT_TITLE", value = {"accountId", "title"}, unique = true)
        }
)
public class Category implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long accountId;
    @NonNull
    @ColumnInfo(defaultValue = "")
    private String title = "";
    @Nullable
    private CategorySortingMethod sortingMethod;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
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
        if (!(o instanceof Category)) return false;

        Category category = (Category) o;

        if (id != category.id) return false;
        if (accountId != category.accountId) return false;
        if (!title.equals(category.title)) return false;
        return sortingMethod == category.sortingMethod;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + title.hashCode();
        result = 31 * result + (sortingMethod != null ? sortingMethod.hashCode() : 0);
        return result;
    }
}