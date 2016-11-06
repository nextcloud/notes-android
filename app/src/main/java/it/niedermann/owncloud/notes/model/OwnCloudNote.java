package it.niedermann.owncloud.notes.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * OwnCloudNote represents a remote note from an OwnCloud server.
 * It can be directly generated from the JSON answer from the server.
 */
public class OwnCloudNote implements Serializable {
    private long remoteId = 0;
    private String title = "";
    private Calendar modified = null;
    private String content = "";
    private boolean favorite = false;

    public OwnCloudNote(long remoteId, Calendar modified, String title, String content, boolean favorite) {
        this.remoteId = remoteId;
        if (title != null)
            setTitle(title);
        setTitle(title);
        setContent(content);
        setFavorite(favorite);
        this.modified = modified;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(long remoteId) {
        this.remoteId = remoteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = NoteUtil.removeMarkDown(title);
    }

    @SuppressWarnings("WeakerAccess")
    public Calendar getModified() {
        return modified;
    }

    public String getModified(String format) {
        return new SimpleDateFormat(format, Locale.GERMANY)
                .format(this.getModified().getTimeInMillis());
    }

    public void setModified(Calendar modified) {
        this.modified = modified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "#" + getRemoteId() + " " + (isFavorite() ? " (*) " : "     ") + getTitle() + " (" + getModified(NoteSQLiteOpenHelper.DATE_FORMAT) + ")";
    }
}