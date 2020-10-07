package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "accountId",
                        childColumns = "id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "accountId"),
                @Index(value = "title"),
                @Index(value = "categorySortingMethod")
        }
)
public class Category {
    @PrimaryKey
    private long id;
    private long accountId;
    private String title;
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