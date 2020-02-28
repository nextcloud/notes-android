package it.niedermann.owncloud.notes.model;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ItemAdapter.class.getCanonicalName();

    private static final int section_type = 0;
    private static final int note_type = 1;
    private final NoteClickListener noteClickListener;
    private List<Item> itemList;
    private boolean showCategory = true;
    private final List<Integer> selected;

    public ItemAdapter(@NonNull NoteClickListener noteClickListener) {
        this.itemList = new ArrayList<>();
        this.selected = new ArrayList<>();
        this.noteClickListener = noteClickListener;
    }

    /**
     * Updates the item list and notifies respective view to update.
     *
     * @param itemList List of items to be set
     */
    public void setItemList(@NonNull List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    /**
     * Adds the given note to the top of the list.
     *
     * @param note Note that should be added.
     */
    public void add(@NonNull DBNote note) {
        itemList.add(0, note);
        notifyItemInserted(0);
        notifyItemChanged(0);
    }

    /**
     * Removes all items from the adapter.
     */
    public void removeAll() {
        itemList.clear();
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == section_type) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notes_list_section_item, parent, false);
            return new SectionViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notes_list_note_item, parent, false);
            return new NoteViewHolder(v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Item item = itemList.get(position);
        if (item.isSection()) {
            SectionItem section = (SectionItem) item;
            ((SectionViewHolder) holder).sectionTitle.setText(section.geTitle());
        } else {
            final DBNote note = (DBNote) item;
            final NoteViewHolder nvHolder = ((NoteViewHolder) holder);
            nvHolder.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
            nvHolder.noteTitle.setText(Html.fromHtml(note.getTitle()));
            nvHolder.noteCategory.setVisibility(showCategory && !note.getCategory().isEmpty() ? View.VISIBLE : View.GONE);
            nvHolder.noteCategory.setText(Html.fromHtml(note.getCategory()));
            nvHolder.noteExcerpt.setText(Html.fromHtml(note.getExcerpt()));
            nvHolder.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
            nvHolder.noteFavorite.setImageResource(note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
            nvHolder.noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(holder.getAdapterPosition(), view));
        }
    }

    public boolean select(Integer position) {
        return !selected.contains(position) && selected.add(position);
    }

    public void clearSelection(RecyclerView recyclerView) {
        for (Integer i : getSelected()) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolder != null) {
                viewHolder.itemView.setSelected(false);
            } else {
                Log.w(TAG, "Could not found viewholder to remove selection");
            }
        }
        selected.clear();
    }

    @NonNull
    public List<Integer> getSelected() {
        return selected;
    }

    public void deselect(Integer position) {
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i).equals(position)) {
                //position was selected and removed
                selected.remove(i);
                return;
            }
        }
        // position was not selected
    }

    public Item getItem(int notePosition) {
        return itemList.get(notePosition);
    }

    public void remove(@NonNull Item item) {
        itemList.remove(item);
        notifyDataSetChanged();
    }

    public void setShowCategory(boolean showCategory) {
        this.showCategory = showCategory;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isSection() ? section_type : note_type;
    }

    public interface NoteClickListener {
        void onNoteClick(int position, View v);

        void onNoteFavoriteClick(int position, View v);

        boolean onNoteLongClick(int position, View v);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public View noteSwipeable;
        private final ItemNotesListNoteItemBinding b;
        private final TextView noteTitle;
        private final TextView noteCategory;
        private final TextView noteExcerpt;
        private final ImageView noteStatus;
        private final ImageView noteFavorite;

        private NoteViewHolder(View v) {
            super(v);
            b = ItemNotesListNoteItemBinding.bind(v);
            this.noteSwipeable = b.noteSwipeable;
            this.noteTitle = b.noteTitle;
            this.noteCategory = b.noteCategory;
            this.noteExcerpt = b.noteExcerpt;
            this.noteStatus = b.noteStatus;
            this.noteFavorite = b.noteFavorite;
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int adapterPosition = getAdapterPosition();
            if (adapterPosition != NO_POSITION) {
                noteClickListener.onNoteClick(adapterPosition, v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return noteClickListener.onNoteLongClick(getAdapterPosition(), v);
        }

        public void showSwipe(boolean left) {
            b.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
            b.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
            b.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        private TextView sectionTitle;

        private SectionViewHolder(View view) {
            super(view);
            ItemNotesListSectionItemBinding binding = ItemNotesListSectionItemBinding.bind(view);
            this.sectionTitle = binding.sectionTitle;
        }
    }
}