package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CategoryEntity {
    @PrimaryKey
    public int id;

    public int remoteId;

    public int accountId;

    public String status;

    public String title;

    public int modified;

    public String content;

    public String eTag;

    public String excerpt;

    public int scrollY;

}
//                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_category_id + "), " +
//                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
//                DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);