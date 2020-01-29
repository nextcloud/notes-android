package it.niedermann.owncloud.notes.android.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.SpannableString;
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
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.DisplayUtils;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import it.niedermann.owncloud.notes.util.NoteLinksUtils;

public class NoteReadonlyFragment extends SearchableBaseNoteFragment {

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

    public static NoteReadonlyFragment newInstance(String content) {
        NoteReadonlyFragment f = new NoteReadonlyFragment();
        Bundle b = new Bundle();
        b.putString(PARAM_CONTENT, content);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_favorite).setVisible(false);
        menu.findItem(R.id.menu_edit).setVisible(false);
        menu.findItem(R.id.menu_preview).setVisible(false);
        menu.findItem(R.id.menu_cancel).setVisible(false);
        menu.findItem(R.id.menu_delete).setVisible(false);
        menu.findItem(R.id.menu_share).setVisible(false);
        menu.findItem(R.id.menu_move).setVisible(false);
        menu.findItem(R.id.menu_category).setVisible(false);
        if(menu.findItem(MENU_ID_PIN) != null)
        menu.findItem(MENU_ID_PIN).setVisible(false);
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
            noteContent.setText(markdownProcessor.parse(note.getContent()));
            onResume();
        } catch (StringIndexOutOfBoundsException e) {
            // Workaround for RxMarkdown: https://github.com/stefan-niedermann/nextcloud-notes/issues/668
            Toast.makeText(noteContent.getContext(), R.string.could_not_load_preview_two_digit_numbered_list, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        noteContent.setMovementMethod(LinkMovementMethod.getInstance());

        db = NoteSQLiteOpenHelper.getInstance(getActivity());
        swipeRefreshLayout.setEnabled(false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()).getApplicationContext());
        noteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            noteContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    @Override
    public void onCloseNote() {
        // Do nothing
    }

    @Override
    protected void saveNote(@Nullable ISyncCallback callback) {
        // Do nothing
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
        return note.getContent();
    }
}
