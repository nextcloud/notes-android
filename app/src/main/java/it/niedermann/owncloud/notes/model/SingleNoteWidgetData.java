package it.niedermann.owncloud.notes.model;

public class SingleNoteWidgetData extends AbstractWidgetData {
    private long noteId;

    public SingleNoteWidgetData() {

    }

    public SingleNoteWidgetData(int appWidgetId, long accountId, long noteId, int themeMode) {
        super(appWidgetId, accountId, themeMode);
        this.noteId = noteId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

}
