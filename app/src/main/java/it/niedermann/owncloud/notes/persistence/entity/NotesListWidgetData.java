package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity(
        tableName = "WIDGET_NOTE_LISTS",
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "ID",
                        childColumns = "ACCOUNT_ID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "CATEGORY_ID",
                        childColumns = "ID",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class NotesListWidgetData extends AbstractWidgetData {

    @Ignore
    public static final int MODE_DISPLAY_ALL = 0;
    @Ignore
    public static final int MODE_DISPLAY_STARRED = 1;
    @Ignore
    public static final int MODE_DISPLAY_CATEGORY = 2;

    @IntRange(from = 0, to = 2)
    @ColumnInfo(name = "MODE")
    private int mode;

    @ColumnInfo(name = "CATEGORY_ID")
    private Long categoryId;

    @Nullable
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "NotesListWidgetData{" +
                "mode=" + mode +
                ", categoryId=" + categoryId +
                '}';
    }
}