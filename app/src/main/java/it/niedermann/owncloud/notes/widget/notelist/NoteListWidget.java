package it.niedermann.owncloud.notes.widget.notelist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.NoSuchElementException;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.preferences.DarkModeSetting;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.shared.model.Category;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.NotesApplication;

import static it.niedermann.owncloud.notes.edit.EditNoteActivity.PARAM_CATEGORY;
import static it.niedermann.owncloud.notes.widget.notelist.NoteListsWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.widget.notelist.NoteListsWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.widget.notelist.NoteListsWidgetData.MODE_DISPLAY_STARRED;

public class NoteListWidget extends AppWidgetProvider {
    private static final String TAG = NoteListWidget.class.getSimpleName();

    public static final int PENDING_INTENT_NEW_NOTE_RQ = 0;
    public static final int PENDING_INTENT_EDIT_NOTE_RQ = 1;
    public static final int PENDING_INTENT_OPEN_APP_RQ = 2;

    static void updateAppWidget(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        final NotesDatabase db = NotesDatabase.getInstance(context);

        RemoteViews views;
        DarkModeSetting darkTheme;

        for (int appWidgetId : appWidgetIds) {
            try {
                final NoteListsWidgetData data = db.getNoteListWidgetData(appWidgetId);
                final LocalAccount localAccount = db.getAccount(data.getAccountId());

                String category = null;
                if (data.getCategoryId() != null) {
                    category = db.getCategoryTitleById(data.getAccountId(), data.getCategoryId());
                }

                darkTheme = DarkModeSetting.fromModeID(data.getThemeMode());

                Intent serviceIntent = new Intent(context, NoteListWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                // Launch application when user taps the header icon or app title
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName(context.getPackageName(),
                        MainActivity.class.getName()));

                // Open the main app if the user taps the widget header
                PendingIntent openAppI = PendingIntent.getActivity(context, PENDING_INTENT_OPEN_APP_RQ,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                // Launch create note activity if user taps "+" icon on header
                PendingIntent newNoteI = PendingIntent.getActivity(context, (PENDING_INTENT_NEW_NOTE_RQ + appWidgetId),
                        new Intent(context, EditNoteActivity.class).putExtra(PARAM_CATEGORY, new Category(category, data.getMode() == MODE_DISPLAY_STARRED)),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent templatePI = PendingIntent.getActivity(context, PENDING_INTENT_EDIT_NOTE_RQ,
                        new Intent(context, EditNoteActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Log.v(TAG, "-- data - " + data);

                if (NotesApplication.isDarkThemeActive(context, darkTheme)) {
                    views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list_dark);
                    views.setTextViewText(R.id.widget_note_list_title_tv_dark, getWidgetTitle(context, data.getMode(), category));
                    views.setOnClickPendingIntent(R.id.widget_note_header_icon_dark, openAppI);
                    views.setOnClickPendingIntent(R.id.widget_note_list_title_tv_dark, openAppI);
                    views.setOnClickPendingIntent(R.id.widget_note_list_create_icon_dark, newNoteI);
                    views.setPendingIntentTemplate(R.id.note_list_widget_lv_dark, templatePI);
                    views.setRemoteAdapter(appWidgetId, R.id.note_list_widget_lv_dark, serviceIntent);
                    views.setEmptyView(R.id.note_list_widget_lv_dark, R.id.widget_note_list_placeholder_tv_dark);
                    awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.note_list_widget_lv_dark);
                    if (BrandingUtil.isBrandingEnabled(context)) {
                        views.setInt(R.id.widget_note_header_dark, "setBackgroundColor", localAccount.getColor());
                        views.setInt(R.id.widget_note_header_icon_dark, "setColorFilter", localAccount.getTextColor());
                        views.setInt(R.id.widget_note_list_create_icon_dark, "setColorFilter", localAccount.getTextColor());
                        views.setTextColor(R.id.widget_note_list_title_tv_dark, localAccount.getTextColor());
                    } else {
                        views.setInt(R.id.widget_note_header_dark, "setBackgroundColor", context.getResources().getColor(R.color.defaultBrand));
                        views.setInt(R.id.widget_note_header_icon_dark, "setColorFilter", Color.WHITE);
                        views.setInt(R.id.widget_note_list_create_icon_dark, "setColorFilter", Color.WHITE);
                        views.setTextColor(R.id.widget_note_list_title_tv_dark, Color.WHITE);
                    }
                } else {
                    views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);
                    views.setTextViewText(R.id.widget_note_list_title_tv, getWidgetTitle(context, data.getMode(), category));
                    views.setOnClickPendingIntent(R.id.widget_note_header_icon, openAppI);
                    views.setOnClickPendingIntent(R.id.widget_note_list_title_tv, openAppI);
                    views.setOnClickPendingIntent(R.id.widget_note_list_create_icon, newNoteI);
                    views.setPendingIntentTemplate(R.id.note_list_widget_lv, templatePI);
                    views.setRemoteAdapter(appWidgetId, R.id.note_list_widget_lv, serviceIntent);
                    views.setEmptyView(R.id.note_list_widget_lv, R.id.widget_note_list_placeholder_tv);
                    awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.note_list_widget_lv);
                    if (BrandingUtil.isBrandingEnabled(context)) {
                        views.setInt(R.id.widget_note_header, "setBackgroundColor", localAccount.getColor());
                        views.setInt(R.id.widget_note_header_icon, "setColorFilter", localAccount.getTextColor());
                        views.setInt(R.id.widget_note_list_create_icon, "setColorFilter", localAccount.getTextColor());
                        views.setTextColor(R.id.widget_note_list_title_tv, localAccount.getTextColor());
                    } else {
                        views.setInt(R.id.widget_note_header, "setBackgroundColor", context.getResources().getColor(R.color.defaultBrand));
                        views.setInt(R.id.widget_note_header_icon, "setColorFilter", Color.WHITE);
                        views.setInt(R.id.widget_note_list_create_icon, "setColorFilter", Color.WHITE);
                        views.setTextColor(R.id.widget_note_list_title_tv, Color.WHITE);
                    }
                }

                awm.updateAppWidget(appWidgetId, views);
            } catch (NoSuchElementException e) {
                Log.i(TAG, "onUpdate has been triggered before the user finished configuring the widget");
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager awm = AppWidgetManager.getInstance(context);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    if (intent.getExtras() != null) {
                        updateAppWidget(context, awm, new int[]{intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)});
                    } else {
                        Log.w(TAG, "intent.getExtras() is null");
                    }
                } else {
                    updateAppWidget(context, awm, awm.getAppWidgetIds(new ComponentName(context, NoteListWidget.class)));
                }
            }
        } else {
            Log.w(TAG, "intent.getAction() is null");
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        final NotesDatabase db = NotesDatabase.getInstance(context);

        for (int appWidgetId : appWidgetIds) {
            db.removeNoteListWidget(appWidgetId);
        }
    }

    private static String getWidgetTitle(Context context, int displayMode, String category) {
        switch (displayMode) {
            case MODE_DISPLAY_ALL:
                return context.getString(R.string.app_name);
            case MODE_DISPLAY_STARRED:
                return context.getString(R.string.label_favorites);
            case MODE_DISPLAY_CATEGORY:
                if ("".equals(category)) {
                    return context.getString(R.string.action_uncategorized);
                } else {
                    return category;
                }
            default:
                return null;
        }
    }

    /**
     * Update note list widgets, if the note data was changed.
     */
    public static void updateNoteListWidgets(Context context) {
        context.sendBroadcast(new Intent(context, NoteListWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
