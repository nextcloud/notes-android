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
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.edit.BaseNoteFragment
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.shared.util.WidgetUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.net.toUri

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

    companion object {
        private val TAG: String = SingleNoteWidget::class.java.getSimpleName()
        fun updateAppWidget(context: Context, awm: AppWidgetManager, appWidgetIds: IntArray) {
            val templateIntent = Intent(context, EditNoteActivity::class.java)
            val repo = NotesRepository.getInstance(context)

            appWidgetIds.forEach { appWidgetId ->
                repo.getSingleNoteWidgetData(appWidgetId)?.let { data ->
                    templateIntent.putExtra(BaseNoteFragment.PARAM_ACCOUNT_ID, data.accountId)

                    val serviceIntent = Intent(context, SingleNoteWidgetService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setData(toUri(Intent.URI_INTENT_SCHEME).toUri())
                    }


                    val views = RemoteViews(context.packageName, R.layout.widget_single_note).apply {
                        setPendingIntentTemplate(
                            R.id.single_note_widget_lv, PendingIntent.getActivity(
                                context, appWidgetId, templateIntent,
                                WidgetUtil.pendingIntentFlagCompat(PendingIntent.FLAG_UPDATE_CURRENT)
                            )
                        )
                        setRemoteAdapter(R.id.single_note_widget_lv, serviceIntent)
                        setEmptyView(
                            R.id.single_note_widget_lv,
                            R.id.widget_single_note_placeholder_tv
                        )
                    }

                    awm.run {
                        updateAppWidget(appWidgetId, views)
                        notifyAppWidgetViewDataChanged(appWidgetId, R.id.single_note_widget_lv)
                    }
                }
            }
        }

        @JvmStatic
        fun updateSingleNoteWidgets(context: Context) {
            val intent = Intent(context, SingleNoteWidget::class.java).apply {
                setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            }
            context.sendBroadcast(intent)
        }
    }
}
