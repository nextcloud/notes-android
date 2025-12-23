/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.cursoradapter.widget.CursorAdapter
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ItemSuggestionAdapterBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.share.helper.AvatarLoader

/**
 * [CursorAdapter] used to display search suggestions for sharees for sharing notes.
 *
 * This adapter handles the layout and binding of suggestion data, including the
 * display of user avatars or system icons based on the provided search cursor.
 *
 * @param context The [Context] in which the adapter is running.
 * @param cursor The [Cursor] from which to get the data.
 * @param account The [Account] used for loading authenticated avatars.
 */
class SuggestionAdapter(context: Context, cursor: Cursor?, private val account: Account) :
    CursorAdapter(context, cursor, false) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val binding = ItemSuggestionAdapterBinding.inflate(LayoutInflater.from(context), parent, false)
        val brandingUtil = BrandingUtil.of(BrandingUtil.readBrandMainColor(parent.context), parent.context)
        binding.root.setBackgroundColor(brandingUtil.getScheme(parent.context).surfaceContainerHigh)
        return binding.root
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val suggestion =
            cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
        view.findViewById<TextView>(R.id.suggestion_text).text = suggestion

        val icon = view.findViewById<ImageView>(R.id.suggestion_icon)
        val iconColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1)

        if (iconColumn != -1) {
            try {
                val iconId = cursor.getIntOrNull(iconColumn)
                if (iconId != null) {
                    icon.setImageDrawable(ContextCompat.getDrawable(context, iconId))
                }
            } catch (_: Exception) {
                try {
                    val username = cursor.getStringOrNull(iconColumn)
                    if (username != null) {
                        AvatarLoader.load(context, icon, account, username)
                    }
                } catch (_: Exception) {
                    icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_account_circle_grey_24dp))
                }
            }
        }
    }
}
