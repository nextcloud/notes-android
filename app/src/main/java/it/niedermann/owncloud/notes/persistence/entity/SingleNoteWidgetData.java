package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Entity;
import androidx.room.Ignore;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity()
public class SingleNoteWidgetData extends AbstractWidgetData {
    private long noteId;

    public SingleNoteWidgetData() {

    }

    @Ignore
    public SingleNoteWidgetData(int id, long accountId, long noteId, int modeId) {
        super(id, accountId, modeId);
        setNoteId(noteId);
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }
}