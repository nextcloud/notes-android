package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;

@Entity()
public class LocalAccountEntity {
    @PrimaryKey
    public int id;

    public String url;
    public String username;
    public String accountName;
    public String eTag;
    public int modified;
    public String apiVersion;
    public String color;
    public String textColor;
    public String capabilitiesETag;
}

//                DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);