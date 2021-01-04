package it.niedermann.owncloud.notes.edit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentNotePreviewBinding;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.util.NoteLinksUtils;
import it.niedermann.owncloud.notes.shared.util.SSOUtil;
import it.niedermann.owncloud.notes.shared.util.text.NoteLinksProcessor;
import it.niedermann.owncloud.notes.shared.util.text.TextProcessorChain;

import static androidx.core.view.ViewCompat.isAttachedToWindow;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

public class NotePreviewFragment extends SearchableBaseNoteFragment implements OnRefreshListener {

    private String changedText;

    protected FragmentNotePreviewBinding binding;

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
        binding.singleNoteContent.registerOnLinkClickCallback((link) -> {
            if (NoteLinksUtils.isNoteLink(link)) {
                final long noteRemoteId = NoteLinksUtils.extractNoteRemoteId(link);
                final long noteLocalId = db.getLocalIdByRemoteId(this.note.getAccountId(), noteRemoteId);
                final Intent intent = new Intent(requireActivity().getApplicationContext(), EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, noteLocalId);
                startActivity(intent);
                return true;
            }
            return false;
        });
        final TextProcessorChain chain = defaultTextProcessorChain(note);
        binding.singleNoteContent.setMarkdownString(chain.apply(note.getContent()));
        binding.singleNoteContent.setMovementMethod(LinkMovementMethod.getInstance());
        changedText = note.getContent();

        db = NotesDatabase.getInstance(requireContext());
        binding.swiperefreshlayout.setOnRefreshListener(this);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        binding.singleNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(requireContext(), sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.singleNoteContent.setTypeface(Typeface.MONOSPACE);
        }

        binding.singleNoteContent.getMarkdownString().observe(requireActivity(), (newContent) -> {
            changedText = newContent.toString();
            saveNote(null);
        });
    }

    @Override
    protected void colorWithText(@NonNull String newText, @Nullable Integer current, int mainColor, int textColor) {
        if (binding != null && isAttachedToWindow(binding.singleNoteContent)) {
            binding.singleNoteContent.clearFocus();
            binding.singleNoteContent.setSearchText(newText, current);
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
                TextProcessorChain chain = defaultTextProcessorChain(note);
                SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext());
                db.getNoteServerSyncHelper().addCallbackPull(ssoAccount, () -> {
                    note = db.getNote(note.getAccountId(), note.getId());
                    changedText = note.getContent();
                    binding.singleNoteContent.setMarkdownString(chain.apply(note.getContent()));
                    binding.swiperefreshlayout.setRefreshing(false);
                });
                db.getNoteServerSyncHelper().scheduleSync(ssoAccount, false);
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
        binding.singleNoteContent.setSearchColor(mainColor);
        binding.singleNoteContent.setHighlightColor(getTextHighlightBackgroundColor(requireContext(), mainColor, colorPrimary, colorAccent));
    }

    private TextProcessorChain defaultTextProcessorChain(DBNote note) {
        TextProcessorChain chain = new TextProcessorChain();
        chain.add(new NoteLinksProcessor(db.getRemoteIds(note.getAccountId())));
        return chain;
    }

    public static BaseNoteFragment newInstance(long accountId, long noteId) {
        final BaseNoteFragment fragment = new NotePreviewFragment();
        final Bundle args = new Bundle();
        args.putLong(PARAM_NOTE_ID, noteId);
        args.putLong(PARAM_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }
}
