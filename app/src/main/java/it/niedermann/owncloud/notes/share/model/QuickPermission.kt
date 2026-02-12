/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

/**
 * Represents a selectable quick permission option in the UI.
 */
data class QuickPermission(val type: QuickPermissionType, var isSelected: Boolean)
