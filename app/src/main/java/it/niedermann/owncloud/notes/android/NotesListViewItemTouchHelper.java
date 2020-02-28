package it.niedermann.owncloud.notes.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper.ViewProvider;

public class NotesListViewItemTouchHelper extends ItemTouchHelper {

    private static final String TAG = NotesListViewItemTouchHelper.class.getCanonicalName();

    public NotesListViewItemTouchHelper(
            SingleSignOnAccount ssoAccount,
            Context context,
            ViewProvider viewProvider,
            NotesDatabase db,
            ItemAdapter adapter,
            ISyncCallback syncCallBack,
            Runnable refreshLists
    ) {
        super(new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Disable swipe on sections
             *
             * @param recyclerView RecyclerView
             * @param viewHolder   RecyclerView.ViewHoler
             * @return 0 if section, otherwise super()
             */
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ItemAdapter.SectionViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            /**
             * Delete note if note is swiped to left or right
             *
             * @param viewHolder RecyclerView.ViewHoler
             * @param direction  int
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT: {
                        final DBNote dbNoteWithoutContent = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        final DBNote dbNote = db.getNote(dbNoteWithoutContent.getAccountId(), dbNoteWithoutContent.getId());
                        db.deleteNoteAndSync(ssoAccount, dbNote.getId());
                        adapter.remove(dbNote);
                        refreshLists.run();
                        Log.v(TAG, "Item deleted through swipe ----------------------------------------------");
                        Snackbar.make(viewProvider.getView(), context.getString(R.string.action_note_deleted, dbNote.getTitle()), Snackbar.LENGTH_LONG)
                                .setAction(R.string.action_undo, (View v) -> {
                                    db.getNoteServerSyncHelper().addCallbackPush(ssoAccount, refreshLists::run);
                                    db.addNoteAndSync(ssoAccount, dbNote.getAccountId(), dbNote);
                                    refreshLists.run();
                                    Snackbar.make(viewProvider.getView(), context.getString(R.string.action_note_restored, dbNote.getTitle()), Snackbar.LENGTH_SHORT)
                                            .show();
                                })
                                .show();
                        break;
                    }
                    case ItemTouchHelper.RIGHT: {
                        final DBNote dbNote = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        db.toggleFavorite(ssoAccount, dbNote, syncCallBack);
                        refreshLists.run();
                        break;
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                ItemAdapter.NoteViewHolder noteViewHolder = (ItemAdapter.NoteViewHolder) viewHolder;
                // show swipe icon on the side
                noteViewHolder.showSwipe(dX > 0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, noteViewHolder.noteSwipeable, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 3;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView(((ItemAdapter.NoteViewHolder) viewHolder).noteSwipeable);
            }
        });
    }
}
