package it.niedermann.owncloud.notes.model;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * DBNote represents a single note from the local SQLite database with all attributes.
 * It extends OwnCloudNote with attributes required for local data management.
 */
public class DBNote extends OwnCloudNote implements Item, Serializable {

    private long id;
    private DBStatus status;
    private String excerpt = "";

    public DBNote(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, DBStatus status) {
        super(remoteId, modified, title, content, favorite);
        this.id = id;
        setExcerpt(content);
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public DBStatus getStatus() {
        return status;
    }

    public void setStatus(DBStatus status) {
        this.status = status;
    }

    public String getExcerpt() {
        return excerpt;
    }

    private void setExcerpt(String content) {
        excerpt = NoteUtil.generateNoteExcerpt(content);
    }

    public CharSequence getSpannableContent() {
        // TODO Cache the generated CharSequence not possible because CharSequence does not implement Serializable
        return NoteUtil.parseMarkDown(getContent());
    }

    public void setContent(String content) {
        super.setContent(content);
        setExcerpt(content);
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public String toString() {
        return "#" + getId() + "/R"+getRemoteId()+" " + (isFavorite() ? " (*) " : "     ") + getTitle() + " (" + getModified(NoteSQLiteOpenHelper.DATE_FORMAT) + ") " + getStatus();
    }
}
