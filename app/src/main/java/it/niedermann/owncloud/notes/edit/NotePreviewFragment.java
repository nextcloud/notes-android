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
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.util.regex.Matcher;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.FragmentNotePreviewBinding;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.AttachmentUrlUtil;
import it.niedermann.owncloud.notes.shared.util.SSOUtil;
import kotlin.Unit;

public class NotePreviewFragment extends SearchableBaseNoteFragment implements OnRefreshListener {

    private static final String TAG = NotePreviewFragment.class.getSimpleName();

    private String changedText;
    private String originalContent;  // Store original to prevent saving transformed content
    private boolean initialLoadComplete = false;  // Flag to skip saving during initial load

    protected FragmentNotePreviewBinding binding;

    private boolean noteLoaded = false;

    @Nullable
    private Runnable setScrollY;

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(true);
        if(getNormalEditButton().getVisibility() == View.VISIBLE) {
            menu.findItem(R.id.menu_edit).setVisible(false);
        }

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
    protected ExtendedFloatingActionButton getNormalEditButton() {
        return binding.edit;
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

    /**
     * Transforms attachment paths in markdown to authenticated WebDAV URLs so
     * images render via SSO-Glide. The note content itself is not modified; the
     * returned string is used for display only.
     */
    private String transformAttachmentUrls(String content, Note note) {
        if (content == null || localAccount == null || note == null) {
            return content;
        }

        final String username = localAccount.getUserName();
        final String category = note.getCategory();

        final Matcher matcher = AttachmentUrlUtil.ATTACHMENT_PATTERN.matcher(content);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            final String prefix = matcher.group(1);
            final String path = matcher.group(2);
            final String suffix = matcher.group(3);
            final String webdavUrl = AttachmentUrlUtil.transformAttachmentPath(
                    path, username, "Notes", category);
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + webdavUrl + suffix));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @Override
    protected void onNoteLoaded(Note note) {
        super.onNoteLoaded(note);
        if (note == null) {
            Log_OC.w(TAG, "Note is null, onNoteLoaded");
            return;
        }

        noteLoaded = true;
        registerInternalNoteLinkHandler();

        // Set the image URL prefix for loading images from the Nextcloud server
        if (localAccount != null && !localAccount.getUrl().isEmpty()) {
            binding.singleNoteContent.setMarkdownImageUrlPrefix(localAccount.getUrl());
        }

        lifecycleScopeIOJob(() -> {
            originalContent = note.getContent();  // Store original for comparison
            changedText = originalContent;  // Keep original for saving
            initialLoadComplete = false;  // Reset flag before setting content

            // Transform attachment URLs for display only
            final String displayContent = transformAttachmentUrls(originalContent, note);
            Log.d(TAG, "Original content has attachments: " + (originalContent != null && originalContent.contains(".attachments.")));

            onMainThread(() -> {
                binding.singleNoteContent.setMarkdownString(displayContent, setScrollY);

                final var activity = getActivity();
                if (activity == null) {
                    return Unit.INSTANCE;
                }

                binding.singleNoteContent.getMarkdownString().observe(activity, (newContent) -> {
                    // Skip saving during initial load or if content matches original
                    if (!initialLoadComplete) {
                        initialLoadComplete = true;
                        Log.d(TAG, "Skipping save during initial load");
                        return;
                    }

                    String newContentStr = newContent.toString();
                    // Only save if the content is actually different from original
                    // and doesn't contain our transformed API URLs
                    if (!newContentStr.equals(originalContent) &&
                        !newContentStr.contains("/index.php/apps/notes/api/v1/notes/") &&
                        !newContentStr.contains("/attachment?path=")) {
                        changedText = newContentStr;
                        saveNote(null);
                    } else {
                        Log.d(TAG, "Skipping save - content unchanged or contains transformed URLs");
                    }
                });
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
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
            lifecycleScopeIOJob(() -> {
                try {
                    final var account = repo.getAccountByName(SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext()).name);

                    repo.addCallbackPull(account, () -> {
                        note = repo.getNoteById(note.getId());
                        final String content = note.getContent();
                        originalContent = content;  // Store original
                        changedText = content;

                        // Transform attachment URLs for display
                        final String displayContent = transformAttachmentUrls(content, note);

                        onMainThread(() -> {
                            initialLoadComplete = false;  // Reset flag before setting content
                            binding.singleNoteContent.setMarkdownString(displayContent);
                            binding.swiperefreshlayout.setRefreshing(false);
                            return Unit.INSTANCE;
                        });
                    });

                    repo.scheduleSync(account, false);
                } catch (Exception e) {
                    Log_OC.e(TAG, "onRefresh exception: " + e);
                }
                return Unit.INSTANCE;
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
        
        lifecycleScopeIOJob(() -> {
            try {
                final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(getContext());
                binding.singleNoteContent.setCurrentSingleSignOnAccount(ssoAccount, color);
            } catch (Exception e) {
                Log_OC.e(TAG, "applyBrand exception: " + e);
            }
            return Unit.INSTANCE;
        });

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
