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

    @Query("SELECT * FROM ACCOUNTS WHERE ID = :accountId")
    LocalAccount getAccount(long accountId);

    @Query("SELECT * FROM ACCOUNTS WHERE ACCOUNT_NAME = :accountName")
    LocalAccount getLocalAccountByAccountName(String accountName);

    @Query("SELECT * FROM ACCOUNTS")
    List<LocalAccount> getAccounts();

    @Query("SELECT COUNT(*) FROM ACCOUNTS")
    Integer getAccountsCount();

    @Query("UPDATE ACCOUNTS SET COLOR = :color AND TEXT_COLOR = :textColor WHERE id = :id")
    void updateBrand(long id, String color, String textColor);

    @Query("UPDATE ACCOUNTS SET ETAG = :eTag WHERE ID = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE ACCOUNTS SET CAPABILITIES_ETAG = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE ACCOUNTS SET MODIFIED = :modified WHERE id = :id")
    void updateModified(long id, long modified);

    @Query("UPDATE ACCOUNTS SET API_VERSION = :apiVersion WHERE id = :id")
    int updateApiVersion(Long id, String apiVersion);
}
