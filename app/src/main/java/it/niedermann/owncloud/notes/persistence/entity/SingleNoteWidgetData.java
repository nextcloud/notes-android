package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.ColumnInfo;
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
                        entity = Note.class,
                        parentColumns = "id",
                        childColumns = "noteId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_SINGLENOTEWIDGETDATA_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_SINGLENOTEWIDGETDATA_NOTEID", value = "noteId"),
        }
)
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