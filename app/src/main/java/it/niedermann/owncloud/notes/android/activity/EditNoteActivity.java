package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class EditNoteActivity extends AppCompatActivity {
	private EditText content = null;
	private Note note = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        note = (Note) getIntent().getSerializableExtra(
                NoteActivity.EDIT_NOTE);
        content = (EditText) findViewById(R.id.editContent);
        content.setEnabled(false);
        content.setText(note.getContent());
        content.setSelection(note.getContent().length());
        content.setEnabled(true);
	}

	/**
	 * Create Action Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_menu_edit, menu);
		return true;
	}

	/**
	 * Handle Action Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_edit_save:
			content.setEnabled(false);
            note.setContent(((EditText) findViewById(R.id.editContent)).getText().toString());
            NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(this);
            db.updateNoteAndSync(note);
            Intent data = new Intent();
            data.setAction(Intent.ACTION_VIEW);
            data.putExtra(NoteActivity.EDIT_NOTE, note);
            setResult(RESULT_OK, data);
            finish();
			return true;
            case R.id.action_edit_cancel:
            finish();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}