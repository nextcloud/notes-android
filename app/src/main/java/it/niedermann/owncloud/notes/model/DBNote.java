package it.niedermann.owncloud.notes.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * DBNote represents a single note from the local SQLite database with all attributes.
 * It extends CloudNote with attributes required for local data management.
 */
public class DBNote extends CloudNote implements Item, Serializable {

    private long id;
    private long accountId;
    private DBStatus status;
    private String excerpt = "";

    public DBNote(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, String category, String etag, DBStatus status, long accountId) {
        super(remoteId, modified, title, content, favorite, category, etag);
        this.id = id;
        setExcerpt(content);
        this.status = status;
        this.accountId = accountId;
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
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

    public void setExcerptDirectly(String content) {
        excerpt = content;
    }

    private void setExcerpt(String content) {
        excerpt = NoteUtil.generateNoteExcerpt(content);
    }

    public void setContent(String content) {
        super.setContent(content);
        setExcerpt(content);
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "DBNote{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", status=" + status +
                ", excerpt='" + excerpt + '\'' +
                '}';
    }
}
