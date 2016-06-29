package it.niedermann.owncloud.notes.model;

import java.util.Calendar;

import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class DBNote extends Note {

    private DBStatus status;

    public DBNote(long id, Calendar modified, String title, String content, DBStatus status) {
        super(id, modified, title, content);
        this.status = status;
    }

    public DBStatus getStatus() {
        return status;
    }

    public void setStatus(DBStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "#" + getId() + " " + getTitle() + " (" + getModified(NoteSQLiteOpenHelper.DATE_FORMAT) + ") " + getStatus();
    }
}
