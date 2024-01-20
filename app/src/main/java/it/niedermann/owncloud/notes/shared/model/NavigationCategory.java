package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.DEFAULT_CATEGORY;

public class NavigationCategory implements Serializable {

    @NonNull
    private final ENavigationCategoryType type;
    @Nullable
    private final String category;
    private final long accountId;

    public NavigationCategory(@NonNull ENavigationCategoryType type) {
        if (type == DEFAULT_CATEGORY) {
            throw new IllegalArgumentException("If you want to provide a " + DEFAULT_CATEGORY + ", call the constructor with an accountId and category as arguments");
        }
        this.type = type;
        this.category = null;
        this.accountId = Long.MIN_VALUE;
    }

    public NavigationCategory(long accountId, @Nullable String category) {
        this.type = DEFAULT_CATEGORY;
        this.category = category;
        this.accountId = accountId;
    }

    @NonNull
    public ENavigationCategoryType getType() {
        return type;
    }

    public long getAccountId() {
        return accountId;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavigationCategory that)) return false;

        if (accountId != that.accountId) return false;
        if (type != that.type) return false;
        return Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "NavigationCategory{" +
                "type=" + type +
                ", category='" + category + '\'' +
                ", accountId=" + accountId +
                '}';
    }
}
