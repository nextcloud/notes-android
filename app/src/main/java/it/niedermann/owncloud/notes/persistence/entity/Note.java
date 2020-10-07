package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.Item;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "accountId",
                        childColumns = "id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "categoryId",
                        childColumns = "id",
                        onDelete = ForeignKey.SET_DEFAULT
                )
        },
        indices = {
                @Index(value = "remoteId"),
                @Index(value = "accountId"),
                @Index(value = "categoryId"),
                @Index(value = "status"),
                @Index(value = "favorite"),
                @Index(value = "modified")
        }
)
public class Note implements Serializable, Item {
    @PrimaryKey
    private Long id;
    private Long remoteId;
    private Long accountId;
    private Long categoryId;
    private DBStatus status = DBStatus.VOID;
    private String title;
    private Calendar modified;
    private String content;
    private Boolean favorite;
    private String eTag;
    private String excerpt;
    private Integer scrollY;

    public Note() {
        super();
    }

    @Ignore
    public Note(long remoteId, Calendar modified, String title, String content, Boolean favorite, String eTag) {
        this.remoteId = remoteId;
        this.title = title;
        this.modified = modified;
        this.content = content;
        this.favorite = favorite;
        this.eTag = eTag;
    }

    @Ignore
    public Note(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, String etag, DBStatus status, long accountId, String excerpt, Integer scrollY) {
        this(remoteId, modified, title, content, favorite, etag);
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
                ", categoryId=" + categoryId +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", modified=" + modified +
                ", favorite=" + favorite +
                ", eTag='" + eTag + '\'' +
                ", scrollY=" + scrollY +
                '}';
    }
}