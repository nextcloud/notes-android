/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note

private const val ATTACHMENT_ENDPOINT = "/index.php/apps/notes/api/v1/attachment/"


fun Note.getAttachmentImageUrlPrefix(account: Account): String =
    remoteId?.let { "${account.url}${ATTACHMENT_ENDPOINT}$it?path=" }.orEmpty()