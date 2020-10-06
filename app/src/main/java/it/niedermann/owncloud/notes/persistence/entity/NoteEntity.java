package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.Item;

@Entity()
public class NoteEntity implements Serializable, Item {
    @PrimaryKey
    private Long id;
    private Long remoteId;
    private Long accountId;
    private DBStatus status;
    private String title;
    private Calendar modified;
    private String content;
    private Boolean favorite;
    private String eTag;
    private String excerpt;
    private Integer scrollY;
    @Embedded(prefix = "category_")
    private Category category;

    public NoteEntity() {
        super();
    }

    public NoteEntity(long remoteId, Calendar modified, String title, String content, Boolean favorite, String category, String eTag) {
        this.remoteId = remoteId;
        this.title = title;
        this.modified = modified;
        this.content = content;
        this.favorite = favorite;
        this.eTag = eTag;
        this.category = new Category();
        this.category.setTitle(category);
    }

    public NoteEntity(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, String category, String etag, DBStatus status, long accountId, String excerpt, Integer scrollY) {
        this(remoteId, modified, title, content, favorite, category, etag);
        this.id = id;
        this.status = status;
        this.accountId = accountId;
        this.excerpt = excerpt;
        this.scrollY = scrollY;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(Long remoteId) {
        this.remoteId = remoteId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
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

    public Calendar getModified() {
        return modified;
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

    public Integer getScrollY() {
        return scrollY;
    }

    public void setScrollY(Integer scrollY) {
        this.scrollY = scrollY;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Ignore
    @Override
    public boolean isSection() {
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "NoteEntity{" +
                "id=" + id +
                ", remoteId=" + remoteId +
                ", accountId=" + accountId +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", modified=" + modified +
                ", favorite=" + favorite +
                ", eTag='" + eTag + '\'' +
                ", scrollY=" + scrollY +
                ", category=" + category +
                '}';
    }
}
//                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_category_id + "), " +
//                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
//                DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);