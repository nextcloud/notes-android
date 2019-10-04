package it.niedermann.owncloud.notes.model;

public class LocalAccount {

    private long id;
    private String userName;
    private String accountName;
    private String url;
    private String displayName;
    private String token;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LocalAccount{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", accountName='" + accountName + '\'' +
                ", url='" + url + '\'' +
                ", displayName='" + displayName + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
