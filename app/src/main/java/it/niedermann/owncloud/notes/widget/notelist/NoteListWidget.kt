/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.notelist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.owncloud.android.lib.common.utils.Log_OC
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.shared.util.WidgetUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.net.toUri

class NoteListWidget : AppWidgetProvider() {
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAppWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val awm = AppWidgetManager.getInstance(context)

        if (intent.action == null) {
            Log.w(TAG, "Intent action is null")
            return
        }

        if (intent.action != AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Log.w(TAG, "Intent action is not ACTION_APPWIDGET_UPDATE")
            return
        }

        if (!intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            Log.w(TAG, "Update widget via default appWidgetIds")
            updateAppWidget(
                context,
                awm,
                awm.getAppWidgetIds(ComponentName(context, NoteListWidget::class.java))
            )
        }

        Log.w(TAG, "Update widget via given appWidgetIds")

        val appWidgetIds = intArrayOf(intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1)

        updateAppWidget(
            context,
            awm,
            appWidgetIds
        )
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val repo = NotesRepository.getInstance(context)

        for (appWidgetId in appWidgetIds) {
            executor.submit(Runnable { repo.removeNoteListWidget(appWidgetId) })
        }
    }

    companion object {
        private val TAG: String = NoteListWidget::class.java.getSimpleName()
        fun updateAppWidget(context: Context, awm: AppWidgetManager, appWidgetIds: IntArray) {
            val repo = NotesRepository.getInstance(context)
            appWidgetIds.forEach { appWidgetId ->
                repo.getNoteListWidgetData(appWidgetId)?.let { data ->
                    val serviceIntent = Intent(context, NoteListWidgetService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setData(toUri(Intent.URI_INTENT_SCHEME).toUri())
                    }


                    Log.v(TAG, "-- data - $data")

                    val editNoteIntent = Intent(context, EditNoteActivity::class.java).apply {
                        setPackage(context.packageName)
                    }

                    val pendingIntentFlags =
                        WidgetUtil.pendingIntentFlagCompat(PendingIntent.FLAG_UPDATE_CURRENT or Intent.FILL_IN_COMPONENT)
                    val editNotePendingIntent =
                        PendingIntent.getActivity(context, 0, editNoteIntent, pendingIntentFlags)

                    val views = RemoteViews(context.packageName, R.layout.widget_note_list).apply {
                        setRemoteAdapter(R.id.note_list_widget_lv, serviceIntent)
                        setPendingIntentTemplate(R.id.note_list_widget_lv, editNotePendingIntent)
                        setEmptyView(
                            R.id.note_list_widget_lv,
                            R.id.widget_note_list_placeholder_tv
                        )
                    }

                    awm.run {
                        updateAppWidget(appWidgetId, views)
                        notifyAppWidgetViewDataChanged(appWidgetId, R.id.note_list_widget_lv)
                    }
                }
            }
        }

        @JvmStatic
        fun updateNoteListWidgets(context: Context) {
            val intent = Intent(context, NoteListWidget::class.java).apply {
                setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            }
            context.sendBroadcast(intent)
        }
    }
}
