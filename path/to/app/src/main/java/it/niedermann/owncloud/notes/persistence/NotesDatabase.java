import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import it.niedermann.owncloud.notes.persistence.NoteDao;

@Database(entities = {Note.class}, version = 1)
public abstract class NotesDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();

    private static NotesDatabase INSTANCE;

    public static synchronized NotesDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), NotesDatabase.class, "notes_database")
                    .build();
        }
        return INSTANCE;
    }
}