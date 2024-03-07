/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import static java.lang.Boolean.TRUE;
import static it.niedermann.owncloud.notes.edit.EditNoteActivity.ACTION_SHORTCUT;
import static it.niedermann.owncloud.notes.shared.util.WidgetUtil.pendingIntentFlagCompat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment.CategoryDialogListener;
import it.niedermann.owncloud.notes.edit.title.EditTitleDialogFragment;
import it.niedermann.owncloud.notes.edit.title.EditTitleDialogFragment.EditTitleListener;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public abstract class BaseNoteFragment extends BrandedFragment implements CategoryDialogListener, EditTitleListener {

    private static final String TAG = BaseNoteFragment.class.getSimpleName();
    protected final ExecutorService executor = Executors.newCachedThreadPool();

    protected static final int MENU_ID_PIN = -1;
    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_NEWNOTE = "newNote";
    private static final String SAVEDKEY_NOTE = "note";
    private static final String SAVEDKEY_ORIGINAL_NOTE = "original_note";

    private Account localAccount;

    protected Note note;
    // TODO do we really need this? The reference to note is currently the same
    @Nullable
    private Note originalNote;
    private int originalScrollY;
    protected NotesRepository repo;
    @Nullable
    protected NoteFragmentListener listener;
    private boolean titleModified = false;

    protected boolean isNew = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (NoteFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + NoteFragmentListener.class);
        }
        repo = NotesRepository.getInstance(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor.submit(() -> {
            try {
                final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext().getApplicationContext());
                this.localAccount = repo.getAccountByName(ssoAccount.name);

                if (savedInstanceState == null) {
                    final long id = requireArguments().getLong(PARAM_NOTE_ID);
                    if (id > 0) {
                        final long accountId = requireArguments().getLong(PARAM_ACCOUNT_ID);
                        if (accountId > 0) {
                            /* Switch account if account id has been provided */
                            this.localAccount = repo.getAccountById(accountId);
                            SingleAccountHelper.commitCurrentAccount(requireContext().getApplicationContext(), localAccount.getAccountName());
                        }
                        isNew = false;
                        note = originalNote = repo.getNoteById(id);
                        requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                        requireActivity().invalidateOptionsMenu();
                    } else {
                        final var paramNote = (Note) requireArguments().getSerializable(PARAM_NEWNOTE);
                        final var content = requireArguments().getString(PARAM_CONTENT);
                        if (paramNote == null) {
                            if (content == null) {
                                throw new IllegalArgumentException(PARAM_NOTE_ID + " is not given, argument " + PARAM_NEWNOTE + " is missing and " + PARAM_CONTENT + " is missing.");
                            } else {
                                note = new Note(-1, null, Calendar.getInstance(), NoteUtil.generateNoteTitle(content), content, getString(R.string.category_readonly), false, null, DBStatus.VOID, -1, "", 0);
                                requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                                requireActivity().invalidateOptionsMenu();
                            }
                        } else {
                            paramNote.setStatus(DBStatus.LOCAL_EDITED);
                            note = repo.addNote(localAccount.getId(), paramNote);
                            originalNote = null;
                            requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                            requireActivity().invalidateOptionsMenu();
                        }
                    }
                } else {
                    note = (Note) savedInstanceState.getSerializable(SAVEDKEY_NOTE);
                    originalNote = (Note) savedInstanceState.getSerializable(SAVEDKEY_ORIGINAL_NOTE);
                    requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                    requireActivity().invalidateOptionsMenu();
                }
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                e.printStackTrace();
            }
        });
        setHasOptionsMenu(true);
    }

    @Nullable
    protected abstract ScrollView getScrollView();


    protected abstract void scrollToY(int scrollY);

    @Override
    public void onResume() {
        super.onResume();
        listener.onNoteUpdated(note);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveNote(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveNote(null);
        outState.putSerializable(SAVEDKEY_NOTE, note);
        outState.putSerializable(SAVEDKEY_ORIGINAL_NOTE, originalNote);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_fragment, menu);

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.add(Menu.NONE, MENU_ID_PIN, 110, R.string.pin_to_homescreen);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (note != null) {
            prepareFavoriteOption(menu.findItem(R.id.menu_favorite));

            final var preferredApiVersion = ApiVersionUtil.getPreferredApiVersion(localAccount.getApiVersion());
            menu.findItem(R.id.menu_title).setVisible(preferredApiVersion != null && preferredApiVersion.compareTo(ApiVersion.API_VERSION_1_0) >= 0);
            menu.findItem(R.id.menu_delete).setVisible(!isNew);
        }
    }

    private void prepareFavoriteOption(MenuItem item) {
        item.setIcon(note.getFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        item.setChecked(note.getFavorite());

        final var utils = BrandingUtil.of(colorAccent, requireContext());
        utils.platform.colorToolbarMenuIcon(requireContext(), item);
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menu_cancel) {
            executor.submit(() -> {
                if (originalNote == null) {
                    repo.deleteNoteAndSync(localAccount, note.getId());
                } else {
                    repo.updateNoteAndSync(localAccount, originalNote, null, null, null);
                }
            });
            listener.close();
            return true;
        } else if (itemId == R.id.menu_delete) {
            repo.deleteNoteAndSync(localAccount, note.getId());
            listener.close();
            return true;
        } else if (itemId == R.id.menu_favorite) {
            note.setFavorite(!note.getFavorite());
            repo.toggleFavoriteAndSync(localAccount, note.getId());
            listener.onNoteUpdated(note);
            prepareFavoriteOption(item);
            return true;
        } else if (itemId == R.id.menu_category) {
            showCategorySelector();
            return true;
        } else if (itemId == R.id.menu_title) {
            showEditTitleDialog();
            return true;
        } else if (itemId == R.id.menu_move) {
            executor.submit(() -> AccountPickerDialogFragment
                    .newInstance(new ArrayList<>(repo.getAccounts()), note.getAccountId())
                    .show(requireActivity().getSupportFragmentManager(), BaseNoteFragment.class.getSimpleName()));
            return true;
        } else if (itemId == R.id.menu_share) {
            shareNote();
            return false;
        } else if (itemId == MENU_ID_PIN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final var context = requireContext();
                if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                    final var pinShortcutInfo = new ShortcutInfoCompat.Builder(context, String.valueOf(note.getId()))
                            .setShortLabel(note.getTitle())
                            .setIcon(IconCompat.createWithResource(context.getApplicationContext(), TRUE.equals(note.getFavorite()) ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                            .setIntent(new Intent(getActivity(), EditNoteActivity.class).putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId()).setAction(ACTION_SHORTCUT))
                            .build();

                    ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, PendingIntent.getBroadcast(context, 0, ShortcutManagerCompat.createShortcutResultIntent(context, pinShortcutInfo), pendingIntentFlagCompat(0)).getIntentSender());
                } else {
                    Log.i(TAG, "RequestPinShortcut is not supported");
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void shareNote() {
        ShareUtil.openShareDialog(requireContext(), note.getTitle(), note.getContent());
    }

    @CallSuper
    protected void onNoteLoaded(Note note) {
        this.originalScrollY = note.getScrollY();
        scrollToY(originalScrollY);
        final var scrollView = getScrollView();
        if (scrollView != null) {
            scrollView.setOnScrollChangeListener((View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
                if (scrollY > 0) {
                    note.setScrollY(scrollY);
                }
                onScroll(scrollY, oldScrollY);
            });
        }
    }

    /**
     * Scroll callback, to be overridden by subclasses. Default implementation is empty
     */
    protected void onScroll(int scrollY, int oldScrollY) {
    }

    protected boolean shouldShowToolbar() {
        return true;
    }

    public void onCloseNote() {
        if (!titleModified && originalNote == null && getContent().isEmpty()) {
            repo.deleteNoteAndSync(localAccount, note.getId());
        }
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    protected void saveNote(@Nullable ISyncCallback callback) {
        Log.d(TAG, "saveData()");
        if (note != null) {
            final var newContent = getContent();
            if (note.getContent().equals(newContent)) {
                if (note.getScrollY() != originalScrollY) {
                    Log.v(TAG, "... only saving new scroll state, since content did not change");
                    repo.updateScrollY(note.getId(), note.getScrollY());
                } else {
                    Log.v(TAG, "... not saving, since nothing has changed");
                }
            } else {
                // FIXME requires database queries on main thread!
                note = repo.updateNoteAndSync(localAccount, note, newContent, null, callback);
                listener.onNoteUpdated(note);
                requireActivity().invalidateOptionsMenu();
            }
        } else {
            Log.e(TAG, "note is null");
        }
    }

    protected abstract String getContent();

    /**
     * Opens a dialog in order to chose a category
     */
    private void showCategorySelector() {
        final var fragmentId = "fragment_category";
        final var manager = requireActivity().getSupportFragmentManager();
        final var frag = manager.findFragmentByTag(fragmentId);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        final var categoryFragment = CategoryDialogFragment.newInstance(note.getAccountId(), note.getCategory());
        categoryFragment.setTargetFragment(this, 0);
        categoryFragment.show(manager, fragmentId);
    }

    /**
     * Opens a dialog in order to chose a category
     */
    public void showEditTitleDialog() {
        saveNote(null);
        final var fragmentId = "fragment_edit_title";
        final var manager = requireActivity().getSupportFragmentManager();
        final var frag = manager.findFragmentByTag(fragmentId);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        final var editTitleFragment = EditTitleDialogFragment.newInstance(note.getTitle());
        editTitleFragment.setTargetFragment(this, 0);
        editTitleFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        repo.setCategory(localAccount, note.getId(), category);
        note.setCategory(category);
        listener.onNoteUpdated(note);
    }

    @Override
    public void onTitleEdited(String newTitle) {
        titleModified = true;
        note.setTitle(newTitle);
        executor.submit(() -> {
            note = repo.updateNoteAndSync(localAccount, note, note.getContent(), newTitle, null);
            requireActivity().runOnUiThread(() -> listener.onNoteUpdated(note));
        });
    }

    public void moveNote(Account account) {
        final var moveLiveData = repo.moveNoteToAnotherAccount(account, note);
        moveLiveData.observe(this, (v) -> moveLiveData.removeObservers(this));
        listener.close();
    }

    public interface NoteFragmentListener {
        enum Mode {
            EDIT, PREVIEW, DIRECT_EDIT
        }

        void close();

        void onNoteUpdated(Note note);

        void changeMode(@NonNull Mode mode, boolean reloadNote);
    }
}
