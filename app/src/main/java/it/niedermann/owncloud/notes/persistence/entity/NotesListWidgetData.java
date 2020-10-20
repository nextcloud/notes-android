package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_NOTESLISTWIDGETDATA_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_NOTESLISTWIDGETDATA_CATEGORYID", value = "categoryId"),
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
    private int mode;
    @Nullable
    private Long categoryId;

    @Nullable
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setMode(@IntRange(from = 0, to = 2) int mode) {
        this.mode = mode;
    }

    @IntRange(from = 0, to = 2)
    public int getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotesListWidgetData)) return false;

        NotesListWidgetData that = (NotesListWidgetData) o;

        if (mode != that.mode) return false;
        return categoryId != null ? categoryId.equals(that.categoryId) : that.categoryId == null;
    }

    @Override
    public int hashCode() {
        int result = mode;
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "NotesListWidgetData{" +
                "mode=" + mode +
                ", categoryId=" + categoryId +
                '}';
    }
}