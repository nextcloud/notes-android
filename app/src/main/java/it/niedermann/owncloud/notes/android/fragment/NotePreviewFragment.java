package it.niedermann.owncloud.notes.android.fragment;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.rxmarkdown.RxMDTextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LoginStatus;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;

public class NotePreviewFragment extends BaseNoteFragment {

    private static final String TAG = NotePreviewFragment.class.getSimpleName();

    private NoteSQLiteOpenHelper db = null;

    private String changedText;

    MarkdownProcessor markdownProcessor;

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.single_note_content)
    RxMDTextView noteContent;

    public static NotePreviewFragment newInstance(long accountId, long noteId) {
        NotePreviewFragment f = new NotePreviewFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_NOTE_ID, noteId);
        b.putLong(PARAM_ACCOUNT_ID, accountId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(true);
        menu.findItem(R.id.menu_preview).setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_single_note, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, Objects.requireNonNull(getView()));
        markdownProcessor = new MarkdownProcessor(getActivity());
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(
                MarkDownUtil.getMarkDownConfiguration(noteContent.getContext())
//                        .setOnTodoClickCallback((view, line, lineNumber) -> {
//                                    String[] lines = TextUtils.split(note.getContent(), "\\r?\\n");
//                                    /*
//                                     * Workaround for a bug when checkbox is the last line:
//                                     * When (un)checking a checkbox which is in the last line, every time it gets toggled, the last character of the line gets lost.
//                                     */
//                                    if ((lines.length - 1) == lineNumber) {
//                                        if(lines[lineNumber].contains("- [ ]")) {
//                                            lines[lineNumber] = lines[lineNumber].replace("- [ ]", "- [x]");
//                                        } else {
//                                            lines[lineNumber] = lines[lineNumber].replace("- [x]", "- [ ]");
//                                        }
//
//                                    } else if (lines.length >= lineNumber) {
//                                        lines[lineNumber] = line;
//                                    }
//                                    changedText = TextUtils.join("\n", lines);
//                                    noteContent.setText(markdownProcessor.parse(changedText));
//                                    saveNote(null);
//                                    return line;
//                                }
//                        )
                        .build());
        setActiveTextView(noteContent);
        noteContent.setText(markdownProcessor.parse(note.getContent()));
        changedText = note.getContent();
        noteContent.setMovementMethod(LinkMovementMethod.getInstance());

        db = NoteSQLiteOpenHelper.getInstance(getActivity().getApplicationContext());
        // Pull to Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (db.getNoteServerSyncHelper().isSyncPossible()) {
                swipeRefreshLayout.setRefreshing(true);
                db.getNoteServerSyncHelper().addCallbackPull(new ICallback() {
                    @Override
                    public void onFinish() {
                        noteContent.setText(markdownProcessor.parse(db.getNote(note.getAccountId(), note.getId()).getContent()));
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onScheduled() {
                    }
                });
                db.getNoteServerSyncHelper().scheduleSync(false);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_sync, getString(LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        noteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            noteContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    @Override
    protected String getContent() {
        return changedText;
    }
}
