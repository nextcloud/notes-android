package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity()
public class WidgetNotesListEntity {
    @Ignore
    public static final int MODE_DISPLAY_ALL = 0;
    @Ignore
    public static final int MODE_DISPLAY_STARRED = 1;
    @Ignore
    public static final int MODE_DISPLAY_CATEGORY = 2;

    @PrimaryKey
    private long id;

    private long accountId;

    @IntRange(from = 0, to = 2)
    private int themeMode;

    @IntRange(from = 0, to = 2)
    private int mode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }
}