package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Embedded;

public class NoteWithCategory {
    @Embedded
    private Note note;
    @Embedded
    private Category category;

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
