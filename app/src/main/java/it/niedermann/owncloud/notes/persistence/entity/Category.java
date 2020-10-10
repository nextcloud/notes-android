package it.niedermann.owncloud.notes.persistence.entity;

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
                @Index(name = "IDX_CATEGORIES_TITLE", value = "title")
        }
)
public class Category implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long accountId;
    private String title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategorySortingMethod getSortingMethod() {
        return sortingMethod;
    }

    public void setSortingMethod(CategorySortingMethod sortingMethod) {
        this.sortingMethod = sortingMethod;
    }
}