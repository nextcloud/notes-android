package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.persistence.dao.LocalAccountDao;
import it.niedermann.owncloud.notes.persistence.dao.NoteDao;
import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;
import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;

@Database(
        entities = {
                LocalAccountEntity.class,
                NoteEntity.class
        }, version = 18
)
public abstract class NotesRoomDatabase extends RoomDatabase {

    private static final String TAG = NotesRoomDatabase.class.getSimpleName();
    private static final String NOTES_DB_NAME = "OWNCLOUD_NOTES";
    //    private final NoteServerSyncHelper serverSyncHelper;
    private static NotesRoomDatabase instance;

//    private NotesRoomDatabase(Context context) {
////        serverSyncHelper = NoteServerSyncHelper.getInstance(this);
//    }

    public static NotesRoomDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static NotesRoomDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                NotesRoomDatabase.class,
                NOTES_DB_NAME)
                .addMigrations(OLD_STUFF)
                .fallbackToDestructiveMigration()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.v(TAG, NotesRoomDatabase.class.getSimpleName() + " created.");
                    }
                })
                .build();
    }

    private static final Migration OLD_STUFF = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE `Account` ADD `color` TEXT NOT NULL DEFAULT '#0082c9'");
//            database.execSQL("ALTER TABLE `Account` ADD `textColor` TEXT NOT NULL DEFAULT '#ffffff'");
//            database.execSQL("ALTER TABLE `Account` ADD `serverDeckVersion` TEXT NOT NULL DEFAULT '0.6.4'");
//            database.execSQL("ALTER TABLE `Account` ADD `maintenanceEnabled` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract NoteDao getNoteDao();

    public abstract LocalAccountDao getLocalAccountDao();
}
