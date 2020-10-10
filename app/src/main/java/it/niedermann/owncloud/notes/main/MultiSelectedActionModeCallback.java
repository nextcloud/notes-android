package it.niedermann.owncloud.notes.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class MultiSelectedActionModeCallback implements Callback {

    @ColorInt
    private int colorAccent;

    private final Context context;
    private final View view;
    private final NotesDatabase db;
    private final long currentLocalAccountId;
    private final boolean canMoveNoteToAnotherAccounts;
    private final ItemAdapter adapter;
    private final RecyclerView recyclerView;
    private final FragmentManager fragmentManager;
    private final SearchView searchView;

    public MultiSelectedActionModeCallback(
            Context context, View view, NotesDatabase db, long currentLocalAccountId, boolean canMoveNoteToAnotherAccounts, ItemAdapter adapter, RecyclerView recyclerView, FragmentManager fragmentManager, SearchView searchView) {
        this.context = context;
        this.view = view;
        this.db = db;
        this.currentLocalAccountId = currentLocalAccountId;
        this.canMoveNoteToAnotherAccounts = canMoveNoteToAnotherAccounts;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.fragmentManager = fragmentManager;
        this.searchView = searchView;

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
        switch (item.getItemId()) {
            case R.id.menu_delete:
                try {
                    SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
                    List<NoteWithCategory> deletedNotes = new ArrayList<>();
                    List<Integer> selection = adapter.getSelected();
                    for (Integer i : selection) {
                        NoteWithCategory note = (NoteWithCategory) adapter.getItem(i);
                        deletedNotes.add(db.getNoteDao().getNoteWithCategory(note.getAccountId(), note.getId()));
                        db.deleteNoteAndSync(ssoAccount, note.getId());
                    }
                    mode.finish(); // Action picked, so close the CAB
                    //after delete selection has to be cleared
                    searchView.setIconified(true);
                    String deletedSnackbarTitle = deletedNotes.size() == 1
                            ? context.getString(R.string.action_note_deleted, deletedNotes.get(0).getTitle())
                            : context.getResources().getQuantityString(R.plurals.bulk_notes_deleted, deletedNotes.size(), deletedNotes.size());
                    BrandedSnackbar.make(view, deletedSnackbarTitle, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, (View v) -> {
                                db.getNoteServerSyncHelper().addCallbackPush(ssoAccount, () -> {
                                });
                                for (NoteWithCategory deletedNote : deletedNotes) {
                                    db.addNoteAndSync(ssoAccount, deletedNote.getAccountId(), deletedNote);
                                }
                                String restoreSnackbarTitle = deletedNotes.size() == 1
                                        ? context.getString(R.string.action_note_restored, deletedNotes.get(0).getTitle())
                                        : context.getResources().getQuantityString(R.plurals.bulk_notes_restored, deletedNotes.size(), deletedNotes.size());
                                BrandedSnackbar.make(view, restoreSnackbarTitle, Snackbar.LENGTH_SHORT)
                                        .show();
                            })
                            .show();
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_move:
                AccountPickerDialogFragment
                        .newInstance(currentLocalAccountId)
                        .show(fragmentManager, MainActivity.class.getSimpleName());
                return true;
            case R.id.menu_share:
                final String subject = (adapter.getSelected().size() == 1)
                        ? ((NoteWithCategory) adapter.getItem(adapter.getSelected().get(0))).getTitle()
                        : context.getResources().getQuantityString(R.plurals.share_multiple, adapter.getSelected().size(), adapter.getSelected().size());
                final StringBuilder noteContents = new StringBuilder();
                for (Integer i : adapter.getSelected()) {
                    final NoteWithCategory noteWithoutContent = (NoteWithCategory) adapter.getItem(i);
                    final String tempFullNote = db.getNoteDao().getNoteWithCategory(noteWithoutContent.getAccountId(), noteWithoutContent.getId()).getContent();
                    if (!TextUtils.isEmpty(tempFullNote)) {
                        if (noteContents.length() > 0) {
                            noteContents.append("\n\n");
                        }
                        noteContents.append(tempFullNote);
                    }
                }
                ShareUtil.openShareDialog(context, subject, noteContents.toString());
                return true;
            case R.id.menu_category:
                // TODO detect whether all selected notes do have the same category - in this case preselect it
                CategoryDialogFragment
                        .newInstance(currentLocalAccountId, "")
                        .show(fragmentManager, CategoryDialogFragment.class.getSimpleName());
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelection(recyclerView);
        adapter.notifyDataSetChanged();
    }
}
