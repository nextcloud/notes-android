package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.R;

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
        final Cursor cursor = database.query("SELECT id FROM ACCOUNT", null);
        final int COLUMN_POSITION_ID = cursor.getColumnIndex("id");

        while (cursor.moveToNext()) {
            long accountId = cursor.getLong(COLUMN_POSITION_ID);
            resetSharedPreferences(sharedPreferences, editor, context.getString(R.string.meta_category_uncategorized), accountId);
            resetSharedPreferences(sharedPreferences, editor, context.getString(R.string.meta_category_favorites), accountId);
            resetSharedPreferences(sharedPreferences, editor, context.getString(R.string.meta_category_all_notes), accountId);
        }

        final String categorySorting = context.getString(R.string.category_sorting);

        editor.remove(categorySorting + ' ' + context.getString(R.string.meta_category_uncategorized));
        editor.remove(categorySorting + ' ' + context.getString(R.string.meta_category_favorites));
        editor.remove(categorySorting + ' ' + context.getString(R.string.meta_category_all_notes));
        editor.apply();
        cursor.close();
    }

    private void resetSharedPreferences(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, String label, long accountId) {
        final String categorySorting = context.getString(R.string.category_sorting);
        final String key = categorySorting + ' ' + label;
        if (sharedPreferences.contains(key)) {
            int sortingMethod = sharedPreferences.getInt(key, 0);
            editor.putInt(key + accountId, sortingMethod);
        }
    }
}
