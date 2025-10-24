/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.singlenote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SingleNoteWidget : AppWidgetProvider() {
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAppWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        val awm = AppWidgetManager.getInstance(context)

        val provider = ComponentName(context, SingleNoteWidget::class.java)
        val appWidgetIds = awm.getAppWidgetIds(provider)
        updateAppWidget(context, awm, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val repo = NotesRepository.getInstance(context)

        for (appWidgetId in appWidgetIds) {
            executor.submit { repo.removeSingleNoteWidget(appWidgetId) }
        }
        super.onDeleted(context, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, awm: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = NotesRepository.getInstance(context)
        appWidgetIds.forEach { appWidgetId ->
            repo.getSingleNoteWidgetData(appWidgetId)?.let { data ->
                val pendingIntent = getPendingIntent(context, appWidgetId)
                val serviceIntent = getServiceIntent(context, appWidgetId)
                val views = getRemoteViews(context, pendingIntent, serviceIntent)
                awm.run {
                    updateAppWidget(appWidgetId, views)
                    notifyAppWidgetViewDataChanged(appWidgetId, R.id.single_note_widget_lv)
                }
            }
        }
    }

    private fun getPendingIntent(
        context: Context,
        id: Int
    ): PendingIntent {
        val intent = Intent(context, EditNoteActivity::class.java).apply {
            setPackage(context.packageName)
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_MUTABLE or
                Intent.FILL_IN_COMPONENT

        return PendingIntent.getActivity(
            context,
            id,
            intent,
            pendingIntentFlags
        )
    }

    private fun getServiceIntent(context: Context, id: Int): Intent {
        return Intent(context, SingleNoteWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            val dataFlag = toUri(Intent.URI_INTENT_SCHEME) + id
            val dataUri = dataFlag.toUri()
            setData(dataUri)
        }
    }

    private fun getRemoteViews(
        context: Context,
        pendingIntent: PendingIntent,
        serviceIntent: Intent
    ): RemoteViews {
        return RemoteViews(
            context.packageName,
            R.layout.widget_single_note
        ).apply {
            setPendingIntentTemplate(R.id.single_note_widget_lv, pendingIntent)
            setEmptyView(
                R.id.single_note_widget_lv,
                R.id.widget_single_note_placeholder_tv
            )
            setRemoteAdapter(R.id.single_note_widget_lv, serviceIntent)
        }
    }

    companion object {
        @JvmStatic
        fun updateSingleNoteWidgets(context: Context) {
            val intent = Intent(context, SingleNoteWidget::class.java).apply {
                setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            }
            context.sendBroadcast(intent)
        }
    }
}
