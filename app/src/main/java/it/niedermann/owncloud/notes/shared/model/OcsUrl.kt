/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model

import com.google.gson.annotations.Expose

data class OcsUrl(
    @Expose
    @JvmField
    var url: String? = null
)
