package it.niedermann.owncloud.notes.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int section_type = 0;
    private static final int note_type = 1;
    private final NoteClickListener noteClickListener;
    private List<Item> itemList = null;
    private List<Integer> selected = null;

    public ItemAdapter(NoteClickListener noteClickListener) {
        this.itemList = new ArrayList<>();
        this.selected = new ArrayList<>();
        this.noteClickListener = noteClickListener;
    }

    /**
     * Updates the item list and notifies respective view to update.
     * @param itemList
     */
    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    /**
     * Adds the given note to the top of the list.
     * @param note Note that should be added.
     */
    public void add(DBNote note) {
        itemList.add(0, note);
        notifyItemInserted(0);
        notifyItemChanged(0);
    }

    /**
     * Replaces a note with an updated version
     * @param note Note with the changes.
     * @param position position in the list of the node
     */
    public void replace(DBNote note, int position) {
        itemList.set(position, note);
        notifyItemChanged(position);
    }

    /**
     * Removes all items from the adapter.
     */
    public void removeAll() {
        itemList.clear();
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == section_type) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notes_list_section_item, parent, false);
            return new SectionViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_notes_list_note_item, parent, false);
            return new NoteViewHolder(v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Item item = itemList.get(position);
        if (item.isSection()) {
            SectionItem section = (SectionItem) item;
            ((SectionViewHolder) holder).sectionTitle.setText(section.geTitle());
            ((SectionViewHolder) holder).setPosition(position);
        } else {
            final DBNote note = (DBNote) item;
            final NoteViewHolder nvHolder = ((NoteViewHolder) holder);
            nvHolder.noteTitle.setText(note.getTitle());
            nvHolder.noteCategory.setVisibility(note.getCategory().isEmpty() ? View.GONE : View.VISIBLE);
            nvHolder.noteCategory.setText(note.getCategory());
            nvHolder.noteExcerpt.setText(note.getExcerpt());
            nvHolder.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.GONE : View.VISIBLE);
            nvHolder.noteFavorite.setImageResource(note.isFavorite() ? R.drawable.ic_star_grey600_24dp : R.drawable.ic_star_outline_grey600_24dp);
            nvHolder.noteFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    noteClickListener.onNoteFavoriteClick(position, view);
                }
            });
            nvHolder.setPosition(position);
        }
    }

    public boolean select(Integer position) {
        return !selected.contains(position) && selected.add(position);
    }

    public void clearSelection() {
        selected.clear();
    }

    public List<Integer> getSelected() {
        return selected;
    }

    public boolean deselect(Integer position) {
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i).equals(position)) {
                //position was selected and removed
                selected.remove(i);
                return true;
            }
        }
        // position was not selected
        return false;
    }

    public Item getItem(int notePosition) {
        return itemList.get(notePosition);
    }

    public void remove(Item item) {
        itemList.remove(item);
        notifyDataSetChanged();
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
        public ImageView noteDeleteLeft, noteDeleteRight;
        public TextView noteTitle;
        public TextView noteCategory;
        public TextView noteExcerpt;
        public ImageView noteStatus;
        public ImageView noteFavorite;
        public int position = -1;

        private NoteViewHolder(View v) {
            super(v);
            this.noteSwipeable = v.findViewById(R.id.noteSwipeable);
            this.noteDeleteLeft = (ImageView) v.findViewById(R.id.noteDeleteLeft);
            this.noteDeleteRight = (ImageView) v.findViewById(R.id.noteDeleteRight);
            this.noteTitle = (TextView) v.findViewById(R.id.noteTitle);
            this.noteCategory = (TextView) v.findViewById(R.id.noteCategory);
            this.noteExcerpt = (TextView) v.findViewById(R.id.noteExcerpt);
            this.noteStatus = (ImageView) v.findViewById(R.id.noteStatus);
            this.noteFavorite = (ImageView) v.findViewById(R.id.noteFavorite);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        public void setPosition(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            noteClickListener.onNoteClick(position, v);
        }

        @Override
        public boolean onLongClick(View v) {
            return noteClickListener.onNoteLongClick(position, v);
        }

        public void showSwipeDelete(boolean left) {
            noteDeleteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
            noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public TextView sectionTitle;
        public int position = -1;

        private SectionViewHolder(View v) {
            super(v);
            this.sectionTitle = (TextView) v.findViewById(R.id.sectionTitle);
        }

        public void setPosition(int pos) {
            position = pos;
        }
    }
}