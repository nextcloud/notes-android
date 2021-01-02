package it.niedermann.owncloud.notes.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import androidx.lifecycle.LiveData;
import androidx.recyclerview.selection.SelectionTracker;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class MultiSelectedActionModeCallback implements Callback {

    @ColorInt
    private final int colorAccent;
    @NonNull
    private final Context context;
    @NonNull
    private final View view;
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
            @NonNull Context context, @NonNull View view, @NonNull MainViewModel mainViewModel, @NonNull LifecycleOwner lifecycleOwner, boolean canMoveNoteToAnotherAccounts, @NonNull SelectionTracker<Long> tracker, @NonNull FragmentManager fragmentManager) {
        this.context = context;
        this.view = view;
        this.mainViewModel = mainViewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.canMoveNoteToAnotherAccounts = canMoveNoteToAnotherAccounts;
        this.tracker = tracker;
        this.fragmentManager = fragmentManager;

        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // inflate contextual menu
        mode.getMenuInflater().inflate(R.menu.menu_list_context_multiple, menu);
        menu.findItem(R.id.menu_move).setVisible(canMoveNoteToAnotherAccounts);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
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
            final List<Long> selection = new ArrayList<>(tracker.getSelection().size());
            for(Long sel : tracker.getSelection()) {
                selection.add(sel);
            }
            final LiveData<List<Note>> fullNotes$ = mainViewModel.getFullNotesWithCategory(selection);
            fullNotes$.observe(lifecycleOwner, (fullNotes) -> {
                fullNotes$.removeObservers(lifecycleOwner);
                tracker.clearSelection();
                final LiveData<Void> deleteLiveData = mainViewModel.deleteNotesAndSync(selection);
                deleteLiveData.observe(lifecycleOwner, (next) -> deleteLiveData.removeObservers(lifecycleOwner));
                String deletedSnackbarTitle = fullNotes.size() == 1
                        ? context.getString(R.string.action_note_deleted, fullNotes.get(0).getTitle())
                        : context.getResources().getQuantityString(R.plurals.bulk_notes_deleted, fullNotes.size(), fullNotes.size());
                BrandedSnackbar.make(view, deletedSnackbarTitle, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, (View v) -> {
                            for (Note deletedNote : fullNotes) {
                                final LiveData<Note> undoLiveData = mainViewModel.addNoteAndSync(deletedNote);
                                undoLiveData.observe(lifecycleOwner, (o) -> undoLiveData.removeObservers(lifecycleOwner));
                            }
                            String restoreSnackbarTitle = fullNotes.size() == 1
                                    ? context.getString(R.string.action_note_restored, fullNotes.get(0).getTitle())
                                    : context.getResources().getQuantityString(R.plurals.bulk_notes_restored, fullNotes.size(), fullNotes.size());
                            BrandedSnackbar.make(view, restoreSnackbarTitle, Snackbar.LENGTH_SHORT)
                                    .show();
                        })
                        .show();
            });
            return true;
        } else if (itemId == R.id.menu_move) {
            final LiveData<Account> currentAccount$ = mainViewModel.getCurrentAccount();
            currentAccount$.observe(lifecycleOwner, account -> {
                currentAccount$.removeObservers(lifecycleOwner);
                new Thread(() -> {
                    AccountPickerDialogFragment
                            .newInstance(new ArrayList<>(mainViewModel.getAccounts()), account.getId())
                            .show(fragmentManager, AccountPickerDialogFragment.class.getSimpleName());
                }).start();
            });
            return true;
        } else if (itemId == R.id.menu_share) {
            final List<Long> selection = new ArrayList<>(tracker.getSelection().size());
            for(Long sel : tracker.getSelection()) {
                selection.add(sel);
            }
            // FIXME use title if only one
            final String subject = context.getResources().getQuantityString(R.plurals.share_multiple, selection.size(), selection.size());
//            final String subject = (selection.size() == 1)
//                    ? ((Note) adapter.getItem(adapter.getSelected().get(0))).getTitle()
//                    : context.getResources().getQuantityString(R.plurals.share_multiple, adapter.getSelected().size(), adapter.getSelected().size());

            final LiveData<String> contentCollector = mainViewModel.collectNoteContents(selection);
            contentCollector.observe(lifecycleOwner, (next) -> {
                contentCollector.removeObservers(lifecycleOwner);
                ShareUtil.openShareDialog(context, subject, next);
                tracker.clearSelection();
            });
            return true;
        } else if (itemId == R.id.menu_category) {// TODO detect whether all selected notes do have the same category - in this case preselect it
            final LiveData<Account> accountLiveData = mainViewModel.getCurrentAccount();
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
        if(mode != null) {
            mode.finish();
        }
        tracker.clearSelection();
    }
}
