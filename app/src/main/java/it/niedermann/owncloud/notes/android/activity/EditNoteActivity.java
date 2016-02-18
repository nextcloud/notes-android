package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class EditNoteActivity extends AppCompatActivity {
    private final long DELAY = 1000; // in ms
    private EditText content = null;
    private Note note = null;
    private Timer timer = new Timer();
    private ActionBar actionBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        note = (Note) getIntent().getSerializableExtra(
                NoteActivity.EDIT_NOTE);
        content = (EditText) findViewById(R.id.editContent);
        content.setEnabled(false);
        content.setText(note.getContent());
        content.setEnabled(true);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            actionBar.setSubtitle(getString(R.string.action_edit_editing));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {
                if (timer != null)
                    timer.cancel();
            }

            @Override
            public void afterTextChanged(final Editable s) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                saveData();
                            }
                        });
                    }
                }, DELAY);
            }
        });
    }

    @Override
    public void onBackPressed() {
        content.setEnabled(false);
        saveData();
        Intent data = new Intent();
        data.setAction(Intent.ACTION_VIEW);
        data.putExtra(NoteActivity.EDIT_NOTE, note);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                content.setEnabled(false);
                saveData();
                Intent data = new Intent();
                data.setAction(Intent.ACTION_VIEW);
                data.putExtra(NoteActivity.EDIT_NOTE, note);
                setResult(RESULT_OK, data);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setSubtitle(getResources().getString(R.string.action_edit_saving));
        }
        // #74
        note.setModified(Calendar.getInstance());
        note.setContent(((EditText) findViewById(R.id.editContent)).getText().toString());
        // #80
        note.setTitle(NoteUtil.generateNoteTitle(note.getContent()));
        NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(this);
        db.getNoteServerSyncHelper().addCallback(new ICallback() {
            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setSubtitle(getResources().getString(R.string.action_edit_saved));
                        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getSupportActionBar().setSubtitle(null);
                                    }
                                });
                            }
                        }, 1, TimeUnit.SECONDS);
                    }
                });

                /* TODO Notify widgets

                int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SingleNoteWidget.class));

                for (int id : widgetIDs) {
                    AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.layout.widget_single_note);
                }*/
            }
        });
        db.updateNoteAndSync(note);
    }
}