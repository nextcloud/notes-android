package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {
    // Intent backToListViewIntent = null;
    public final static String EDIT_NOTE = "it.niedermann.owncloud.notes.edit_note_id";
    public final static int EDIT_NOTE_CMD = 1;
    private Note note = null;
    private int notePosition = 0;
    private TextView noteContent = null;
    private ActionBar actionBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // backToListViewIntent = new Intent();
        setContentView(R.layout.activity_single_note);
        note = (Note) getIntent().getSerializableExtra(
                NotesListViewActivity.SELECTED_NOTE);
        notePosition = getIntent().getIntExtra(
                NotesListViewActivity.SELECTED_NOTE_POSITION, 0);
        findViewById(R.id.fab_edit).setOnClickListener(this);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            actionBar.setSubtitle(note.getModified("dd.MM.yyyy HH:mm"));
        }
        noteContent = (TextView) findViewById(R.id.singleNoteContent);
        noteContent.setText(note.getSpannableContent());
    }

    @Override
    public void onClick(View v) {
        Intent editIntent = new Intent(this, EditNoteActivity.class);
        editIntent.putExtra(EDIT_NOTE, note);
        startActivityForResult(editIntent, EDIT_NOTE_CMD);
    }

    /**
     * Main-Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list_view, menu);
        return true;
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        NoteSQLiteOpenHelper db;
        switch (id) {
            case R.id.menu_delete:
                //setContentView(R.layout.activity_notes_list_view);
                db = new NoteSQLiteOpenHelper(this);
                db.deleteNoteAndSync(note.getId());
                finish();
                return true;
            case R.id.menu_share:
                Log.v("Note", "Share Action pressed.");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        note.getTitle());
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        note.getContent());
                startActivity(shareIntent);
                return true;
            case R.id.menu_copy:
                Log.v("Note", "Copy Action pressed.");
                db = new NoteSQLiteOpenHelper(this);
                Note newNote = db.getNote(db.addNoteAndSync(note.getContent()));
                newNote.setTitle(note.getTitle() + " (" + getResources().getString(R.string.copy) + ")");
                db.updateNote(newNote);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == EDIT_NOTE_CMD) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Note editedNote = (Note) data.getExtras().getSerializable(
                        EDIT_NOTE);
                if (editedNote != null) {
                    noteContent.setText(editedNote.getSpannableContent());
                    actionBar.setTitle(editedNote.getTitle());
                    actionBar.setSubtitle(editedNote.getModified("dd.MM.yyyy HH:mm"));
                }
                // TODO Fire changed note to noteslistviewactivity
                data.putExtra(NotesListViewActivity.SELECTED_NOTE_POSITION,
                        notePosition);
                setResult(RESULT_OK, data);
            }
        }
    }
}