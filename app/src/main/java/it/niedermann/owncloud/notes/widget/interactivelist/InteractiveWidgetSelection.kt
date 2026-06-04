/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist

import it.niedermann.owncloud.notes.main.MainActivity
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData

object InteractiveWidgetSelection {
    private const val CATEGORY_ID_PREFIX = "category:"
    private const val CATEGORY_SEPARATOR = '/'

    @JvmStatic
    fun navigationItemId(data: NotesListWidgetData?): String {
        if (data == null) {
            return MainActivity.ADAPTER_KEY_RECENT
        }

        return when (data.mode) {
            NotesListWidgetData.MODE_DISPLAY_STARRED -> MainActivity.ADAPTER_KEY_STARRED
            NotesListWidgetData.MODE_DISPLAY_CATEGORY -> categoryId(data.category)
            else -> MainActivity.ADAPTER_KEY_RECENT
        }
    }

    private fun categoryId(category: String?): String {
        if (category.isNullOrEmpty()) {
            return MainActivity.ADAPTER_KEY_UNCATEGORIZED
        }

        return CATEGORY_ID_PREFIX + category.substringBefore(CATEGORY_SEPARATOR)
    }
}
