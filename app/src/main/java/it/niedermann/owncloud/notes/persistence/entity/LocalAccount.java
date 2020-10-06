package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.Ignore;
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

@Entity()
public class LocalAccount {
    @PrimaryKey
    public Long id;
    private String url;
    private String userName;
    private String accountName;
    private String eTag;
    private Calendar modified;
    private String apiVersion;
    private String capabilitiesETag;
    private String color;
    private String textColor;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        try {
            this.color = ColorUtil.formatColorToParsableHexString(color).substring(1);
        } catch (Exception e) {
            this.color = "0082C9";
        }
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        try {
            this.textColor = ColorUtil.formatColorToParsableHexString(textColor).substring(1);
        } catch (Exception e) {
            this.textColor = "FFFFFF";
        }
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
}

//                DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);