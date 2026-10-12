import androidx.room.Dao;
import androidx.room.Query;
import it.niedermann.owncloud.notes.persistence.Note;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes")
    List<Note> getNotes();

    @Query("SELECT content FROM notes WHERE id = :id")
    String getNoteContent(int id);
}