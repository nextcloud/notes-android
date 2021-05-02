package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// CS304 issue link : https://github.com/stefan-niedermann/nextcloud-notes/issues/1168

public class Migration_21_22 extends Migration {
    @NonNull
    private final Context context;

    public Migration_21_22(@NonNull Context context) {
        super(21, 22);
        this.context = context;
    }

    /**
     * Enabling Set backgroundSync to every 15 minutes, and wifiOnly to true
     * @param database no use just implement
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("wifiOnly", true);
        editor.putString("backgroundSync", "15_minutes");
        editor.apply();
    }
}
