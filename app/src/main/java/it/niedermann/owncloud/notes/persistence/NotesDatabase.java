/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.persistence.dao.AccountDao;
import it.niedermann.owncloud.notes.persistence.dao.CategoryOptionsDao;
import it.niedermann.owncloud.notes.persistence.dao.NoteDao;
import it.niedermann.owncloud.notes.persistence.dao.WidgetNotesListDao;
import it.niedermann.owncloud.notes.persistence.dao.WidgetSingleNoteDao;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.CategoryOptions;
import it.niedermann.owncloud.notes.persistence.entity.Converters;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.persistence.entity.SingleNoteWidgetData;
import it.niedermann.owncloud.notes.persistence.migration.Migration_10_11;
import it.niedermann.owncloud.notes.persistence.migration.Migration_11_12;
import it.niedermann.owncloud.notes.persistence.migration.Migration_12_13;
import it.niedermann.owncloud.notes.persistence.migration.Migration_13_14;
import it.niedermann.owncloud.notes.persistence.migration.Migration_14_15;
import it.niedermann.owncloud.notes.persistence.migration.Migration_15_16;
import it.niedermann.owncloud.notes.persistence.migration.Migration_16_17;
import it.niedermann.owncloud.notes.persistence.migration.Migration_17_18;
import it.niedermann.owncloud.notes.persistence.migration.Migration_18_19;
import it.niedermann.owncloud.notes.persistence.migration.Migration_19_20;
import it.niedermann.owncloud.notes.persistence.migration.Migration_20_21;
import it.niedermann.owncloud.notes.persistence.migration.Migration_21_22;
import it.niedermann.owncloud.notes.persistence.migration.Migration_22_23;
import it.niedermann.owncloud.notes.persistence.migration.Migration_23_24;
import it.niedermann.owncloud.notes.persistence.migration.Migration_24_25;
import it.niedermann.owncloud.notes.persistence.migration.Migration_9_10;

@Database(
        entities = {
                Account.class,
                Note.class,
                CategoryOptions.class,
                SingleNoteWidgetData.class,
                NotesListWidgetData.class
        }, version = 25
)
@TypeConverters({Converters.class})
public abstract class NotesDatabase extends RoomDatabase {

    private static final String TAG = NotesDatabase.class.getSimpleName();
    private static final String NOTES_DB_NAME = "OWNCLOUD_NOTES";
    private static volatile NotesDatabase instance;

    public static NotesDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = create(context.getApplicationContext());
        }
        return instance;
    }

    private static NotesDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                NotesDatabase.class,
                NOTES_DB_NAME)
                .addMigrations(
                        new Migration_9_10(), // v2.0.0
                        new Migration_10_11(context),
                        new Migration_11_12(context),
                        new Migration_12_13(context),
                        new Migration_13_14(context),
                        new Migration_14_15(),
                        new Migration_15_16(context),
                        new Migration_16_17(),
                        new Migration_17_18(),
                        new Migration_18_19(context),
                        new Migration_19_20(context),
                        new Migration_20_21(),
                        new Migration_21_22(context),
                        new Migration_22_23(),
                        new Migration_23_24(context),
                        new Migration_24_25()
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .fallbackToDestructiveMigration()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        final String cleanUpStatement = "DELETE FROM CategoryOptions WHERE CategoryOptions.category NOT IN (SELECT Note.category FROM Note WHERE Note.accountId = CategoryOptions.accountId);";
                        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_DEL AFTER DELETE ON Note BEGIN " + cleanUpStatement + " END;");
                        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_UPD AFTER UPDATE ON Note BEGIN " + cleanUpStatement + " END;");
                        Log.v(TAG, NotesDatabase.class.getSimpleName() + " created.");
                    }
                })
                .allowMainThreadQueries() // FIXME Needed in BaseNoteFragment#saveNote()
                .build();
    }

    public abstract AccountDao getAccountDao();

    public abstract CategoryOptionsDao getCategoryOptionsDao();

    public abstract NoteDao getNoteDao();

    public abstract WidgetSingleNoteDao getWidgetSingleNoteDao();

    public abstract WidgetNotesListDao getWidgetNotesListDao();
}
