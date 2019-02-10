package it.niedermann.owncloud.notes.android.fragment;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yydcdut.markdown.syntax.edit.EditFactory;
import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import it.niedermann.owncloud.notes.util.StyleCallback;
import rx.Subscriber;

public class NoteEditFragment extends BaseNoteFragment {

    private static final String LOG_TAG_AUTOSAVE = "AutoSave";

    private static final long DELAY = 2000; // Wait for this time after typing before saving
    private static final long DELAY_AFTER_SYNC = 5000; // Wait for this time after saving before checking for next save
    @BindView(R.id.editContent)
    RxMDEditText editContent;
    private Handler handler;
    private boolean saveActive, unsavedEdit;
    private final Runnable runAutoSave = new Runnable() {
        @Override
        public void run() {
            if (unsavedEdit) {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: start AutoSave");
                autoSave();
            } else {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: nothing changed");
            }
        }
    };
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
            if (!saveActive) {
                handler.removeCallbacks(runAutoSave);
                handler.postDelayed(runAutoSave, DELAY);
            }
        }
    };

    public static NoteEditFragment newInstance(long noteId) {
        NoteEditFragment f = new NoteEditFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_NOTE_ID, noteId);
        f.setArguments(b);
        return f;
    }

    public static NoteEditFragment newInstanceWithNewNote(CloudNote newNote) {
        NoteEditFragment f = new NoteEditFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_NEWNOTE, newNote);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if(getView() != null) {
            ButterKnife.bind(this, getView());

            setActiveTextView(editContent);

            if (note.getContent().isEmpty()) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }

            // workaround for issue yydcdut/RxMarkdown#41
            note.setContent(note.getContent().replace("\r\n", "\n"));

            editContent.setText(note.getContent());
            editContent.setEnabled(true);

            RxMarkdown.live(editContent)
                    .config(MarkDownUtil.getMarkDownConfiguration(getActivity().getApplicationContext()).build())
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
                            editContent.setText(charSequence, TextView.BufferType.SPANNABLE);
                        }
                    });

            editContent.setCustomSelectionActionModeCallback(new StyleCallback(this.editContent));
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
                editContent.setTypeface(Typeface.MONOSPACE);
            }
        } else {
            Log.e(NoteEditFragment.class.getSimpleName(), "getView() is null");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        editContent.addTextChangedListener(textWatcher);
    }

    @Override
    public void onPause() {
        super.onPause();
        editContent.removeTextChangedListener(textWatcher);
        cancelTimers();
    }

    private void cancelTimers() {
        handler.removeCallbacks(runAutoSave);
    }

    /**
     * Gets the current content of the EditText field in the UI.
     *
     * @return String of the current content.
     */
    @Override
    protected String getContent() {
        return editContent.getText().toString();
    }

    @Override
    protected void saveNote(@Nullable ICallback callback) {
        super.saveNote(callback);
        unsavedEdit = false;
    }

    /**
     * Saves the current changes and show the status in the ActionBar
     */
    private void autoSave() {
        Log.d(LOG_TAG_AUTOSAVE, "STARTAUTOSAVE");
        saveActive = true;
        saveNote(new ICallback() {
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

                // AFTER "DELAY_AFTER_SYNC" SECONDS: allow next auto-save or start it directly
                handler.postDelayed(runAutoSave, DELAY_AFTER_SYNC);

            }
        });
    }
}
