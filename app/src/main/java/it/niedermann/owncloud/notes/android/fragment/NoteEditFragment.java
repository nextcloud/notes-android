package it.niedermann.owncloud.notes.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.syntax.edit.EditFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;

public class NoteEditFragment extends Fragment implements NoteFragmentI {

    public static final String PARAM_NOTE = "note";

    private static final String LOG_TAG = "NoteEditFragment";
    private static final String LOG_TAG_AUTOSAVE = "AutoSave";

    private static final long DELAY = 2000; // Wait for this time after typing before saving
    private static final long DELAY_AFTER_SYNC = 5000; // Wait for this time after saving before checking for next save
    private static final long DELAY_SHOW_SAVED = 1000; // How long "saved" is shown

    private DBNote note;
    private NoteSQLiteOpenHelper db;
    private Handler handler;
    private boolean saveActive, unsavedEdit;

    public static NoteEditFragment newInstance(DBNote note) {
        NoteEditFragment f = new NoteEditFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_NOTE, note);
        f.setArguments(b);
        return f;
    }

    public DBNote getNote() {
        cancelTimers();
        if (!getContent().equals(note.getContent()))
            saveData(null);
        return note;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(false);
        menu.findItem(R.id.menu_preview).setVisible(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        note = (DBNote) getArguments().getSerializable(PARAM_NOTE);
        db = NoteSQLiteOpenHelper.getInstance(getActivity());

        final RxMDEditText content = getContentView();
        content.setText(note.getContent());
        content.setEnabled(true);

        RxMarkdown.live(content)
                .config(MarkDownUtil.getMarkDownConfiguration(getActivity().getApplicationContext()))
                .factory(EditFactory.create())
                .intoObservable()
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        content.setText(charSequence, TextView.BufferType.SPANNABLE);
                    }
                });
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(final Editable s) {
            unsavedEdit = true;
            if(!saveActive) {
                handler.removeCallbacks(runAutoSave);
                handler.postDelayed(runAutoSave, DELAY);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getContentView().addTextChangedListener(textWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentView().removeTextChangedListener(textWatcher);
        cancelTimers();
    }

    private final Runnable runAutoSave = new Runnable() {
        @Override
        public void run() {
            if(unsavedEdit) {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: start AutoSave");
                autoSave();
            } else {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: nothing changed");
            }
        }
    };

    private final Runnable runResetActionBar = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getActionBar();
            if(actionBar == null) {
                Log.w(LOG_TAG_AUTOSAVE, "runResetActionBar: ActionBar NOT AVAILABLE!");
                return;
            }
            Log.d(LOG_TAG_AUTOSAVE, "runResetActionBar: reset action bar");
            actionBar.setSubtitle(getString(R.string.action_edit_editing));
        }
    };

    private void cancelTimers() {
        handler.removeCallbacks(runAutoSave);
        handler.removeCallbacks(runResetActionBar);
    }

    private RxMDEditText getContentView() {
        return (RxMDEditText) getActivity().findViewById(R.id.editContent);
    }

    /**
     * Gets the current content of the EditText field in the UI.
     *
     * @return String of the current content.
     */
    private String getContent() {
        return getContentView().getText().toString();
    }

    private ActionBar getActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if(activity == null) {
            return null;
        }
        return activity.getSupportActionBar();
    }

    /**
     * Saves the current changes and show the status in the ActionBar
     */
    private void autoSave() {
        Log.d(LOG_TAG_AUTOSAVE, "STARTAUTOSAVE");
        ActionBar actionBar = getActionBar();
        // if fragment is not attached to activity, then there is nothing to save
        if (getActivity() == null) {
            Log.w(LOG_TAG_AUTOSAVE, "autoSave: Activity NOT AVAILABLE!");
            return;
        }
        saveActive = true;
        if (actionBar != null) {
            actionBar.setSubtitle(getString(R.string.action_edit_saving));
        }
        saveData(new ICallback() {
            @Override
            public void onFinish() {
                onSaved();
            }

            @Override
            public void onScheduled() {
                onSaved();
            }

            private void onSaved() {
                // AFTER SYNCHRONIZATION
                Log.d(LOG_TAG_AUTOSAVE, "FINISHED AUTOSAVE");
                saveActive = false;
                ActionBar actionBar = getActionBar();
                if (actionBar == null) {
                    Log.w(LOG_TAG_AUTOSAVE, "autoSave/onSaved: ActionBar NOT AVAILABLE!");
                    return;
                }
                actionBar.setTitle(note.getTitle());
                actionBar.setSubtitle(getResources().getString(R.string.action_edit_saved));

                // AFTER "DELAY_SHOW_SAVED": set ActionBar to default title
                handler.postDelayed(runResetActionBar, DELAY_SHOW_SAVED);

                // AFTER "DELAY_AFTER_SYNC" SECONDS: allow next auto-save or start it directly
                handler.postDelayed(runAutoSave, DELAY_AFTER_SYNC);

            }
        });
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    private void saveData(ICallback callback) {
        Log.d(LOG_TAG, "saveData()");
        note = db.updateNoteAndSync(note, getContent(), callback);
        unsavedEdit = false;
    }
}
