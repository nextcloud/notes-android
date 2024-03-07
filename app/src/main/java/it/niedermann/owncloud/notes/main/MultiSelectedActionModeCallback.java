/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.selection.SelectionTracker;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class MultiSelectedActionModeCallback implements Callback {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @ColorInt
    private final int colorAccent;
    @NonNull
    private final Context context;
    @NonNull
    private final View view;
    @NonNull
    private final View anchorView;
    @NonNull
    private final MainViewModel mainViewModel;
    @NonNull
    private final LifecycleOwner lifecycleOwner;
    private final boolean canMoveNoteToAnotherAccounts;
    @NonNull
    private final SelectionTracker<Long> tracker;
    @NonNull
    private final FragmentManager fragmentManager;

    public MultiSelectedActionModeCallback(
            @NonNull Context context,
            @NonNull View view,
            @NonNull View anchorView,
            @NonNull MainViewModel mainViewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            boolean canMoveNoteToAnotherAccounts,
            @NonNull SelectionTracker<Long> tracker,
            @NonNull FragmentManager fragmentManager) {
        this.context = context;
        this.view = view;
        this.anchorView = anchorView;
        this.mainViewModel = mainViewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.canMoveNoteToAnotherAccounts = canMoveNoteToAnotherAccounts;
        this.tracker = tracker;
        this.fragmentManager = fragmentManager;

        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // inflate contextual menu
        mode.getMenuInflater().inflate(R.menu.menu_list_context_multiple, menu);
        menu.findItem(R.id.menu_move).setVisible(canMoveNoteToAnotherAccounts);
        for (int i = 0; i < menu.size(); i++) {
            var drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, colorAccent);
                menu.getItem(i).setIcon(drawable);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * @param mode ActionMode - used to close the Action Bar after all work is done.
     * @param item MenuItem - the item in the List that contains the Node
     * @return boolean
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_delete) {
            final var selection = new ArrayList<Long>(tracker.getSelection().size());
            for (final var sel : tracker.getSelection()) {
                selection.add(sel);
            }
            final var fullNotes$ = mainViewModel.getFullNotesWithCategory(selection);
            fullNotes$.observe(lifecycleOwner, (fullNotes) -> {
                fullNotes$.removeObservers(lifecycleOwner);
                tracker.clearSelection();
                final var deleteLiveData = mainViewModel.deleteNotesAndSync(selection);
                deleteLiveData.observe(lifecycleOwner, (next) -> deleteLiveData.removeObservers(lifecycleOwner));
                final String deletedSnackbarTitle = fullNotes.size() == 1
                        ? context.getString(R.string.action_note_deleted, fullNotes.get(0).getTitle())
                        : context.getResources().getQuantityString(R.plurals.bulk_notes_deleted, fullNotes.size(), fullNotes.size());
                BrandedSnackbar.make(view, deletedSnackbarTitle, Snackbar.LENGTH_LONG)
                        .setAnchorView(anchorView)
                        .setAction(R.string.action_undo, (View v) -> {
                            for (final var deletedNote : fullNotes) {
                                final var undoLiveData = mainViewModel.addNoteAndSync(deletedNote);
                                undoLiveData.observe(lifecycleOwner, (o) -> undoLiveData.removeObservers(lifecycleOwner));
                            }
                            String restoreSnackbarTitle = fullNotes.size() == 1
                                    ? context.getString(R.string.action_note_restored, fullNotes.get(0).getTitle())
                                    : context.getResources().getQuantityString(R.plurals.bulk_notes_restored, fullNotes.size(), fullNotes.size());
                            BrandedSnackbar.make(view, restoreSnackbarTitle, Snackbar.LENGTH_SHORT)
                                    .setAnchorView(anchorView)
                                    .show();
                        })
                        .show();
            });
            return true;
        } else if (itemId == R.id.menu_move) {
            final var currentAccount$ = mainViewModel.getCurrentAccount();
            currentAccount$.observe(lifecycleOwner, account -> {
                currentAccount$.removeObservers(lifecycleOwner);
                executor.submit(() -> AccountPickerDialogFragment
                        .newInstance(new ArrayList<>(mainViewModel.getAccounts()), account.getId())
                        .show(fragmentManager, AccountPickerDialogFragment.class.getSimpleName()));
            });
            return true;
        } else if (itemId == R.id.menu_share) {
            final var selection = new ArrayList<Long>(tracker.getSelection().size());
            for (final var sel : tracker.getSelection()) {
                selection.add(sel);
            }
            tracker.clearSelection();

            executor.submit(() -> {
                if (selection.size() == 1) {
                    final var note = mainViewModel.getFullNote(selection.get(0));
                    ShareUtil.openShareDialog(context, note.getTitle(), note.getContent());
                } else {
                    ShareUtil.openShareDialog(context,
                            context.getResources().getQuantityString(R.plurals.share_multiple, selection.size(), selection.size()),
                            mainViewModel.collectNoteContents(selection));
                }
            });
            return true;
        } else if (itemId == R.id.menu_category) {// TODO detect whether all selected notes do have the same category - in this case preselect it
            final var accountLiveData = mainViewModel.getCurrentAccount();
            accountLiveData.observe(lifecycleOwner, account -> {
                accountLiveData.removeObservers(lifecycleOwner);
                CategoryDialogFragment
                        .newInstance(account.getId(), "")
                        .show(fragmentManager, CategoryDialogFragment.class.getSimpleName());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mode != null) {
            mode.finish();
        }
        tracker.clearSelection();
    }
}
