package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;

@Dao
public interface LocalAccountDao {

    @Insert
    long insert(LocalAccountEntity localAccountEntity);

    @Delete
    int deleteAccount(LocalAccountEntity localAccountEntity);

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

    @Query("UPDATE localaccountentity SET apiVersion = :apiVersion WHERE id = :id")
    int updateApiVersion(Long id, String apiVersion);
}
