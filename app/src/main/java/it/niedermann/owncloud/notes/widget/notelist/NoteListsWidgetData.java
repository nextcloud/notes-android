package it.niedermann.owncloud.notes.widget.notelist;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

public class NoteListsWidgetData extends AbstractWidgetData {
    public static final int MODE_DISPLAY_ALL = 0;
    public static final int MODE_DISPLAY_STARRED = 1;
    public static final int MODE_DISPLAY_CATEGORY = 2;

    @IntRange(from = 0, to = 2)
    private int mode;
    @Nullable
    private Long categoryId;

    public int getMode() {
        return mode;
    }

    public void setMode(@IntRange(from = 0, to = 2) int mode) {
        this.mode = mode;
    }

    @Nullable
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable Long categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteListsWidgetData{" +
                "mode=" + mode +
                ", categoryId=" + categoryId +
                '}';
    }
}
