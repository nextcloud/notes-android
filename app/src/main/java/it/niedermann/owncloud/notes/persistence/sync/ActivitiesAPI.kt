/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import android.content.Context
import android.net.Uri
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.activities.GetActivitiesRemoteOperation

class ActivitiesAPI (
    val context: Context,
    val ssoAccount: SingleSignOnAccount
) {

    val client = NextcloudClient(
        Uri.parse(ssoAccount.url),
        ssoAccount.userId,
        "???", // TODO what's this?
        context

    )

    suspend fun fetch(noteId: Long, lastGiven: Long) : RemoteOperationResult<Any> {
        val op = GetActivitiesRemoteOperation(
            noteId,
            lastGiven
        )
        return op.execute(client) as RemoteOperationResult<Any>
    }


}