package it.niedermann.owncloud.notes.main.navigation;

import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;

public class NavigationItem {
    @NonNull
    public String id;
    @NonNull
    public String label;
    @DrawableRes
    public int icon;
    @Nullable
    public Integer count;
    @Nullable
    public ENavigationCategoryType type;

    public NavigationItem(@NonNull String id, @NonNull String label, @Nullable Integer count, @DrawableRes int icon) {
        this.id = id;
        this.label = label;
        this.type = TextUtils.isEmpty(label) ? UNCATEGORIZED : null;
        this.count = count;
        this.icon = icon;
    }

    public NavigationItem(@NonNull String id, @NonNull String label, @Nullable Integer count, @DrawableRes int icon, @NonNull ENavigationCategoryType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.count = count;
        this.icon = icon;
    }

    public static class CategoryNavigationItem extends NavigationItem {
        @NonNull
        public Long categoryId;

        public CategoryNavigationItem(@NonNull String id, @NonNull String label, @Nullable Integer count, @DrawableRes int icon, @NonNull Long categoryId) {
            super(id, label, count, icon, ENavigationCategoryType.DEFAULT_CATEGORY);
            this.categoryId = categoryId;
        }
    }
}