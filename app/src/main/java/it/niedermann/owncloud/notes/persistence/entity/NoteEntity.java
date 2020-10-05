package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
public class NoteEntity {
    @PrimaryKey
    public long id;

    public long remoteId;

    public long accountId;

    public String status;

    public String title;

    public long modified;

    public String content;

    public Boolean favorite;

    public String eTag;

    public String excerpt;

    public int scrollY;

    public long categoryId;

}
//                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_category_id + "), " +
//                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
//                DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);