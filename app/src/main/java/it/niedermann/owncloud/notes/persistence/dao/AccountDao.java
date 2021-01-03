package it.niedermann.owncloud.notes.persistence.dao;

import androidx.annotation.ColorInt;
import androidx.lifecycle.LiveData;
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

    String getAccountById = "SELECT * FROM Account WHERE ID = :accountId";
    String getAccountByName = "SELECT * FROM Account WHERE ACCOUNTNAME = :accountName";
    String getAccounts = "SELECT * FROM Account";
    String countAccounts = "SELECT COUNT(*) FROM Account";

    @Query(getAccountById)
    LiveData<Account> getAccountById$(long accountId);

    @Query(getAccountById)
    Account getAccountById(long accountId);

    @Query(getAccountByName)
    LiveData<Account> getAccountByName$(String accountName);

    @Query(getAccountByName)
    Account getAccountByName(String accountName);

    @Query(getAccounts)
    LiveData<List<Account>> getAccounts$();

    @Query(getAccounts)
    List<Account> getAccounts();

    @Query(countAccounts)
    LiveData<Integer> countAccounts$();

    @Query(countAccounts)
    Integer countAccounts();

    @Query("UPDATE Account SET COLOR = :color, TEXTCOLOR = :textColor WHERE id = :id")
    void updateBrand(long id, @ColorInt Integer color, @ColorInt Integer textColor);

    @Query("UPDATE Account SET ETAG = :eTag WHERE ID = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE Account SET CAPABILITIESETAG = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE Account SET MODIFIED = :modified WHERE id = :id")
    void updateModified(long id, long modified);

    @Query("UPDATE Account SET APIVERSION = :apiVersion WHERE id = :id")
    int updateApiVersion(Long id, String apiVersion);
}
