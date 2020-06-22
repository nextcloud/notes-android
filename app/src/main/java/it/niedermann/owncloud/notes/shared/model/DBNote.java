package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

/**
 * DBNote represents a single note from the local SQLite database with all attributes.
 * It extends CloudNote with attributes required for local data management.
 */
public class DBNote extends CloudNote implements Item, Serializable {

    private final long id;
    private final long accountId;
    private DBStatus status;
    private String excerpt;
    private int scrollY;

    public DBNote(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, String category, String etag, DBStatus status, long accountId, String excerpt, int scrollY) {
        super(remoteId, modified, title, content, favorite, category, etag);
        this.id = id;
        this.excerpt = excerpt;
        this.status = status;
        this.accountId = accountId;
        this.scrollY = scrollY;
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

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void setContent(String content) {
        super.setContent(content);
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

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }
}
