package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_19_20 extends Migration {

    @NonNull
    private final Context context;

    /**
     * Removes <code>branding</code> from {@link SharedPreferences} because we do no longer allow to disable it.
     *
     * @param context {@link Context}
     */
    public Migration_19_20(@NonNull Context context) {
        super(19, 20);
        this.context = context;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("branding").apply();
    }
}