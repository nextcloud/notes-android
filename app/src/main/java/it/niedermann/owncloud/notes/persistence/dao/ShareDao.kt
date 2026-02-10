/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.niedermann.owncloud.notes.persistence.entity.ShareEntity

@Dao
interface ShareDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addShareEntities(entities: List<ShareEntity>)

    @Query("SELECT * FROM share_table WHERE path = :path")
    fun getShareEntities(path: String): List<ShareEntity>

    @Query("DELETE FROM share_table WHERE id = :id")
    fun deleteById(id: Int)
}
