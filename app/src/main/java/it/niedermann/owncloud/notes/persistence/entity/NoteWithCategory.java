package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Embedded;

import java.io.Serializable;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
