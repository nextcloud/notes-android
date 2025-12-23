/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "share_table")
data class ShareEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,
    val note: String? = null,
    val path: String? = null,
    val file_target: String? = null,
    val share_with: String? = null,
    val share_with_displayname: String? = null,
    val uid_file_owner: String? = null,
    val displayname_file_owner: String? = null,
    val uid_owner: String? = null,
    val displayname_owner: String? = null,
    val url: String? = null,
    val expiration_date: Long? = null
)
