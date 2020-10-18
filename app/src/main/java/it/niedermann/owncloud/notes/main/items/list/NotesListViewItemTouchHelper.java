package it.niedermann.owncloud.notes.main.items.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.main.MainViewModel;
import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.main.items.section.SectionViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;

public class NotesListViewItemTouchHelper extends ItemTouchHelper {

    private static final String TAG = NotesListViewItemTouchHelper.class.getSimpleName();
    private static final int UNDO_DURATION = 12_000;

    public NotesListViewItemTouchHelper(
            @NonNull Account account,
            @NonNull Context context,
            @NonNull MainViewModel mainViewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull ItemAdapter adapter,
            @Nullable SwipeRefreshLayout swipeRefreshLayout,
            @Nullable View view,
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
            @SuppressLint("WrongConstant")
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        final NoteWithCategory dbNoteWithoutContent = (NoteWithCategory) adapter.getItem(viewHolder.getAdapterPosition());
                        final LiveData<NoteWithCategory> dbNoteLiveData = mainViewModel.getFullNoteWithCategory(dbNoteWithoutContent.getId());
                        dbNoteLiveData.observe(lifecycleOwner, (dbNote) -> {
                            dbNoteLiveData.removeObservers(lifecycleOwner);
                            final LiveData<Void> deleteLiveData = mainViewModel.deleteNoteAndSync(dbNote.getId());
                            deleteLiveData.observe(lifecycleOwner, (next) -> deleteLiveData.removeObservers(lifecycleOwner));
                            Log.v(TAG, "Item deleted through swipe ----------------------------------------------");
                            if (view == null) {
                                Toast.makeText(context, context.getString(R.string.action_note_deleted, dbNote.getTitle()), Toast.LENGTH_LONG).show();
                            } else {
                                BrandedSnackbar.make(view, context.getString(R.string.action_note_deleted, dbNote.getTitle()), UNDO_DURATION)
                                        .setAction(R.string.action_undo, (View v) -> {
                                            final LiveData<NoteWithCategory> undoLiveData = mainViewModel.addNoteAndSync(dbNote);
                                            undoLiveData.observe(lifecycleOwner, (o) -> undoLiveData.removeObservers(lifecycleOwner));
                                            BrandedSnackbar.make(view, context.getString(R.string.action_note_restored, dbNote.getTitle()), Snackbar.LENGTH_SHORT)
                                                    .show();
                                        })
                                        .show();
                            }
                        });
                        break;
                    case ItemTouchHelper.RIGHT:
                        final NoteWithCategory adapterNote = (NoteWithCategory) adapter.getItem(viewHolder.getAdapterPosition());
                        LiveData<Void> toggleLiveData = mainViewModel.toggleFavoriteAndSync(adapterNote.getId());
                        toggleLiveData.observe(lifecycleOwner, (next) -> toggleLiveData.removeObservers(lifecycleOwner));
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
