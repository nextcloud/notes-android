package it.niedermann.owncloud.notes.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class Category implements Serializable {

    @Nullable
    public final String category;
    @Nullable
    public final Boolean favorite;

    public Category(@Nullable String category, @Nullable Boolean favorite) {
        this.category = category;
        this.favorite = favorite;
    }
}
