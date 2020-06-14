package it.niedermann.owncloud.notes.model;


import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

import it.niedermann.owncloud.notes.persistence.NotesClient;

public class LocalAccount {

    private long id;
    private String userName;
    private String accountName;
    private String url;
    private String etag;
    private String capabilitiesETag;
    private long modified;
    @Nullable
    private ApiVersion preferredApiVersion;
    @ColorInt
    private int color;
    @ColorInt
    private int textColor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEtag() {
        return etag;
    }

    public void setETag(String etag) {
        this.etag = etag;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    @Nullable
    public ApiVersion getPreferredApiVersion() {
        return preferredApiVersion;
    }

    public String getCapabilitiesETag() {
        return capabilitiesETag;
    }

    public void setCapabilitiesETag(String capabilitiesETag) {
        this.capabilitiesETag = capabilitiesETag;
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

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocalAccount{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", accountName='" + accountName + '\'' +
                ", url='" + url + '\'' +
                ", etag='" + etag + '\'' +
                ", modified=" + modified +
                ", preferredApiVersion='" + preferredApiVersion + '\'' +
                ", color=" + color +
                ", textColor=" + textColor +
                ", capabilitiesETag=" + capabilitiesETag +
                '}';
    }
}
