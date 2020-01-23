package it.niedermann.owncloud.notes.android.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.MarkdownTextView;
import com.yydcdut.markdown.syntax.text.TextFactory;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.LoginStatus;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.DisplayUtils;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import it.niedermann.owncloud.notes.util.NoteLinksUtils;

public class NotePreviewFragment extends SearchableBaseNoteFragment {

    private String changedText;

    private MarkdownProcessor markdownProcessor;

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.searchNext)
    FloatingActionButton searchNext;

    @BindView(R.id.searchPrev)
    FloatingActionButton searchPrev;

    @BindView(R.id.single_note_content)
    MarkdownTextView noteContent;

    public static NotePreviewFragment newInstance(long accountId, long noteId) {
        NotePreviewFragment f = new NotePreviewFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_NOTE_ID, noteId);
        b.putLong(PARAM_ACCOUNT_ID, accountId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(true);
        menu.findItem(R.id.menu_preview).setVisible(false);
    }

    @Override
    public ScrollView getScrollView() {
        return scrollView;
    }

    @Override
    protected FloatingActionButton getSearchNextButton() {
        return searchNext;
    }

    @Override
    protected FloatingActionButton getSearchPrevButton() {
        return searchPrev;
    }

    @Override
    protected Layout getLayout() {
        noteContent.onPreDraw();
        return noteContent.getLayout();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_preview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, Objects.requireNonNull(getView()));
        markdownProcessor = new MarkdownProcessor(Objects.requireNonNull(getActivity()));
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(
                MarkDownUtil.getMarkDownConfiguration(noteContent.getContext())
                        .setOnTodoClickCallback((view, line, lineNumber) -> {
                                    try {
                                        String[] lines = TextUtils.split(note.getContent(), "\\r?\\n");
                                        /*
                                         * Workaround for RxMarkdown-bug:
                                         * When (un)checking a checkbox in a note which contains code-blocks, the "`"-characters get stripped out in the TextView and therefore the given lineNumber is wrong
                                         * Find number of lines starting with ``` before lineNumber
                                         */
                                        for (int i = 0; i < lines.length; i++) {
                                            if (lines[i].startsWith("```")) {
                                                lineNumber++;
                                            }
                                            if (i == lineNumber) {
                                                break;
                                            }
                                        }

                                        /*
                                         * Workaround for multiple RxMarkdown-bugs:
                                         * When (un)checking a checkbox which is in the last line, every time it gets toggled, the last character of the line gets lost.
                                         * When (un)checking a checkbox, every markdown gets stripped in the given line argument
                                         */
                                        if (lines[lineNumber].startsWith("- [ ]") || lines[lineNumber].startsWith("* [ ]")) {
                                            lines[lineNumber] = lines[lineNumber].replace("- [ ]", "- [x]");
                                            lines[lineNumber] = lines[lineNumber].replace("* [ ]", "* [x]");
                                        } else {
                                            lines[lineNumber] = lines[lineNumber].replace("- [x]", "- [ ]");
                                            lines[lineNumber] = lines[lineNumber].replace("* [x]", "* [ ]");
                                        }

                                        changedText = TextUtils.join("\n", lines);
                                        noteContent.setText(markdownProcessor.parse(changedText));
                                        saveNote(null);
                                    } catch (IndexOutOfBoundsException e) {
                                        Toast.makeText(getActivity(), R.string.checkbox_could_not_be_toggled, Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                    return line;
                                }
                        )
                        .setOnLinkClickCallback((view, link) -> {
                            if (NoteLinksUtils.isNoteLink(link)) {
                                long noteRemoteId = NoteLinksUtils.extractNoteRemoteId(link);
                                long noteLocalId = db.getLocalIdByRemoteId(this.note.getAccountId(), noteRemoteId);
                                Intent intent = new Intent(getActivity().getApplicationContext(), EditNoteActivity.class);
                                intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, noteLocalId);
                                startActivity(intent);
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(browserIntent);
                            }
                        })
                        .build());
        try {
            noteContent.setText(markdownProcessor.parse(NoteLinksUtils.replaceNoteLinksWithDummyUrls(note.getContent(), db.getRemoteIds(note.getAccountId()))));
            onResume();
        } catch (StringIndexOutOfBoundsException e) {
            // Workaround for RxMarkdown: https://github.com/stefan-niedermann/nextcloud-notes/issues/668
            noteContent.setText(NoteLinksUtils.replaceNoteLinksWithDummyUrls(note.getContent(), db.getRemoteIds(note.getAccountId())));
            Toast.makeText(noteContent.getContext(), R.string.could_not_load_preview_two_digit_numbered_list, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        changedText = note.getContent();
        noteContent.setMovementMethod(LinkMovementMethod.getInstance());

        db = NoteSQLiteOpenHelper.getInstance(getActivity());
        // Pull to Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (db.getNoteServerSyncHelper().isSyncPossible()) {
                swipeRefreshLayout.setRefreshing(true);
                db.getNoteServerSyncHelper().addCallbackPull(new ICallback() {
                    @Override
                    public void onFinish() {
                        note = db.getNote(note.getAccountId(), note.getId());
                        noteContent.setText(markdownProcessor.parse(NoteLinksUtils.replaceNoteLinksWithDummyUrls(note.getContent(), db.getRemoteIds(note.getAccountId()))));
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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()).getApplicationContext());
        noteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            noteContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    @Override
    protected void colorWithText(String newText) {
        if (noteContent != null && ViewCompat.isAttachedToWindow(noteContent)) {
            noteContent.setText(markdownProcessor.parse(DisplayUtils.searchAndColor(getContent(), new SpannableString
                            (getContent()), newText, getResources().getColor(R.color.primary))),
                    TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected String getContent() {
        return changedText;
    }
}
