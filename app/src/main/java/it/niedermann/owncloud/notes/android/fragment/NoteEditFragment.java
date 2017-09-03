package it.niedermann.owncloud.notes.android.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.EditFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;

public class NoteEditFragment extends Fragment implements NoteFragmentI, CategoryDialogFragment.CategoryDialogListener {

    public static final String PARAM_NOTE = "note";

    private static final String LOG_TAG = "NoteEditFragment";
    private static final long DELAY = 2000; // in ms
    private static final long DELAY_AFTER_SYNC = 5000; // in ms

    private DBNote note;
    private Timer timer, timerNextSync;
    private boolean saveActive = false;
    private NoteSQLiteOpenHelper db;

    public static NoteEditFragment newInstance(DBNote note) {
        NoteEditFragment f = new NoteEditFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_NOTE, note);
        f.setArguments(b);
        return f;
    }

    public DBNote getNote() {
        saveData(null);
        return note;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem itemPreview = menu.findItem(R.id.menu_preview);
        itemPreview.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_eye_white_24dp));
        MenuItem itemCategory = menu.findItem(R.id.menu_category);
        itemCategory.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_category:
                showCategorySelector();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens a dialog in order to chose a category
     */
    private void showCategorySelector() {
        final String fragmentId = "fragment_category";
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag(fragmentId);
        if(frag!=null) {
            manager.beginTransaction().remove(frag).commit();
        }
        Bundle arguments = new Bundle();
        arguments.putString(CategoryDialogFragment.PARAM_CATEGORY, note.getCategory());
        CategoryDialogFragment categoryFragment = new CategoryDialogFragment();
        categoryFragment.setTargetFragment(this, 1);
        categoryFragment.setArguments(arguments);
        categoryFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        note.setCategory(category);
        autoSave();
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

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if(timer != null) {
                    timer.cancel();
                }
                if(!saveActive) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(getActivity() != null)
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        autoSave();
                                    }
                                });
                        }
                    }, DELAY);
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerNextSync != null) {
            timerNextSync.cancel();
            timerNextSync = null;
        }
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
        return ((EditText) getContentView()).getText().toString();
    }

    /**
     * Saves the current changes and show the status in the ActionBar
     */
    private void autoSave() {
        Log.d(LOG_TAG, "START save+sync");
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        saveActive = true;
        if (actionBar != null) {
            actionBar.setSubtitle(getString(R.string.action_edit_saving));
        }
        final String content = getContent();
        saveData(new ICallback() {
            @Override
            public void onFinish() {
                onSaved();
            }

            @Override
            public void onScheduled() {
                onSaved();
            }

            public void onSaved() {
                // AFTER SYNCHRONIZATION
                Log.d(LOG_TAG, "...sync finished");
                if (getActivity() != null && actionBar != null) {
                    actionBar.setTitle(note.getTitle());
                    actionBar.setSubtitle(getResources().getString(R.string.action_edit_saved));
                    Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // AFTER 1 SECOND: set ActionBar to default title
                                        if (getActivity() != null && actionBar != null)
                                            actionBar.setSubtitle(getString(R.string.action_edit_editing));
                                    }
                                });
                            }
                        }
                    }, 1, TimeUnit.SECONDS);

                    timerNextSync = new Timer();
                    timerNextSync.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // AFTER "DELAY_AFTER_SYNC" SECONDS: allow next auto-save or start it directly
                                        if (getContent().equals(content)) {
                                            saveActive = false;
                                            Log.d(LOG_TAG, "FINISH, no new changes");
                                        } else {
                                            Log.d(LOG_TAG, "content has changed meanwhile -> restart save");
                                            autoSave();
                                        }
                                    }
                                });
                            }
                        }
                    }, DELAY_AFTER_SYNC);
                }

            }
        });
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback
     */
    private void saveData(ICallback callback) {
        Log.d(LOG_TAG, "saveData()");
        note = db.updateNoteAndSync(note, getContent(), callback);
    }
}
