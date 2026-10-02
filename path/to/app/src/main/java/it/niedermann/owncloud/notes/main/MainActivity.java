import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

public class MainActivity extends AppCompatActivity {
    private NotesDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = NotesDatabase.getInstance(this);

        try {
            // Load note content from database
            String noteContent = database.getNoteContent();
        } catch (RedisException e) {
            Log.e("MainActivity", "RedisException occurred", e);
            // Prevent note content from being replaced with error message
            String noteContent = "Note content not available";
        }
    }
}