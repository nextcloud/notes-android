package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Entity(
        tableName = "CATEGORIES",
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "ID",
                        childColumns = "CATEGORY_ID",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "CATEGORIES_CATEGORY_ACCOUNT_ID_idx", value = "CATEGORY_ACCOUNT_ID"),
                @Index(name = "CATEGORIES_CATEGORY_ID_idx", value = "CATEGORY_ID"),
                @Index(name = "CATEGORIES_CATEGORY_SORTING_METHOD_idx", value = "CATEGORY_SORTING_METHOD"),
                @Index(name = "CATEGORIES_CATEGORY_TITLE_idx", value = "CATEGORY_TITLE")
        }
)
public class Category {
    @PrimaryKey
    @ColumnInfo(name = "CATEGORY_ID")
    private long id;
    @ColumnInfo(name = "CATEGORY_ACCOUNT_ID")
    private long accountId;
    @ColumnInfo(name = "CATEGORY_TITLE")
    private String title;
    @ColumnInfo(name = "CATEGORY_SORTING_METHOD")
    private CategorySortingMethod categorySortingMethod;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategorySortingMethod getCategorySortingMethod() {
        return categorySortingMethod;
    }

    public void setCategorySortingMethod(CategorySortingMethod categorySortingMethod) {
        this.categorySortingMethod = categorySortingMethod;
    }
}