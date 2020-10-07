package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import it.niedermann.owncloud.notes.widget.AbstractWidgetData;

@Entity(
        tableName = "WIDGET_SINGLE_NOTES",
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "ID",
                        childColumns = "ACCOUNT_ID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Note.class,
                        parentColumns = "ID",
                        childColumns = "NOTE_ID",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class SingleNoteWidgetData extends AbstractWidgetData {
    @ColumnInfo(name = "NOTE_ID")
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