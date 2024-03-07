/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.Item;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "accountId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(name = "IDX_NOTE_ACCOUNTID", value = "accountId"),
                @Index(name = "IDX_NOTE_CATEGORY", value = "category"),
                @Index(name = "IDX_NOTE_FAVORITE", value = "favorite"),
                @Index(name = "IDX_NOTE_MODIFIED", value = "modified"),
                @Index(name = "IDX_NOTE_REMOTEID", value = "remoteId"),
                @Index(name = "IDX_NOTE_STATUS", value = "status")
        }
)
public class Note implements Serializable, Item {
    @SerializedName("localId")
    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    @Expose
    @SerializedName("id")
    private Long remoteId;

    private long accountId;

    @NonNull
    private DBStatus status = DBStatus.VOID;

    @NonNull
    @ColumnInfo(defaultValue = "")
    @Expose
    private String title = "";

    @NonNull
    @Expose
    @ColumnInfo(defaultValue = "")
    private String category = "";

    @Expose
    @Nullable
    private Calendar modified;

    @NonNull
    @ColumnInfo(defaultValue = "")
    @Expose
    private String content = "";

    @Expose
    @ColumnInfo(defaultValue = "0")
    private boolean favorite = false;

    @Expose
    @Nullable
    @SerializedName("etag")
    private String eTag;

    @NonNull
    @ColumnInfo(defaultValue = "")
    private String excerpt = "";

    @ColumnInfo(defaultValue = "0")
    private int scrollY = 0;

    public Note() {
        super();
    }

    @Ignore
    public Note(@Nullable Long remoteId, @Nullable Calendar modified, @NonNull String title, @NonNull String content, @NonNull String category, boolean favorite, @Nullable String eTag) {
        this.remoteId = remoteId;
        this.title = title;
        this.modified = modified;
        this.content = content;
        this.favorite = favorite;
        this.category = category;
        this.eTag = eTag;
    }

    @Ignore
    public Note(long id, @Nullable Long remoteId, @Nullable Calendar modified, @NonNull String title, @NonNull String content, @NonNull String category, boolean favorite, @Nullable String etag, @NonNull DBStatus status, long accountId, @NonNull String excerpt, int scrollY) {
        this(remoteId, modified, title, content, category, favorite, etag);
        this.id = id;
        this.status = status;
        this.accountId = accountId;
        this.excerpt = excerpt;
        this.scrollY = scrollY;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    @Nullable
    public Long getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(@Nullable Long remoteId) {
        this.remoteId = remoteId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public DBStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull DBStatus status) {
        this.status = status;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @Nullable
    public Calendar getModified() {
        return modified;
    }

    public void setModified(@Nullable Calendar modified) {
        this.modified = modified;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Nullable
    public String getETag() {
        return eTag;
    }

    public void setETag(@Nullable String eTag) {
        this.eTag = eTag;
    }

    @NonNull
    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(@NonNull String excerpt) {
        this.excerpt = excerpt;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;

        if (id != note.id) return false;
        if (accountId != note.accountId) return false;
        if (favorite != note.favorite) return false;
        if (scrollY != note.scrollY) return false;
        if (!Objects.equals(remoteId, note.remoteId))
            return false;
        if (status != note.status) return false;
        if (!title.equals(note.title)) return false;
        if (!category.equals(note.category)) return false;
        if (!Objects.equals(modified, note.modified))
            return false;
        if (!content.equals(note.content)) return false;
        if (!Objects.equals(eTag, note.eTag)) return false;
        return excerpt.equals(note.excerpt);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (remoteId != null ? remoteId.hashCode() : 0);
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + status.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        result = 31 * result + content.hashCode();
        result = 31 * result + (favorite ? 1 : 0);
        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        result = 31 * result + excerpt.hashCode();
        result = 31 * result + scrollY;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", remoteId=" + remoteId +
                ", accountId=" + accountId +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", modified=" + modified +
                ", content='" + content + '\'' +
                ", favorite=" + favorite +
                ", eTag='" + eTag + '\'' +
                ", excerpt='" + excerpt + '\'' +
                ", scrollY=" + scrollY +
                '}';
    }
}