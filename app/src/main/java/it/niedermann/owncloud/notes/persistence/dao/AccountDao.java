/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
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
    void deleteAccount(Account localAccount);

    String getAccounts = "SELECT id, url, userName, accountName, eTag, modified, apiVersion, color, textColor, capabilitiesEtag, COALESCE(displayName, userName) as displayName, directEditingAvailable FROM Account";
    String getAccountById = "SELECT id, url, userName, accountName, eTag, modified, apiVersion, color, textColor, capabilitiesEtag, COALESCE(displayName, userName) as displayName, directEditingAvailable FROM Account WHERE ID = :accountId";

    @Query(getAccounts)
    LiveData<List<Account>> getAccounts$();

    @Query(getAccounts)
    List<Account> getAccounts();

    @Query(getAccountById)
    LiveData<Account> getAccountById$(long accountId);

    @Query(getAccountById)
    Account getAccountById(long accountId);

    @Query("SELECT id, url, userName, accountName, eTag, modified, apiVersion, color, textColor, capabilitiesEtag, COALESCE(displayName, userName) as displayName, directEditingAvailable FROM Account WHERE ACCOUNTNAME = :accountName")
    Account getAccountByName(String accountName);

    @Query("SELECT COUNT(*) FROM Account")
    LiveData<Integer> countAccounts$();

    @Query("UPDATE Account SET COLOR = :color WHERE id = :id")
    void updateBrand(long id, @ColorInt Integer color);

    @Query("UPDATE Account SET ETAG = :eTag WHERE ID = :id")
    void updateETag(long id, String eTag);

    @Query("UPDATE Account SET CAPABILITIESETAG = :capabilitiesETag WHERE id = :id")
    void updateCapabilitiesETag(long id, String capabilitiesETag);

    @Query("UPDATE Account SET MODIFIED = :modified WHERE id = :id")
    void updateModified(long id, long modified);

    @Query("UPDATE Account SET APIVERSION = :apiVersion WHERE id = :id AND ((APIVERSION IS NULL AND :apiVersion IS NOT NULL) OR (APIVERSION IS NOT NULL AND :apiVersion IS NULL) OR APIVERSION <> :apiVersion)")
    int updateApiVersion(Long id, String apiVersion);

    @Query("UPDATE Account SET DISPLAYNAME = :displayName WHERE id = :id")
    void updateDisplayName(long id, @Nullable String displayName);

    @Query("UPDATE Account SET directEditingAvailable = :available WHERE id = :id")
    void updateDirectEditingAvailable(long id, boolean available);
}
