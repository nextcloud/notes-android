package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.Item;

public class NoteWithCategory implements Serializable, Item {
    @Embedded
    private Note note;
    private String category;

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

    public Long getCategoryId() {
        return note.getCategoryId();
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
}
