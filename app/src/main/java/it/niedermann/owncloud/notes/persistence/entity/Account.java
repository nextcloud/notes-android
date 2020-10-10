package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

import it.niedermann.owncloud.notes.persistence.NotesClient;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.util.ColorUtil;

@Entity(
        indices = {
                @Index(name = "IDX_ACCOUNT_MODIFIED", value = "modified"),
                @Index(name = "IDX_ACCOUNT_URL", value = "url"),
                @Index(name = "IDX_ACCOUNT_USERNAME", value = "userName"),
                @Index(name = "IDX_ACCOUNT_ACCOUNTNAME", value = "accountName"),
                @Index(name = "IDX_ACCOUNT_ETAG", value = "eTag")
        }
)
public class Account {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String url;
    private String userName;
    private String accountName;
    private String eTag;
    private Calendar modified;
    private String apiVersion;
    @ColorInt
    @ColumnInfo(defaultValue = "-16743735")
    private Integer color;
    @ColorInt
    @ColumnInfo(defaultValue = "-16777216")
    private Integer textColor;
    private String capabilitiesETag;

    public Account() {
        // Default constructor
    }

    public Account(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        setUrl(url);
        setUserName(username);
        setAccountName(accountName);
        setCapabilities(capabilities);
    }

    @Nullable
    @Ignore
    private ApiVersion preferredApiVersion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public Calendar getModified() {
        return modified;
    }

    public void setModified(Calendar modified) {
        this.modified = modified;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        setPreferredApiVersion(apiVersion);
    }

    public String getCapabilitiesETag() {
        return capabilitiesETag;
    }

    public void setCapabilitiesETag(String capabilitiesETag) {
        this.capabilitiesETag = capabilitiesETag;
    }

    public void setCapabilities(@NonNull Capabilities capabilities) {
        capabilitiesETag = capabilities.getETag();
        apiVersion = capabilities.getApiVersion();
        setColor(capabilities.getColor());
        setTextColor(capabilities.getTextColor());
    }

    @Nullable
    public ApiVersion getPreferredApiVersion() {
        return preferredApiVersion;
    }

    /**
     * @param availableApiVersions <code>["0.2", "1.0", ...]</code>
     */
    public void setPreferredApiVersion(@Nullable String availableApiVersions) {
        // TODO move this logic to NotesClient?
        try {
            if (availableApiVersions == null) {
                this.preferredApiVersion = null;
                return;
            }
            JSONArray versionsArray = new JSONArray(availableApiVersions);
            Collection<ApiVersion> supportedApiVersions = new HashSet<>(versionsArray.length());
            for (int i = 0; i < versionsArray.length(); i++) {
                ApiVersion parsedApiVersion = ApiVersion.of(versionsArray.getString(i));
                for (ApiVersion temp : NotesClient.SUPPORTED_API_VERSIONS) {
                    if (temp.compareTo(parsedApiVersion) == 0) {
                        supportedApiVersions.add(parsedApiVersion);
                        break;
                    }
                }
            }
            this.preferredApiVersion = Collections.max(supportedApiVersions);
        } catch (JSONException | NoSuchElementException e) {
            e.printStackTrace();
            this.preferredApiVersion = null;
        }
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public void setTextColor(Integer textColor) {
        this.textColor = textColor;
    }

    @NonNull
    @Override
    public String toString() {
        return "Account{" +
                "accountName='" + accountName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;

        Account account = (Account) o;

        if (id != null ? !id.equals(account.id) : account.id != null) return false;
        if (url != null ? !url.equals(account.url) : account.url != null) return false;
        if (userName != null ? !userName.equals(account.userName) : account.userName != null)
            return false;
        if (accountName != null ? !accountName.equals(account.accountName) : account.accountName != null)
            return false;
        if (eTag != null ? !eTag.equals(account.eTag) : account.eTag != null) return false;
        if (modified != null ? !modified.equals(account.modified) : account.modified != null)
            return false;
        if (apiVersion != null ? !apiVersion.equals(account.apiVersion) : account.apiVersion != null)
            return false;
        if (color != null ? !color.equals(account.color) : account.color != null) return false;
        if (textColor != null ? !textColor.equals(account.textColor) : account.textColor != null)
            return false;
        if (capabilitiesETag != null ? !capabilitiesETag.equals(account.capabilitiesETag) : account.capabilitiesETag != null)
            return false;
        return preferredApiVersion != null ? preferredApiVersion.equals(account.preferredApiVersion) : account.preferredApiVersion == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (accountName != null ? accountName.hashCode() : 0);
        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (textColor != null ? textColor.hashCode() : 0);
        result = 31 * result + (capabilitiesETag != null ? capabilitiesETag.hashCode() : 0);
        result = 31 * result + (preferredApiVersion != null ? preferredApiVersion.hashCode() : 0);
        return result;
    }
}