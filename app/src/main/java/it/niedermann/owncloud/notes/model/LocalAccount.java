package it.niedermann.owncloud.notes.model;

import androidx.annotation.NonNull;

public class LocalAccount {

    private long id;
    private String userName;
    private String accountName;
    private String url;
    private String etag;
    private long modified;

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

    @NonNull
    @Override
    public String toString() {
        return "LocalAccount{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", accountName='" + accountName + '\'' +
                ", url='" + url + '\'' +
                ", etag='" + etag + '\'' +
                ", modified='" + modified + '\'' +
                '}';
    }
}
