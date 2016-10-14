package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;

public class NoteActivity extends AppCompatActivity {

    public static final String PARAM_NOTE = EditNoteActivity.PARAM_NOTE;

    private DBNote note = null;
    private TextView noteContent = null;
    private ActionBar actionBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);
        note = (DBNote) getIntent().getSerializableExtra(PARAM_NOTE);
        if (savedInstanceState != null) {
            note = (DBNote) savedInstanceState.getSerializable(PARAM_NOTE);
        }
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            actionBar.setSubtitle(DateUtils.getRelativeDateTimeString(getApplicationContext(), note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
        }
        noteContent = (TextView) findViewById(R.id.single_note_content);
        noteContent.setText(note.getSpannableContent());
        findViewById(R.id.fab_edit).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.single_note_content)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM_NOTE, note);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}