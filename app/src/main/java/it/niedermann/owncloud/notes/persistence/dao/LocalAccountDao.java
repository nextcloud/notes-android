package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccount;

@Dao
public interface LocalAccountDao {

    @Insert
    long insert(LocalAccount localAccount);

    @Delete
    int deleteAccount(LocalAccount localAccount);

    @Query("SELECT * FROM LocalAccount WHERE id = :accountId")
    LocalAccount getAccount(long accountId);

    @Query("SELECT * FROM LocalAccount WHERE accountName = :accountName")
    LocalAccount getLocalAccountByAccountName(String accountName);

    @Query("SELECT * FROM LocalAccount")
    List<LocalAccount> getAccounts();

    @Query("SELECT COUNT(*) FROM LocalAccount")
    Integer getAccountsCount();

    @Query("UPDATE LocalAccount SET color = :color AND textColor = :textColor WHERE id = :id")
    void updateBrand(long id, String color, String textColor);

    @Query("UPDATE LocalAccount SET eTag = :eTag WHERE id = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE LocalAccount SET capabilitiesETag = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE LocalAccount SET modified = :modified WHERE id = :id")
    void updateModified(long id, long modified);

    @Query("UPDATE LocalAccount SET apiVersion = :apiVersion WHERE id = :id")
    int updateApiVersion(Long id, String apiVersion);
}
