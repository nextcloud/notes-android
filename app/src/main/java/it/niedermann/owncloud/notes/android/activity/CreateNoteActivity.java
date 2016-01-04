package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText editTextField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        editTextField = (EditText) findViewById(R.id.createContent);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                editTextField.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu_create, menu);
        return true;
    }

    /**
     * Main-Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_create_save:
                editTextField.setEnabled(false);
                String content = editTextField.getText().toString();
                NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(this);
                Intent data = new Intent();
                data.putExtra(NotesListViewActivity.CREATED_NOTE, db.getNote(db.addNoteAndSync(content)));
                setResult(RESULT_OK, data);
                finish();
                return true;
            case R.id.action_create_cancel:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}