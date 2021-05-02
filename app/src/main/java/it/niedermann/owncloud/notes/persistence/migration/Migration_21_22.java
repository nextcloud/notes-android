package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_21_22 extends Migration {
    @NonNull
    private final Context context;

    public Migration_21_22(@NonNull Context context) {
        super(21, 22);
        this.context = context;
    }
    /**
     * Enabling backgroundSync, set from {@link String} values to {@link Boolean} value true
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains("backgroundSync")) {
            editor.remove("backgroundSync");
        }
        editor.putBoolean("backgroundSync", true);
        editor.apply();
    }
}
