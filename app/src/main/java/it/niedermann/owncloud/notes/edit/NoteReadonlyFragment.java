package it.niedermann.owncloud.notes.edit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentNotePreviewBinding;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.util.MarkDownUtil;
import it.niedermann.owncloud.notes.shared.util.NoteLinksUtils;

import static androidx.core.view.ViewCompat.isAttachedToWindow;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.searchAndColor;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.parseCompat;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

public class NoteReadonlyFragment extends SearchableBaseNoteFragment {

    private MarkdownProcessor markdownProcessor;

    private FragmentNotePreviewBinding binding;

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
        if (menu.findItem(MENU_ID_PIN) != null)
            menu.findItem(MENU_ID_PIN).setVisible(false);
    }

    @Override
    public ScrollView getScrollView() {
        return binding.scrollView;
    }

    @Override
    protected FloatingActionButton getSearchNextButton() {
        return binding.searchNext;
    }

    @Override
    protected FloatingActionButton getSearchPrevButton() {
        return binding.searchPrev;
    }

    @Override
    protected Layout getLayout() {
        binding.singleNoteContent.onPreDraw();
        return binding.singleNoteContent.getLayout();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotePreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        markdownProcessor = new MarkdownProcessor(requireActivity());
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(
                MarkDownUtil.getMarkDownConfiguration(binding.singleNoteContent.getContext())
                        .setOnLinkClickCallback((view, link) -> {
                            if (NoteLinksUtils.isNoteLink(link)) {
                                long noteRemoteId = NoteLinksUtils.extractNoteRemoteId(link);
                                long noteLocalId = db.getLocalIdByRemoteId(this.note.getAccountId(), noteRemoteId);
                                Intent intent = new Intent(requireActivity().getApplicationContext(), EditNoteActivity.class);
                                intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, noteLocalId);
                                startActivity(intent);
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(browserIntent);
                            }
                        })
                        .build());
        try {
            binding.singleNoteContent.setText(parseCompat(markdownProcessor, note.getContent()));
            onResume();
        } catch (StringIndexOutOfBoundsException e) {
            // Workaround for RxMarkdown: https://github.com/stefan-niedermann/nextcloud-notes/issues/668
            binding.singleNoteContent.setText(note.getContent());
            Toast.makeText(binding.singleNoteContent.getContext(), R.string.could_not_load_preview_two_digit_numbered_list, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        binding.singleNoteContent.setMovementMethod(LinkMovementMethod.getInstance());

        db = NotesDatabase.getInstance(getActivity());
        binding.swiperefreshlayout.setEnabled(false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        binding.singleNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(requireContext(), sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.singleNoteContent.setTypeface(Typeface.MONOSPACE);
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
    protected void colorWithText(@NonNull String newText, @Nullable Integer current, int mainColor, int textColor) {
        if ((binding != null) && isAttachedToWindow(binding.singleNoteContent)) {
            binding.singleNoteContent.setText(searchAndColor(new SpannableString(parseCompat(markdownProcessor, getContent())), newText, requireContext(), current, mainColor, textColor), TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected String getContent() {
        return note.getContent();
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        super.applyBrand(mainColor, textColor);
        binding.singleNoteContent.setHighlightColor(getTextHighlightBackgroundColor(requireContext(), mainColor, colorPrimary, colorAccent));
    }
}
