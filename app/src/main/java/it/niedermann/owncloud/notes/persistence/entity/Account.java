package it.niedermann.owncloud.notes.persistence.entity;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import it.niedermann.owncloud.notes.shared.model.Capabilities;

@Entity(
        indices = {
                @Index(name = "IDX_ACCOUNT_MODIFIED", value = "modified"),
                @Index(name = "IDX_ACCOUNT_URL", value = "url"),
                @Index(name = "IDX_ACCOUNT_USERNAME", value = "userName"),
                @Index(name = "IDX_ACCOUNT_ACCOUNTNAME", value = "accountName"),
                @Index(name = "IDX_ACCOUNT_ETAG", value = "eTag")
        }
)
public class Account implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    @ColumnInfo(defaultValue = "")
    private String url = "";
    @NonNull
    @ColumnInfo(defaultValue = "")
    private String userName = "";
    @NonNull
    @ColumnInfo(defaultValue = "")
    private String accountName = "";
    @Nullable
    private String eTag;
    @Nullable
    private Calendar modified;
    @Nullable
    private String apiVersion;
    @ColorInt
    @ColumnInfo(defaultValue = "-16743735")
    private int color = Color.parseColor("#0082C9");
    @ColorInt
    @ColumnInfo(defaultValue = "-16777216")
    private int textColor = Color.WHITE;
    @Nullable
    private String capabilitiesETag;
    @Nullable
    private String displayName;
    private boolean directEditingAvailable;

    public Account() {
        // Default constructor
    }

    public Account(@NonNull String url, @NonNull String username, @NonNull String accountName, @Nullable String displayName, @NonNull Capabilities capabilities) {
        setUrl(url);
        setUserName(username);
        setAccountName(accountName);
        setDisplayName(displayName);
        setCapabilities(capabilities);
    }

    public void setCapabilities(@NonNull Capabilities capabilities) {
        capabilitiesETag = capabilities.getETag();
        apiVersion = capabilities.getApiVersion();
        directEditingAvailable = capabilities.isDirectEditingAvailable();
        setColor(capabilities.getColor());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    @NonNull
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(@NonNull String accountName) {
        this.accountName = accountName;
    }

    @Nullable
    public String getETag() {
        return eTag;
    }

    public void setETag(@Nullable String eTag) {
        this.eTag = eTag;
    }

    @Nullable
    public Calendar getModified() {
        return modified;
    }

    public void setModified(@Nullable Calendar modified) {
        this.modified = modified;
    }

    @Nullable
    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(@Nullable String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Nullable
    public String getCapabilitiesETag() {
        return capabilitiesETag;
    }

    public void setCapabilitiesETag(@Nullable String capabilitiesETag) {
        this.capabilitiesETag = capabilitiesETag;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public boolean isDirectEditingAvailable() {
        return directEditingAvailable;
    }

    public void setDirectEditingAvailable(boolean directEditingAvailable) {
        this.directEditingAvailable = directEditingAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;

        if (id != account.id) return false;
        if (color != account.color) return false;
        if (textColor != account.textColor) return false;
        if (!url.equals(account.url)) return false;
        if (!userName.equals(account.userName)) return false;
        if (!accountName.equals(account.accountName)) return false;
        if (!Objects.equals(eTag, account.eTag)) return false;
        if (!Objects.equals(modified, account.modified))
            return false;
        if (!Objects.equals(apiVersion, account.apiVersion))
            return false;
        if (!Objects.equals(capabilitiesETag, account.capabilitiesETag))
            return false;
        return directEditingAvailable == account.directEditingAvailable;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + url.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + accountName.hashCode();
        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        result = 31 * result + color;
        result = 31 * result + textColor;
        result = 31 * result + (capabilitiesETag != null ? capabilitiesETag.hashCode() : 0);
        result = 31 * result + (directEditingAvailable ? 1 : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                ", accountName='" + accountName + '\'' +
                ", eTag='" + eTag + '\'' +
                ", modified=" + modified +
                ", apiVersion='" + apiVersion + '\'' +
                ", color=" + color +
                ", textColor=" + textColor +
                ", capabilitiesETag='" + capabilitiesETag + '\'' +
                ", directEditingAvailable='" + directEditingAvailable + '\'' +
                '}';
    }
}
