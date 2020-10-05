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
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.util.ColorUtil;

@Database(
        entities = {
                LocalAccountEntity.class,
                NoteEntity.class
        }, version = 18
)
public abstract class NotesRoomDatabase extends RoomDatabase {

    private static final String TAG = NotesRoomDatabase.class.getSimpleName();
    private static final String NOTES_DB_NAME = "OWNCLOUD_NOTES";
    private static NotesRoomDatabase instance;

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
                .allowMainThreadQueries() // FIXME remove
                .build();
    }

    private static final Migration OLD_STUFF = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };

    public abstract NoteDao getNoteDao();

    public abstract LocalAccountDao getLocalAccountDao();

    @SuppressWarnings("UnusedReturnValue")
    public long addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        final LocalAccountEntity entity = new LocalAccountEntity();
        entity.setUrl(url);
        entity.setUsername(username);
        entity.setAccountName(accountName);
        entity.setCapabilities(capabilities);
        return getLocalAccountDao().insert(entity);
    }

    public void updateBrand(long accountId, @NonNull Capabilities capabilities) throws IllegalArgumentException {
        validateAccountId(accountId);

        String color;
        try {
            color = ColorUtil.formatColorToParsableHexString(capabilities.getColor()).substring(1);
        } catch (Exception e) {
            color = "0082C9";
        }

        String textColor;
        try {
            textColor = ColorUtil.formatColorToParsableHexString(capabilities.getTextColor()).substring(1);
        } catch (Exception e) {
            textColor = "FFFFFF";
        }

        getLocalAccountDao().updateBrand(accountId, color, textColor);
    }

    private static void validateAccountId(long accountId) {
        if (accountId < 1) {
            throw new IllegalArgumentException("accountId must be greater than 0");
        }
    }
}
