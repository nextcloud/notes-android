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
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper.ViewProvider;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class MultiSelectedActionModeCallback implements Callback {

    @ColorInt
    private int colorAccent;

    private final Context context;
    private final ViewProvider viewProvider;
    private final NotesDatabase db;
    private final long currentLocalAccountId;
    private final boolean canMoveNoteToAnotherAccounts;
    private final ItemAdapter adapter;
    private final RecyclerView recyclerView;
    private final Runnable refreshLists;
    private final FragmentManager fragmentManager;
    private final SearchView searchView;

    public MultiSelectedActionModeCallback(
            Context context, ViewProvider viewProvider, NotesDatabase db, long currentLocalAccountId, boolean canMoveNoteToAnotherAccounts, ItemAdapter adapter, RecyclerView recyclerView, Runnable refreshLists, FragmentManager fragmentManager, SearchView searchView) {
        this.context = context;
        this.viewProvider = viewProvider;
        this.db = db;
        this.currentLocalAccountId = currentLocalAccountId;
        this.canMoveNoteToAnotherAccounts = canMoveNoteToAnotherAccounts;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.refreshLists = refreshLists;
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
                    List<DBNote> deletedNotes = new ArrayList<>();
                    List<Integer> selection = adapter.getSelected();
                    for (Integer i : selection) {
                        DBNote note = (DBNote) adapter.getItem(i);
                        deletedNotes.add(db.getNote(note.getAccountId(), note.getId()));
                        db.deleteNoteAndSync(ssoAccount, note.getId());
                    }
                    mode.finish(); // Action picked, so close the CAB
                    //after delete selection has to be cleared
                    searchView.setIconified(true);
                    refreshLists.run();
                    String deletedSnackbarTitle = deletedNotes.size() == 1
                            ? context.getString(R.string.action_note_deleted, deletedNotes.get(0).getTitle())
                            : context.getString(R.string.bulk_notes_deleted, deletedNotes.size());
                    BrandedSnackbar.make(viewProvider.getView(), deletedSnackbarTitle, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, (View v) -> {
                                db.getNoteServerSyncHelper().addCallbackPush(ssoAccount, refreshLists::run);
                                for (DBNote deletedNote : deletedNotes) {
                                    db.addNoteAndSync(ssoAccount, deletedNote.getAccountId(), deletedNote);
                                }
                                refreshLists.run();
                                String restoreSnackbarTitle = deletedNotes.size() == 1
                                        ? context.getString(R.string.action_note_restored, deletedNotes.get(0).getTitle())
                                        : context.getString(R.string.bulk_notes_restored, deletedNotes.size());
                                BrandedSnackbar.make(viewProvider.getView(), restoreSnackbarTitle, Snackbar.LENGTH_SHORT)
                                        .show();
                            })
                            .show();
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.menu_move:
                AccountPickerDialogFragment.newInstance(currentLocalAccountId).show(fragmentManager, MainActivity.class.getSimpleName());
                return true;
            case R.id.menu_share:
                final String subject = (adapter.getSelected().size() == 1)
                        ? ((DBNote) adapter.getItem(adapter.getSelected().get(0))).getTitle()
                        : context.getString(R.string.share_multiple, adapter.getSelected().size());
                final StringBuilder noteContents = new StringBuilder();
                for (Integer i : adapter.getSelected()) {
                    final DBNote noteWithoutContent = (DBNote) adapter.getItem(i);
                    final String tempFullNote = db.getNote(noteWithoutContent.getAccountId(), noteWithoutContent.getId()).getContent();
                    if (!TextUtils.isEmpty(tempFullNote)) {
                        if (noteContents.length() > 0) {
                            noteContents.append("\n\n");
                        }
                        noteContents.append(tempFullNote);
                    }
                }
                ShareUtil.openShareDialog(context, subject, noteContents.toString());
                return true;
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
