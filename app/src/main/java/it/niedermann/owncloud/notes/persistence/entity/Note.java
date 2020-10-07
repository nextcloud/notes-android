package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
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
        tableName = "NOTES",
        foreignKeys = {
                @ForeignKey(
                        entity = LocalAccount.class,
                        parentColumns = "ID",
                        childColumns = "ACCOUNT_ID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "CATEGORY_ID",
                        childColumns = "CATEGORY",
                        onDelete = ForeignKey.SET_DEFAULT
                )
        },
        indices = {
                @Index(name = "NOTES_ACCOUNT_ID_idx", value = "ACCOUNT_ID"),
                @Index(name = "NOTES_CATEGORY_idx", value = "CATEGORY"),
                @Index(name = "NOTES_FAVORITE_idx", value = "FAVORITE"),
                @Index(name = "NOTES_MODIFIED_idx", value = "MODIFIED"),
                @Index(name = "NOTES_REMOTEID_idx", value = "REMOTEID"),
                @Index(name = "NOTES_STATUS_idx", value = "STATUS")
        }
)
public class Note implements Serializable, Item {
    @PrimaryKey
    @ColumnInfo(name = "ID")
    private Long id;
    @ColumnInfo(name = "REMOTEID")
    private Long remoteId;
    @ColumnInfo(name = "ACCOUNT_ID")
    private Long accountId;
    @ColumnInfo(name = "STATUS")
    private DBStatus status = DBStatus.VOID;
    @ColumnInfo(name = "TITLE")
    private String title;
    @ColumnInfo(name = "MODIFIED")
    private Calendar modified;
    @ColumnInfo(name = "CONTENT")
    private String content;
    @ColumnInfo(name = "FAVORITE")
    private Boolean favorite;
    @ColumnInfo(name = "CATEGORY")
    private Long categoryId;
    @ColumnInfo(name = "ETAG")
    private String eTag;
    @ColumnInfo(name = "EXCERPT")
    private String excerpt;
    @ColumnInfo(name = "SCROLL_Y")
    private Integer scrollY;
    @Ignore
    private String category;

    public Note() {
        super();
    }

    @Ignore
    public Note(long remoteId, Calendar modified, String title, String content, Boolean favorite, String category, String eTag) {
        this.remoteId = remoteId;
        this.title = title;
        this.modified = modified;
        this.content = content;
        this.favorite = favorite;
        this.eTag = eTag;
        this.category = category;
    }

    @Ignore
    public Note(long id, long remoteId, Calendar modified, String title, String content, boolean favorite, String category, String etag, DBStatus status, long accountId, String excerpt, Integer scrollY) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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