package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class OldCategory implements Serializable {

    @Nullable
    public final String category;
    @Nullable
    public final Boolean favorite;

    public OldCategory(@Nullable String category, @Nullable Boolean favorite) {
        this.category = category;
        this.favorite = favorite;
    }
}
