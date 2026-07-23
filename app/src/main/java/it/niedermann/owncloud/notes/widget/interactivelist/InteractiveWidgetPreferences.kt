/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist

import android.content.Context
import androidx.core.content.edit

object InteractiveWidgetPreferences {
    private const val PREFS_NAME = "interactive_note_list_widget"
    private const val KEY_FAVORITES_FIRST = "favorites_first_"
    private const val KEY_SORT_ORDER = "sort_order_"

    @JvmStatic
    fun save(context: Context, appWidgetId: Int, favoritesFirst: Boolean, sortOrder: WidgetSortOrder) {
        prefs(context).edit {
            putBoolean(KEY_FAVORITES_FIRST + appWidgetId, favoritesFirst)
            putString(KEY_SORT_ORDER + appWidgetId, sortOrder.name)
        }
    }

    @JvmStatic
    fun isFavoritesFirst(context: Context, appWidgetId: Int): Boolean =
        prefs(context).getBoolean(KEY_FAVORITES_FIRST + appWidgetId, false)

    @JvmStatic
    fun getSortOrder(context: Context, appWidgetId: Int): WidgetSortOrder {
        val stored = prefs(context).getString(KEY_SORT_ORDER + appWidgetId, null)
            ?: return WidgetSortOrder.NEWEST_FIRST
        return runCatching { WidgetSortOrder.valueOf(stored) }.getOrDefault(WidgetSortOrder.NEWEST_FIRST)
    }

    @JvmStatic
    fun remove(context: Context, appWidgetId: Int) {
        prefs(context).edit {
            remove(KEY_FAVORITES_FIRST + appWidgetId)
            remove(KEY_SORT_ORDER + appWidgetId)
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
