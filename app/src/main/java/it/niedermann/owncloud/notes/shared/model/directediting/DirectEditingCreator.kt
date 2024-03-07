/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

data class DirectEditingCreator(
    @Expose
    val id: String,
    @Expose
    val editor: String,
    @Expose
    val name: String,
    @Expose
    val extension: String,
    @Expose
    val mimetype: String,
    @Expose
    val templates: Boolean,
)
