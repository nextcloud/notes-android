package it.niedermann.owncloud.notes.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.NoteViewHolder;
import it.niedermann.owncloud.notes.model.SectionViewHolder;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper.ViewProvider;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

public class NotesListViewItemTouchHelper extends ItemTouchHelper {

    private static final String TAG = NotesListViewItemTouchHelper.class.getSimpleName();

    public NotesListViewItemTouchHelper(
            @NonNull SingleSignOnAccount ssoAccount,
            @NonNull Context context,
            @NonNull NotesDatabase db,
            @NonNull ItemAdapter adapter,
            @NonNull ISyncCallback syncCallBack,
            @NonNull Runnable refreshLists,
            @Nullable SwipeRefreshLayout swipeRefreshLayout,
            @Nullable ViewProvider viewProvider,
            boolean gridView) {
        super(new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private boolean swipeRefreshLayoutEnabled;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Disable swipe on sections and if grid view is enabled
             *
             * @param recyclerView RecyclerView
             * @param viewHolder   RecyclerView.ViewHoler
             * @return 0 if viewHolder is section or grid view is enabled, otherwise super()
             */
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (gridView || viewHolder instanceof SectionViewHolder) return 0;
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
                    case ItemTouchHelper.LEFT:
                        final DBNote dbNoteWithoutContent = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        final DBNote dbNote = db.getNote(dbNoteWithoutContent.getAccountId(), dbNoteWithoutContent.getId());
                        db.deleteNoteAndSync(ssoAccount, dbNote.getId());
                        adapter.remove(dbNote);
                        refreshLists.run();
                        Log.v(TAG, "Item deleted through swipe ----------------------------------------------");
                        if (viewProvider == null) {
                            Toast.makeText(context, context.getString(R.string.action_note_deleted, dbNote.getTitle()), Toast.LENGTH_LONG).show();
                        } else {
                            BrandedSnackbar.make(viewProvider.getView(), context.getString(R.string.action_note_deleted, dbNote.getTitle()), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_undo, (View v) -> {
                                        db.getNoteServerSyncHelper().addCallbackPush(ssoAccount, refreshLists::run);
                                        db.addNoteAndSync(ssoAccount, dbNote.getAccountId(), dbNote);
                                        refreshLists.run();
                                        BrandedSnackbar.make(viewProvider.getView(), context.getString(R.string.action_note_restored, dbNote.getTitle()), Snackbar.LENGTH_SHORT)
                                                .show();
                                    })
                                    .show();
                        }
                        break;
                    case ItemTouchHelper.RIGHT:
                        final DBNote adapterNote = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        db.toggleFavorite(ssoAccount, adapterNote, syncCallBack);
                        refreshLists.run();
                        break;
                    default:
                        //NoOp
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                NoteViewHolder noteViewHolder = (NoteViewHolder) viewHolder;
                // show swipe icon on the side
                noteViewHolder.showSwipe(dX > 0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, noteViewHolder.getNoteSwipeable(), dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState == ACTION_STATE_SWIPE && swipeRefreshLayout != null) {
                    Log.i(TAG, "Start swiping, disable swipeRefreshLayout");
                    swipeRefreshLayoutEnabled = swipeRefreshLayout.isEnabled();
                    swipeRefreshLayout.setEnabled(false);
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                Log.i(TAG, "End swiping, resetting swipeRefreshLayout state");
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setEnabled(swipeRefreshLayoutEnabled);
                }
                getDefaultUIUtil().clearView(((NoteViewHolder) viewHolder).getNoteSwipeable());
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 3;
            }
        });
    }
}
