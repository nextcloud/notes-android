/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.nextcloud.android.common.ui.util.PlatformThemeUtil
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData
import it.niedermann.owncloud.notes.shared.util.NoteUtil

class InteractiveNoteListWidgetFactory internal constructor(private val context: Context, intent: Intent) :
    RemoteViewsFactory {
    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private val repo: NotesRepository = NotesRepository.getInstance(context)
    private val dbNotes: MutableList<Note> = ArrayList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        dbNotes.clear()
        try {
            val widgetData = repo.getNoteListWidgetData(appWidgetId)
            if (widgetData == null) {
                Log.w(TAG, "Widget data is null")
                return
            }

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

            applySorting(context, appWidgetId, dbNotes)
        } catch (e: Exception) {
            Log.w(TAG, "Error caught at onDataSetChanged", e)
        }
    }

    override fun onDestroy() = Unit

    override fun getCount(): Int {
        return dbNotes.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val note = dbNotes.getOrNull(position) ?: return null

        return RemoteViews(context.packageName, R.layout.widget_interactive_entry).apply {
            setOnClickFillInIntent(R.id.interactive_entry, getOpenNoteIntent(context, note))

            setTextViewText(R.id.interactive_entry_title, note.title)

            if (note.excerpt.isEmpty()) {
                setViewVisibility(R.id.interactive_entry_excerpt, View.GONE)
            } else {
                setViewVisibility(R.id.interactive_entry_excerpt, View.VISIBLE)
                setTextViewText(
                    R.id.interactive_entry_excerpt,
                    note.excerpt.replace(NoteUtil.EXCERPT_LINE_SEPARATOR, "\n")
                )
            }

            if (note.category.isEmpty()) {
                setViewVisibility(R.id.interactive_entry_category, View.GONE)
            } else {
                setViewVisibility(R.id.interactive_entry_category, View.VISIBLE)
                setTextViewText(R.id.interactive_entry_category, note.category)

                val textColorId = if (PlatformThemeUtil.isDarkMode(context)) {
                    R.color.text_color
                } else {
                    R.color.category_border
                }
                val textColor = ContextCompat.getColor(context, textColorId)
                setTextColor(R.id.interactive_entry_category, textColor)
            }

            if (note.favorite) {
                setViewVisibility(R.id.interactive_entry_fav_icon, View.VISIBLE)
                setImageViewResource(R.id.interactive_entry_fav_icon, R.drawable.ic_star_yellow_24dp)
            } else {
                setViewVisibility(R.id.interactive_entry_fav_icon, View.GONE)
            }
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return dbNotes[position].id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private val TAG: String = InteractiveNoteListWidgetFactory::class.java.simpleName
    }
}

private fun applySorting(context: Context, appWidgetId: Int, notes: MutableList<Note>) {
    val sortOrder = InteractiveWidgetPreferences.getSortOrder(context, appWidgetId)
    val byDate: Comparator<Note> = if (sortOrder == WidgetSortOrder.OLDEST_FIRST) {
        compareBy { it.modified?.timeInMillis ?: 0L }
    } else {
        compareByDescending { it.modified?.timeInMillis ?: 0L }
    }

    if (!InteractiveWidgetPreferences.isFavoritesFirst(context, appWidgetId)) {
        notes.sortWith(byDate)
        return
    }

    notes.sortWith(compareByDescending<Note> { it.favorite }.then(byDate))
}

private fun getOpenNoteIntent(context: Context, note: Note): Intent {
    val bundle = Bundle().apply {
        putLong(EditNoteActivity.PARAM_NOTE_ID, note.id)
        putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.accountId)
    }

    return Intent(context, EditNoteActivity::class.java).apply {
        setPackage(context.packageName)
        putExtras(bundle)
        data = toUri(Intent.URI_INTENT_SCHEME).toUri()
    }
}
