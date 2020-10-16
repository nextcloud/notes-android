package it.niedermann.owncloud.notes.edit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;

import java.util.HashSet;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentNotePreviewBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.MarkDownUtil;
import it.niedermann.owncloud.notes.shared.util.NoteLinksUtils;
import it.niedermann.owncloud.notes.shared.util.SSOUtil;
import it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor;
import it.niedermann.owncloud.notes.shared.util.text.TextProcessorChain;
import it.niedermann.owncloud.notes.shared.util.text.WwwLinksProcessor;

import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.searchAndColor;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_CHECKED_MINUS;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_CHECKED_STAR;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_UNCHECKED_MINUS;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_UNCHECKED_STAR;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.parseCompat;
import static it.niedermann.owncloud.notes.shared.util.NoteLinksUtils.extractNoteRemoteId;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

public class NotePreviewFragment extends SearchableBaseNoteFragment implements OnRefreshListener {

    private String changedText;

    private MarkdownProcessor markdownProcessor;

    private FragmentNotePreviewBinding binding;

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
        markdownProcessor = new MarkdownProcessor(requireContext());
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(
                MarkDownUtil.getMarkDownConfiguration(binding.singleNoteContent.getContext())
                        .setOnTodoClickCallback((view, line, lineNumber) -> {
                                    try {
                                        String[] lines = TextUtils.split(note.getContent(), "\\r?\\n");
                                        /*
                                         * Workaround for RxMarkdown-bug:
                                         * When (un)checking a checkbox in a note which contains code-blocks, the "`"-characters get stripped out in the TextView and therefore the given lineNumber is wrong
                                         * Find number of lines starting with ``` before lineNumber
                                         */
                                        boolean inCodefence = false;
                                        for (int i = 0; i < lines.length; i++) {
                                            if (lines[i].startsWith("```")) {
                                                inCodefence = !inCodefence;
                                                lineNumber++;
                                            }
                                            if (inCodefence && TextUtils.isEmpty(lines[i])) {
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
                                        if (lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_MINUS) || lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_STAR)) {
                                            lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_MINUS, CHECKBOX_CHECKED_MINUS);
                                            lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_STAR, CHECKBOX_CHECKED_STAR);
                                        } else {
                                            lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_MINUS, CHECKBOX_UNCHECKED_MINUS);
                                            lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_STAR, CHECKBOX_UNCHECKED_STAR);
                                        }

                                        changedText = TextUtils.join("\n", lines);
                                        binding.singleNoteContent.setText(parseCompat(markdownProcessor, changedText));
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
                                final Intent intent = new Intent(requireActivity().getApplicationContext(), EditNoteActivity.class)
                                        .putExtra(EditNoteActivity.PARAM_NOTE_ID, db.getNoteDao().getLocalIdByRemoteId(this.note.getAccountId(), extractNoteRemoteId(link)));
                                startActivity(intent);
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(browserIntent);
                            }
                        })
                        .build());

        TextProcessorChain chain = defaultTextProcessorChain(note.getNote());
        try {
            binding.singleNoteContent.setText(parseCompat(markdownProcessor, chain.apply(note.getContent())));
        } catch (StringIndexOutOfBoundsException e) {
            // Workaround for RxMarkdown: https://github.com/stefan-niedermann/nextcloud-notes/issues/668
            binding.singleNoteContent.setText(chain.apply(note.getContent()));
            Toast.makeText(binding.singleNoteContent.getContext(), R.string.could_not_load_preview_two_digit_numbered_list, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        changedText = note.getContent();
        binding.singleNoteContent.setMovementMethod(LinkMovementMethod.getInstance());

        binding.swiperefreshlayout.setOnRefreshListener(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        binding.singleNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(requireContext(), sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.singleNoteContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    @Override
    protected void colorWithText(@NonNull String newText, @Nullable Integer current, int mainColor, int textColor) {
        if (binding != null && ViewCompat.isAttachedToWindow(binding.singleNoteContent)) {
            binding.singleNoteContent.setText(
                    searchAndColor(new SpannableString(parseCompat(markdownProcessor, getContent())), newText, requireContext(), current, mainColor, textColor),
                    TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    protected String getContent() {
        return changedText;
    }

    @Override
    public void onRefresh() {
        if (db.getNoteServerSyncHelper().isSyncPossible() && SSOUtil.isConfigured(getContext())) {
            binding.swiperefreshlayout.setRefreshing(true);
            try {
                TextProcessorChain chain = defaultTextProcessorChain(note.getNote());
                Account account = db.getAccountDao().getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext()).name);
                db.getNoteServerSyncHelper().addCallbackPull(account, () -> {
                    note = db.getNoteDao().getNoteWithCategory(note.getAccountId(), note.getId());
                    changedText = note.getContent();
                    binding.singleNoteContent.setText(parseCompat(markdownProcessor, chain.apply(note.getContent())));
                    binding.swiperefreshlayout.setRefreshing(false);
                });
                db.getNoteServerSyncHelper().scheduleSync(account, false);
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                e.printStackTrace();
            }
        } else {
            binding.swiperefreshlayout.setRefreshing(false);
            Toast.makeText(requireContext(), getString(R.string.error_sync, getString(R.string.error_no_network)), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        super.applyBrand(mainColor, textColor);
        binding.singleNoteContent.setHighlightColor(getTextHighlightBackgroundColor(requireContext(), mainColor, colorPrimary, colorAccent));
    }

    private TextProcessorChain defaultTextProcessorChain(Note note) {
        TextProcessorChain chain = new TextProcessorChain();
        chain.add(new NoteLinksProcessor(new HashSet<>(db.getNoteDao().getRemoteIds(note.getAccountId()))));
        chain.add(new WwwLinksProcessor());
        return chain;
    }
}
