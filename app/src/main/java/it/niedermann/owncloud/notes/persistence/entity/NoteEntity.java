package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;
import androidx.room.TypeConverters;

import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Entity()
public class NoteEntity {
    @PrimaryKey
    private long id;
    private long remoteId;
    private long accountId;
    private DBStatus status;
    private String title;
    private long modified;
    private String content;
    private Boolean favorite;
    private String eTag;
    private String excerpt;
    private int scrollY;
    private CategoryEntity category;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(long remoteId) {
        this.remoteId = remoteId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public DBStatus getStatus() {
        return status;
    }

    public void setStatus(DBStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    @Deprecated
    public static DBNote entityToDBNote(NoteEntity entity) {
        if(entity == null) {
            return null;
        }
        Calendar modified = Calendar.getInstance();
        modified.setTimeInMillis(entity.getModified() * 1000);
        DBNote note = new DBNote(
                entity.getId(),
                entity.getRemoteId(),
                modified,
                entity.getTitle(),
                entity.getContent(),
                entity.getFavorite(),
                entity.getCategory().getTitle(),
                entity.getETag(),
                entity.getStatus(),
                entity.getAccountId(),
                entity.getExcerpt(),
                entity.getScrollY()
        );
        return note;
    }
}
//                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_category_id + "), " +
//                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
//                DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);