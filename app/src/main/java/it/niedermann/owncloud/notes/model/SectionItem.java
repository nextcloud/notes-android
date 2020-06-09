package it.niedermann.owncloud.notes.model;

import androidx.annotation.Nullable;

public class SectionItem implements Item {

    @Nullable
    private String title;

    public SectionItem(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
