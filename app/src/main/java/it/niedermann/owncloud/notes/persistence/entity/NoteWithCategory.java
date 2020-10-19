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
    private String category;

    public NoteWithCategory() {
        // Default constructor
    }

    @Ignore
    public NoteWithCategory(@NonNull Note note) {
        this(note, null);
    }

    @Ignore
    public NoteWithCategory(@NonNull Note note, @Nullable String category) {
        this.note = note;
        this.category = category;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public Long getId() {
        return note.getId();
    }

    public Long getRemoteId() {
        return note.getRemoteId();
    }

    public Long getAccountId() {
        return note.getAccountId();
    }

    public DBStatus getStatus() {
        return note.getStatus();
    }

    public String getTitle() {
        return note.getTitle();
    }

    public Calendar getModified() {
        return note.getModified();
    }

    public String getContent() {
        return note.getContent();
    }

    public Boolean getFavorite() {
        return note.getFavorite();
    }

    public String getETag() {
        return note.getETag();
    }

    @NonNull
    public String getExcerpt() {
        return note.getExcerpt();
    }

    public Integer getScrollY() {
        return note.getScrollY();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteWithCategory)) return false;

        NoteWithCategory that = (NoteWithCategory) o;

        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        return category != null ? category.equals(that.category) : that.category == null;
    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
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
