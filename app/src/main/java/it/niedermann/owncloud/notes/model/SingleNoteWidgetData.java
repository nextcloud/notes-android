package it.niedermann.owncloud.notes.model;

public class SingleNoteWidgetData {
    private int appWidgetId;
    private long accountId;
    private long noteId;
    private int themeMode;

    public SingleNoteWidgetData() {

    }

    public SingleNoteWidgetData(int appWidgetId, long accountId, long noteId, int themeMode) {
        this.appWidgetId = appWidgetId;
        this.accountId = accountId;
        this.noteId = noteId;
        this.themeMode = themeMode;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }
}
