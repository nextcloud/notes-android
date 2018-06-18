package it.niedermann.owncloud.notes.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * CloudNote represents a remote note from an OwnCloud server.
 * It can be directly generated from the JSON answer from the server.
 */
public class CloudNote implements Serializable {
    private long remoteId = 0;
    private String title = "";
    private Calendar modified = null;
    private String content = "";
    private boolean favorite = false;
    private String category = "";
    private String etag = "";

    public CloudNote(long remoteId, Calendar modified, String title, String content, boolean favorite, String category, String etag) {
        this.remoteId = remoteId;
        if (title != null)
            setTitle(title);
        setTitle(title);
        setContent(content);
        setFavorite(favorite);
        setCategory(category);
        setEtag(etag);
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
        if (modified == null)
            return null;
        return new SimpleDateFormat(format, Locale.GERMANY).format(this.getModified().getTimeInMillis());
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

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? "" : category;
    }

    @Override
    public String toString() {
        final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        return "R" + getRemoteId() + " " + (isFavorite() ? " (*) " : "     ") + getCategory() + " / " + getTitle() + " (" + getModified(DATE_FORMAT) + " / " + getEtag() + ")";
    }
}