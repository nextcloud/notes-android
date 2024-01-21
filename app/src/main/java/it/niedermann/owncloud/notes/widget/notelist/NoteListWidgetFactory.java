package it.niedermann.owncloud.notes.widget.notelist;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;

import static it.niedermann.owncloud.notes.edit.EditNoteActivity.PARAM_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_STARRED;

public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = NoteListWidgetFactory.class.getSimpleName();

    private final Context context;
    private final int appWidgetId;
    private final NotesRepository repo;
    @NonNull
    private final List<Note> dbNotes = new ArrayList<>();
    private NotesListWidgetData data;

    NoteListWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        repo = NotesRepository.getInstance(context);
    }

    @Override
    public void onCreate() {
        // Nothing to do here…
    }

    @Override
    public void onDataSetChanged() {
        dbNotes.clear();
        try {
            data = repo.getNoteListWidgetData(appWidgetId);
            Log.v(TAG, "--- data - " + data);
            switch (data.getMode()) {
                case MODE_DISPLAY_ALL ->
                        dbNotes.addAll(repo.searchRecentByModified(data.getAccountId(), "%"));
                case MODE_DISPLAY_STARRED ->
                        dbNotes.addAll(repo.searchFavoritesByModified(data.getAccountId(), "%"));
                default -> {
                    if (data.getCategory() != null) {
                        dbNotes.addAll(repo.searchCategoryByModified(data.getAccountId(), "%", data.getCategory()));
                    } else {
                        dbNotes.addAll(repo.searchUncategorizedByModified(data.getAccountId(), "%"));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        //NoOp
    }

    @Override
    public int getCount() {
        return dbNotes.size() + 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews note_content;

        if (position == 0) {
            final Account localAccount = repo.getAccountById(data.getAccountId());
            final Intent openIntent = new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
            final Intent createIntent = new Intent(context, EditNoteActivity.class);
            final Bundle extras = new Bundle();

            extras.putSerializable(PARAM_CATEGORY, data.getMode() == MODE_DISPLAY_STARRED ? new NavigationCategory(ENavigationCategoryType.FAVORITES) : new NavigationCategory(localAccount.getId(), data.getCategory()));
            extras.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, data.getAccountId());

            createIntent.putExtras(extras);
            createIntent.setData(Uri.parse(createIntent.toUri(Intent.URI_INTENT_SCHEME)));

            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry_add);
            note_content.setOnClickFillInIntent(R.id.widget_entry_content_tv, openIntent);
            note_content.setOnClickFillInIntent(R.id.widget_entry_fav_icon, createIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv, getCategoryTitle(context, data.getMode(), data.getCategory()));
            note_content.setImageViewResource(R.id.widget_entry_fav_icon, R.drawable.ic_add_blue_24dp);
            note_content.setInt(R.id.widget_entry_fav_icon, "setColorFilter", NotesColorUtil.contrastRatioIsSufficient(ContextCompat.getColor(context, R.color.widget_background), localAccount.getColor())
                    ? localAccount.getColor()
                    : ContextCompat.getColor(context, R.color.widget_foreground));
        } else {
            position--;
            if (position > dbNotes.size() - 1 || dbNotes.get(position) == null) {
                Log.e(TAG, "Could not find position \"" + position + "\" in dbNotes list.");
                return null;
            }

            final Note note = dbNotes.get(position);
            final Intent fillInIntent = new Intent(context, EditNoteActivity.class);
            final Bundle extras = new Bundle();
            extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
            extras.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.getAccountId());

            fillInIntent.putExtras(extras);
            fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));

            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv, note.getTitle());
            note_content.setImageViewResource(R.id.widget_entry_fav_icon, note.getFavorite()
                    ? R.drawable.ic_star_yellow_24dp
                    : R.drawable.ic_star_grey_ccc_24dp);
        }

        return note_content;

    }

    @NonNull
    private static String getCategoryTitle(@NonNull Context context, int displayMode, String category) {
        return switch (displayMode) {
            case MODE_DISPLAY_STARRED ->
                    context.getString(R.string.label_favorites);
            case MODE_DISPLAY_CATEGORY ->
                    "".equals(category)
                        ? context.getString(R.string.action_uncategorized)
                        : category;
            default -> context.getString(R.string.app_name);
        };
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        } else {
            position--;
            if (position > dbNotes.size() - 1 || dbNotes.get(position) == null) {
                Log.e(TAG, "Could not find position \"" + position + "\" in dbNotes list.");
                return -2;
            }
            return dbNotes.get(position).getId();
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
