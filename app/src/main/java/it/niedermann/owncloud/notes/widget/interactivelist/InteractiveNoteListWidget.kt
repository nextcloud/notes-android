/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.net.toUri
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.edit.EditNoteActivity
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType
import it.niedermann.owncloud.notes.shared.model.NavigationCategory
import it.niedermann.owncloud.notes.shared.util.WidgetUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InteractiveNoteListWidget : AppWidgetProvider() {
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
            updateAppWidget(
                context,
                awm,
                awm.getAppWidgetIds(ComponentName(context, InteractiveNoteListWidget::class.java))
            )
        }

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
            executor.execute {
                repo.removeNoteListWidget(appWidgetId)
                InteractiveWidgetPreferences.remove(context, appWidgetId)
            }
        }
    }

    companion object {
        private val TAG: String = InteractiveNoteListWidget::class.java.simpleName

        fun updateAppWidget(context: Context, awm: AppWidgetManager, appWidgetIds: IntArray) {
            val repo = NotesRepository.getInstance(context)
            appWidgetIds.forEach { appWidgetId ->
                val data = repo.getNoteListWidgetData(appWidgetId) ?: return@forEach
                updateSingleWidget(context, awm, appWidgetId, data)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            awm: AppWidgetManager,
            appWidgetId: Int,
            data: NotesListWidgetData
        ) {
            val serviceIntent = Intent(context, InteractiveNoteListWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setData(toUri(Intent.URI_INTENT_SCHEME).toUri())
            }

            val openTemplateIntent = Intent(context, EditNoteActivity::class.java).apply {
                setPackage(context.packageName)
            }
            val openTemplateFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val openTemplatePendingIntent =
                PendingIntent.getActivity(context, 0, openTemplateIntent, openTemplateFlags)

            val createNotePendingIntent =
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    createNoteIntent(context, data),
                    WidgetUtil.pendingIntentFlagCompat(PendingIntent.FLAG_UPDATE_CURRENT)
                )

            val views = RemoteViews(context.packageName, R.layout.widget_interactive_note_list).apply {
                setRemoteAdapter(R.id.interactive_note_list_lv, serviceIntent)
                setPendingIntentTemplate(R.id.interactive_note_list_lv, openTemplatePendingIntent)
                setEmptyView(
                    R.id.interactive_note_list_lv,
                    R.id.interactive_note_list_placeholder_tv
                )
                setOnClickPendingIntent(R.id.interactive_create_note_button, createNotePendingIntent)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setColorStateList(
                        R.id.interactive_create_note_button,
                        "setBackgroundTintList",
                        ColorStateList.valueOf(BrandingUtil.readBrandMainColor(context))
                    )
                }
            }

            awm.run {
                updateAppWidget(appWidgetId, views)
                notifyAppWidgetViewDataChanged(intArrayOf(appWidgetId), R.id.interactive_note_list_lv)
            }
        }

        private fun createNoteIntent(context: Context, data: NotesListWidgetData): Intent {
            val navigationCategory = if (data.mode == NotesListWidgetData.MODE_DISPLAY_STARRED) {
                NavigationCategory(ENavigationCategoryType.FAVORITES)
            } else {
                NavigationCategory(data.accountId, data.category)
            }

            val bundle = Bundle().apply {
                putSerializable(EditNoteActivity.PARAM_CATEGORY, navigationCategory)
                putLong(EditNoteActivity.PARAM_ACCOUNT_ID, data.accountId)
            }

            return Intent(context, EditNoteActivity::class.java).apply {
                setPackage(context.packageName)
                putExtras(bundle)
                setData("interactive-create://${data.id}".toUri())
            }
        }

        @JvmStatic
        fun updateInteractiveNoteListWidgets(context: Context) {
            val intent = Intent(context, InteractiveNoteListWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
