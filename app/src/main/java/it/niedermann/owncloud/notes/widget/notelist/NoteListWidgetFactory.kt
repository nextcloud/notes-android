/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.notelist

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.core.content.ContextCompat
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.main.MainActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType
import it.niedermann.owncloud.notes.shared.model.NavigationCategory
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil
import androidx.core.net.toUri

class NoteListWidgetFactory internal constructor(private val context: Context, intent: Intent) :
    RemoteViewsFactory {
    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private val repo: NotesRepository = NotesRepository.getInstance(context)
    private val dbNotes: MutableList<Note> = ArrayList()
    private var data: NotesListWidgetData? = null

    override fun onCreate() {
        // Nothing to do here…
    }

    override fun onDataSetChanged() {
        dbNotes.clear()
        try {
            data = repo.getNoteListWidgetData(appWidgetId)
            if (data == null) {
                Log.w(TAG, "Widget data is null")
                return
            }
            val widgetData = data ?: return

            Log.v(TAG, "--- data - $widgetData")

            when (widgetData.mode) {
                NotesListWidgetData.MODE_DISPLAY_ALL -> dbNotes.addAll(
                    repo.searchRecentByModified(
                        widgetData.accountId, "%"
                    )
                )

                NotesListWidgetData.MODE_DISPLAY_STARRED -> dbNotes.addAll(
                    repo.searchFavoritesByModified(
                        widgetData.accountId, "%"
                    )
                )

                else -> {
                    if (widgetData.category != null) {
                        dbNotes.addAll(
                            repo.searchCategoryByModified(
                                widgetData.accountId,
                                "%",
                                widgetData.category
                            )
                        )
                    } else {
                        dbNotes.addAll(
                            repo.searchUncategorizedByModified(
                                widgetData.accountId,
                                "%"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error caught at onDataSetChanged: $e")
        }
    }

    override fun onDestroy() {
        //NoOp
    }

    override fun getCount(): Int {
        return dbNotes.size + 1
    }

    private fun getEditNoteIntent(bundle: Bundle): Intent {
        return Intent(context, EditNoteActivity::class.java).apply {
            setPackage(context.packageName)
            putExtras(bundle)
            setData(toUri(Intent.URI_INTENT_SCHEME).toUri())
        }
    }

    private fun getCreateNoteIntent(localAccount: Account): Intent {
        val bundle = Bundle()

        data?.let {
            val navigationCategory = if (it.mode == NotesListWidgetData.MODE_DISPLAY_STARRED) NavigationCategory(
                ENavigationCategoryType.FAVORITES
            ) else NavigationCategory(localAccount.id, it.category)

            bundle.putSerializable(EditNoteActivity.PARAM_CATEGORY, navigationCategory)
            bundle.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, it.accountId)
        }

        return getEditNoteIntent(bundle)
    }

    private fun getOpenNoteIntent(note: Note): Intent {
        val bundle = Bundle().apply {
            putLong(EditNoteActivity.PARAM_NOTE_ID, note.id)
            putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.accountId)
        }

        return getEditNoteIntent(bundle)
    }

    private fun getRemoteViewFromData(): RemoteViews? {
        val widgetData = data ?: return null

        val localAccount = repo.getAccountById(widgetData.accountId)
        val createNoteIntent = getCreateNoteIntent(localAccount)
        val openIntent = Intent(Intent.ACTION_MAIN).setComponent(
            ComponentName(
                context.packageName,
                MainActivity::class.java.getName()
            )
        )

        return RemoteViews(context.packageName, R.layout.widget_entry_add).apply {
            setOnClickFillInIntent(R.id.widget_entry_content_tv, openIntent)
            setOnClickFillInIntent(R.id.widget_entry_fav_icon, createNoteIntent)
            setTextViewText(
                R.id.widget_entry_content_tv,
                getCategoryTitle(context, widgetData.mode, widgetData.category)
            )
            setImageViewResource(
                R.id.widget_entry_fav_icon,
                R.drawable.ic_add_blue_24dp
            )
            setInt(
                R.id.widget_entry_fav_icon,
                "setColorFilter",
                if (NotesColorUtil.contrastRatioIsSufficient(
                        ContextCompat.getColor(
                            context,
                            R.color.widget_background
                        ), localAccount.color
                    )
                )
                    localAccount.color
                else
                    ContextCompat.getColor(context, R.color.widget_foreground)
            )
        }
    }

    private fun getRemoteViewFromPosition(position: Int): RemoteViews? {
        var position = position
        position--
        if (position < 0 || position >= dbNotes.size) {
            Log.e(TAG, "Could not find position \"$position\" in dbNotes list.")
            return null
        }

        val note = dbNotes[position]
        val openNoteIntent = getOpenNoteIntent(note)

        return RemoteViews(context.packageName, R.layout.widget_entry).apply {
            setOnClickFillInIntent(R.id.widget_note_list_entry, openNoteIntent)
            setTextViewText(R.id.widget_entry_content_tv, note.title)
            setImageViewResource(
                R.id.widget_entry_fav_icon, if (note.favorite)
                    R.drawable.ic_star_yellow_24dp
                else
                    R.drawable.ic_star_grey_ccc_24dp
            )
        }
    }

    override fun getViewAt(position: Int): RemoteViews? {
        return if (position == 0 && data != null) {
            getRemoteViewFromData()
        } else {
            getRemoteViewFromPosition(position)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemId(position: Int): Long {
        var position = position
        if (position == 0) {
            return -1
        } else {
            position--
            if (position > dbNotes.size - 1 || dbNotes.get(position) == null) {
                Log.e(TAG, "Could not find position \"" + position + "\" in dbNotes list.")
                return -2
            }
            return dbNotes[position].id
        }
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private val TAG: String = NoteListWidgetFactory::class.java.getSimpleName()

        private fun getCategoryTitle(
            context: Context,
            displayMode: Int,
            category: String?
        ): String {
            return when (displayMode) {
                NotesListWidgetData.MODE_DISPLAY_STARRED -> context.getString(R.string.label_favorites)
                NotesListWidgetData.MODE_DISPLAY_CATEGORY -> if ("" == category)
                    context.getString(R.string.action_uncategorized)
                else
                    category

                else -> context.getString(R.string.app_name)
            }!!
        }
    }
}
