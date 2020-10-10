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
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId"
                )
        },
        indices = {
                @Index(name = "IDX_NOTE_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_NOTE_CATEGORY", value = "categoryId"),
                @Index(name = "IDX_NOTE_FAVORITE", value = "favorite"),
                @Index(name = "IDX_NOTE_MODIFIED", value = "modified"),
                @Index(name = "IDX_NOTE_REMOTEID", value = "remoteId"),
                @Index(name = "IDX_NOTE_STATUS", value = "status")
        }
)
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long remoteId;
    private Long accountId;
    private DBStatus status = DBStatus.VOID;
    private String title;
    @ColumnInfo(defaultValue = "0")
    private Calendar modified;
    private String content;
    @ColumnInfo(defaultValue = "0")
    private Boolean favorite;
    private Long categoryId;
    private String eTag;
    @NonNull
    @ColumnInfo(defaultValue = "")
    private String excerpt = "";
    @ColumnInfo(defaultValue = "0")
    private Integer scrollY = 0;

    public Note() {
        super();
    }

    @Ignore
    public Note(Long remoteId, Calendar modified, String title, String content, Boolean favorite, String eTag) {
        this.remoteId = remoteId;
        this.title = title;
        this.modified = modified;
        this.content = content;
        this.favorite = favorite;
        this.eTag = eTag;
    }

    @Ignore
    public Note(long id, Long remoteId, Calendar modified, String title, String content, boolean favorite, String etag, DBStatus status, long accountId, @NonNull String excerpt, Integer scrollY) {
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

    @NonNull
    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(@NonNull String excerpt) {
        this.excerpt = excerpt;
    }

    public Integer getScrollY() {
        return scrollY;
    }

    public void setScrollY(Integer scrollY) {
        this.scrollY = scrollY;
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