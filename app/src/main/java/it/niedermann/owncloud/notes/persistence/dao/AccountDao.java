package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Account;

@Dao
public interface AccountDao {

    @Insert
    long insert(Account localAccount);

    @Delete
    int deleteAccount(Account localAccount);

    @Query("SELECT * FROM Account WHERE ID = :accountId")
    Account getAccount(long accountId);

    @Query("SELECT * FROM Account WHERE ACCOUNTNAME = :accountName")
    Account getLocalAccountByAccountName(String accountName);

    @Query("SELECT * FROM Account")
    List<Account> getAccounts();

    @Query("SELECT COUNT(*) FROM Account")
    Integer getAccountsCount();

    @Query("UPDATE Account SET COLOR = :color AND TEXTCOLOR = :textColor WHERE id = :id")
    void updateBrand(long id, Integer color, Integer textColor);

    @Query("UPDATE Account SET ETAG = :eTag WHERE ID = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE Account SET CAPABILITIESETAG = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE Account SET MODIFIED = :modified WHERE id = :id")
    void updateModified(long id, long modified);

    @Query("UPDATE Account SET APIVERSION = :apiVersion WHERE id = :id")
    int updateApiVersion(Long id, String apiVersion);
}
