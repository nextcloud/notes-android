package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

/**
 * Add account ID to sharedPreferences and thus make meta category account aware.
 * https://github.com/stefan-niedermann/nextcloud-notes/issues/1169
 */
public class Migration_23_24 extends Migration {
    @NonNull
    private final Context context;

    public Migration_23_24(@NonNull Context context) {
        super(23, 24);
        this.context = context;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String tmpTableNotes = String.format("tmp_%s", "NOTES");
        Cursor tmpNotesCursor = database.query("SELECT * FROM " + tmpTableNotes, null);
        while (tmpNotesCursor.moveToNext()) {
            int accountId = tmpNotesCursor.getInt(2);
            Log.e("###", accountId + "");
            resetSharedPreferences(sharedPreferences, editor, R.string.action_uncategorized, accountId);
            resetSharedPreferences(sharedPreferences, editor, R.string.label_favorites, accountId);
            resetSharedPreferences(sharedPreferences, editor, R.string.label_all_notes, accountId);
        }
        editor.apply();
    }


    private void resetSharedPreferences(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, int label, int accountId) {
        if (sharedPreferences.contains(context.getString(label) + ' ' + context.getString(label))) {
            int sortingMethod = sharedPreferences.getInt(context.getString(R.string.action_sorting_method) + ' ' + context.getString(label), 0);
            editor.remove(context.getString(R.string.action_sorting_method) + ' ' + context.getString(label));
            editor.putInt(context.getString(R.string.action_sorting_method) + ' ' + context.getString(label) + accountId, sortingMethod);
        }
    }
}
