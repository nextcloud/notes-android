package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

import it.niedermann.owncloud.notes.persistence.entity.Category;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.DEFAULT_CATEGORY;

public class NavigationCategory implements Serializable {

    @NonNull
    private final ENavigationCategoryType type;
    @Nullable
    private final Category category;

    public NavigationCategory(@NonNull ENavigationCategoryType type) {
        if (type == DEFAULT_CATEGORY) {
            throw new IllegalArgumentException("If you want to provide a " + DEFAULT_CATEGORY + ", call the constructor with a " + Category.class.getSimpleName());
        }
        this.type = type;
        this.category = null;
    }

    public NavigationCategory(@NonNull Category category) {
        this.type = DEFAULT_CATEGORY;
        this.category = category;
    }

    @NonNull
    public ENavigationCategoryType getType() {
        return type;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavigationCategory)) return false;

        NavigationCategory that = (NavigationCategory) o;

        if (type != that.type) return false;
        return category != null ? category.equals(that.category) : that.category == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "NavigationCategory{" +
                "type=" + type +
                ", category=" + category +
                '}';
    }
}
