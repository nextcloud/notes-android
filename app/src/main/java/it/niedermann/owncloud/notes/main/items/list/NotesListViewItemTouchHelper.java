package it.niedermann.owncloud.notes.main.items.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.selection.SelectionTracker;
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
import it.niedermann.owncloud.notes.persistence.entity.Note;

public class NotesListViewItemTouchHelper extends ItemTouchHelper {

    private static final String TAG = NotesListViewItemTouchHelper.class.getSimpleName();
    private static final int UNDO_DURATION = 12_000;

    public NotesListViewItemTouchHelper(
            @NonNull Context context,
            @NonNull MainViewModel mainViewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull SelectionTracker<Long> tracker,
            @NonNull ItemAdapter adapter,
            @NonNull SwipeRefreshLayout swipeRefreshLayout,
            @NonNull View view,
            @NonNull View anchorView,
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
             * @param viewHolder   RecyclerView.ViewHolder
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
                    case ItemTouchHelper.LEFT -> {
                        viewHolder.setIsRecyclable(false);
                        final var dbNoteWithoutContent = (Note) adapter.getItem(viewHolder.getLayoutPosition());
                        final var dbNoteLiveData = mainViewModel.getFullNote$(dbNoteWithoutContent.getId());
                        dbNoteLiveData.observe(lifecycleOwner, (dbNote) -> {
                            dbNoteLiveData.removeObservers(lifecycleOwner);
                            tracker.deselect(dbNote.getId());
                            final var deleteLiveData = mainViewModel.deleteNoteAndSync(dbNote.getId());
                            deleteLiveData.observe(lifecycleOwner, (next) -> deleteLiveData.removeObservers(lifecycleOwner));
                            Log.v(TAG, "Item deleted through swipe ----------------------------------------------");
                            BrandedSnackbar.make(view, context.getString(R.string.action_note_deleted, dbNote.getTitle()), UNDO_DURATION)
                                    .setAnchorView(anchorView)
                                    .setAction(R.string.action_undo, (View v) -> {
                                        final var undoLiveData = mainViewModel.addNoteAndSync(dbNote);
                                        undoLiveData.observe(lifecycleOwner, (o) -> undoLiveData.removeObservers(lifecycleOwner));
                                        BrandedSnackbar.make(view, context.getString(R.string.action_note_restored, dbNote.getTitle()), Snackbar.LENGTH_SHORT)
                                                .setAnchorView(anchorView)
                                                .show();
                                    })
                                    .show();
                        });
                    }
                    case ItemTouchHelper.RIGHT -> {
                        viewHolder.setIsRecyclable(false);
                        final var adapterNote = (Note) adapter.getItem(viewHolder.getLayoutPosition());
                        final var toggleLiveData = mainViewModel.toggleFavoriteAndSync(adapterNote.getId());
                        toggleLiveData.observe(lifecycleOwner, (next) -> toggleLiveData.removeObservers(lifecycleOwner));
                    }
                    default -> {
                    }
                    //NoOp
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                final var noteViewHolder = (NoteViewHolder) viewHolder;
                // show swipe icon on the side
                noteViewHolder.showSwipe(dX > 0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, noteViewHolder.getNoteSwipeable(), dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState == ACTION_STATE_SWIPE) {
                    Log.i(TAG, "Start swiping, disable swipeRefreshLayout");
                    swipeRefreshLayoutEnabled = swipeRefreshLayout.isEnabled();
                    swipeRefreshLayout.setEnabled(false);
                    if (viewHolder != null) {
                        adapter.setSwipedPosition(viewHolder.getLayoutPosition());
                    }
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                Log.i(TAG, "End swiping, resetting swipeRefreshLayout state");
                swipeRefreshLayout.setEnabled(swipeRefreshLayoutEnabled);
                getDefaultUIUtil().clearView(((NoteViewHolder) viewHolder).getNoteSwipeable());
                adapter.setSwipedPosition(null);
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 3;
            }
        });
    }
}
