package it.niedermann.owncloud.notes.persistence.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;

@Dao
public interface LocalAccountDao {

    @Insert
    long insert(LocalAccountEntity localAccountEntity);

    @Query("SELECT * FROM localaccountentity WHERE id = :accountId")
    LocalAccountEntity getAccount(long accountId);

    @Query("SELECT * FROM localaccountentity WHERE accountName = :accountName")
    LocalAccountEntity getLocalAccountByAccountName(String accountName);

    @Query("SELECT * FROM localaccountentity")
    List<LocalAccountEntity> getAccounts();

    @Query("SELECT COUNT(*) FROM localaccountentity")
    Integer getAccountsCount();

    @Query("UPDATE localaccountentity SET color = :color AND textColor = :textColor WHERE id = :id")
    void updateBrand(long id, String color, String textColor);

    @Query("UPDATE localaccountentity SET eTag = :eTag WHERE id = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE localaccountentity SET capabilitiesETag = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE localaccountentity SET modified = :modified WHERE id = :id")
    void updateModified(long id, long modified);
}
