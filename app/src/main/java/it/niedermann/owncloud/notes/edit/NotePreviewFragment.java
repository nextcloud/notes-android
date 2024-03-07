/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import static androidx.core.view.ViewCompat.isAttachedToWindow;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.FragmentNotePreviewBinding;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.SSOUtil;

public class NotePreviewFragment extends SearchableBaseNoteFragment implements OnRefreshListener {

    private static final String TAG = NotePreviewFragment.class.getSimpleName();

    private String changedText;

    protected FragmentNotePreviewBinding binding;

    private boolean noteLoaded = false;

    @Nullable
    private Runnable setScrollY;

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
    protected synchronized void scrollToY(int y) {
        this.setScrollY = () -> {
            if (binding != null) {
                Log.v("SCROLL set (preview) to", y + "");
                binding.scrollView.post(() -> binding.scrollView.setScrollY(y));
            }
            setScrollY = null;
        };
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
    protected @NonNull ExtendedFloatingActionButton getDirectEditingButton() {
        return binding.directEditing;
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

        binding.swiperefreshlayout.setOnRefreshListener(this);
        registerInternalNoteLinkHandler();
        binding.singleNoteContent.setMovementMethod(LinkMovementMethod.getInstance());

        final var sp = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        binding.singleNoteContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(requireContext(), sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.singleNoteContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    @Override
    protected void onNoteLoaded(Note note) {
        super.onNoteLoaded(note);
        noteLoaded = true;
        registerInternalNoteLinkHandler();
        changedText = note.getContent();
        binding.singleNoteContent.setMarkdownString(note.getContent(), setScrollY);
        binding.singleNoteContent.getMarkdownString().observe(requireActivity(), (newContent) -> {
            changedText = newContent.toString();
            saveNote(null);
        });
    }

    protected void registerInternalNoteLinkHandler() {
        binding.singleNoteContent.registerOnLinkClickCallback((link) -> {
            try {
                final long noteLocalId = repo.getLocalIdByRemoteId(this.note.getAccountId(), Long.parseLong(link));
                Log.i(TAG, "Found note for remoteId \"" + link + "\" in account \"" + this.note.getAccountId() + "\" with localId + \"" + noteLocalId + "\". Attempt to open " + EditNoteActivity.class.getSimpleName() + " for this note.");
                startActivity(new Intent(requireActivity().getApplicationContext(), EditNoteActivity.class).putExtra(EditNoteActivity.PARAM_NOTE_ID, noteLocalId));
                return true;
            } catch (NumberFormatException e) {
                // Clicked link is not a long and therefore can't be a remote id.
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "It looks like \"" + link + "\" might be a remote id of a note, but a note with this remote id could not be found in account \"" + note.getAccountId() + "\" .", e);
            }
            return false;
        });
    }

    @Override
    protected void colorWithText(@NonNull String newText, @Nullable Integer current, @ColorInt int color) {
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
        if (noteLoaded && repo.isSyncPossible() && SSOUtil.isConfigured(getContext())) {
            binding.swiperefreshlayout.setRefreshing(true);
            executor.submit(() -> {
                try {
                    final var account = repo.getAccountByName(SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext()).name);
                    repo.addCallbackPull(account, () -> executor.submit(() -> {
                        note = repo.getNoteById(note.getId());
                        changedText = note.getContent();
                        requireActivity().runOnUiThread(() -> {
                            binding.singleNoteContent.setMarkdownString(note.getContent());
                            binding.swiperefreshlayout.setRefreshing(false);
                        });
                    }));
                    repo.scheduleSync(account, false);
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            binding.swiperefreshlayout.setRefreshing(false);
            Toast.makeText(requireContext(), getString(R.string.error_sync, getString(R.string.error_no_network)), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void applyBrand(int color) {
        super.applyBrand(color);

        final var util = BrandingUtil.of(color, requireContext());
        binding.singleNoteContent.setSearchColor(color);
        binding.singleNoteContent.setHighlightColor(util.notes.getTextHighlightBackgroundColor(requireContext(), color, colorPrimary, colorAccent));
    }

    public static BaseNoteFragment newInstance(long accountId, long noteId) {
        final var fragment = new NotePreviewFragment();
        final var args = new Bundle();
        args.putLong(PARAM_NOTE_ID, noteId);
        args.putLong(PARAM_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }
}
