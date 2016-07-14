package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class EditNoteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "EditNote/SAVE";
    private final long DELAY = 2000; // in ms
    private final long DELAY_AFTER_SYNC = 5000; // in ms
    private EditText content = null;
    private DBNote note = null;
    private Timer timer, timerNextSync;
    private boolean saveActive = false;
    private ActionBar actionBar;
    private NoteSQLiteOpenHelper db;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        note = (DBNote) getIntent().getSerializableExtra(
                NoteActivity.EDIT_NOTE);
        content = (EditText) findViewById(R.id.editContent);
        content.setEnabled(false);
        content.setText(note.getContent());
        content.setEnabled(true);
        db = new NoteSQLiteOpenHelper(this);
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
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if(db.getNoteServerSyncHelper().isSyncPossible()) {
                    if(timer != null) {
                        timer.cancel();
                    }
                    if(!saveActive) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        autoSave();
                                    }
                                });
                            }
                        }, DELAY);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        saveAndClose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveAndClose();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves all changes and closes the Activity
     */
    private void saveAndClose() {
        content.setEnabled(false);
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
        if(timerNextSync!=null) {
            timerNextSync.cancel();
            timerNextSync = null;
        }
        saveData(null);
        Intent data = new Intent();
        data.setAction(Intent.ACTION_VIEW);
        data.putExtra(NoteActivity.EDIT_NOTE, note);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Gets the current content of the EditText field in the UI.
     * @return String of the current content.
     */
    private String getContent() {
        return ((EditText) findViewById(R.id.editContent)).getText().toString();
    }

    /**
     * Saves the current changes and show the status in the ActionBar
     */
    private void autoSave() {
        Log.d(LOG_TAG, "START save+sync");
        saveActive = true;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setSubtitle(getString(R.string.action_edit_saving));
        }
        // #74
        note.setModified(Calendar.getInstance());
        note.setContent(getContent());
        // #80
        note.setTitle(NoteUtil.generateNoteTitle(note.getContent()));
        final String content = note.getContent();
        saveData(new ICallback() {
            @Override
            public void onFinish() {
                // AFTER SYNCHRONIZATION
                Log.d(LOG_TAG, "...sync finished");
                getSupportActionBar().setSubtitle(getResources().getString(R.string.action_edit_saved));
                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // AFTER 1 SECOND: set ActionBar to default title
                                getSupportActionBar().setSubtitle(getString(R.string.action_edit_editing));
                            }
                        });
                    }
                }, 1, TimeUnit.SECONDS);

                timerNextSync = new Timer();
                timerNextSync.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // AFTER "DELAY_AFTER_SYNC" SECONDS: allow next auto-save or start it directly
                                if(getContent().equals(content)) {
                                    saveActive = false;
                                    Log.d(LOG_TAG, "FINISH, no new changes");
                                } else {
                                    Log.d(LOG_TAG, "content has changed meanwhile -> restart save");
                                    autoSave();
                                }
                            }
                        });
                    }
                }, DELAY_AFTER_SYNC);

                /* TODO Notify widgets

                int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SingleNoteWidget.class));

                for (int id : widgetIDs) {
                    AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.layout.widget_single_note);
                }*/
            }
        });
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     * @param callback
     */
    private void saveData(ICallback callback) {
        db.updateNoteAndSync(note, callback);
    }
}