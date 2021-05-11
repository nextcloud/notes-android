package it.niedermann.owncloud.notes.persistence.migration;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.persistence.entity.Account;

/**
 * Add <code>displayName</code> property to {@link Account}.
 * <p>
 * See: <a href="https://github.com/stefan-niedermann/nextcloud-notes/issues/1079">#1079 Show DisplayName instead of uid attribute for LDAP users</a>
 */
public class Migration_22_23 extends Migration {

    public Migration_22_23() {
        super(22, 23);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE Account ADD COLUMN displayName TEXT");
    }
}
