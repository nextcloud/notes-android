package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Ignore;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.Item;

public class NoteWithCategory implements Serializable, Item {
    @Embedded
    private Note note;
    @NonNull
    private String category = "";

    public NoteWithCategory() {
        // Default constructor
    }

    @Ignore
    public NoteWithCategory(@NonNull Note note) {
        this.note = note;
    }

    @Ignore
    public NoteWithCategory(@NonNull Note note, @NonNull String category) {
        this.note = note;
        this.category = category;
    }

    @NonNull
    public Note getNote() {
        return note;
    }

    public void setNote(@NonNull Note note) {
        this.note = note;
    }

    public long getId() {
        return note.getId();
    }

    @Nullable
    public Long getRemoteId() {
        return note.getRemoteId();
    }

    public long getAccountId() {
        return note.getAccountId();
    }

    @NonNull
    public DBStatus getStatus() {
        return note.getStatus();
    }

    @NonNull
    public String getTitle() {
        return note.getTitle();
    }

    @Nullable
    public Calendar getModified() {
        return note.getModified();
    }

    @NonNull
    public String getContent() {
        return note.getContent();
    }

    public boolean getFavorite() {
        return note.getFavorite();
    }

    @Nullable
    public String getETag() {
        return note.getETag();
    }

    @NonNull
    public String getExcerpt() {
        return note.getExcerpt();
    }

    public int getScrollY() {
        return note.getScrollY();
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteWithCategory)) return false;

        NoteWithCategory that = (NoteWithCategory) o;

        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        return category.equals(that.category);
    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + category.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteWithCategory{" +
                "note=" + note +
                ", category='" + category + '\'' +
                '}';
    }
}
