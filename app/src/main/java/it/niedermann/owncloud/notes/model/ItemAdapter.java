package it.niedermann.owncloud.notes.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int section_type = 0;
    private static final int note_type = 1;
    private final NoteClickListener noteClickListener;
    private List<Item> itemList = null;
    private boolean showCategory = true;
    private List<Integer> selected = null;

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
     * Replaces a note with an updated version
     *
     * @param note     Note with the changes.
     * @param position position in the list of the node
     */
    public void replace(@NonNull DBNote note, int position) {
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
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
            nvHolder.noteFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    noteClickListener.onNoteFavoriteClick(holder.getAdapterPosition(), view);
                }
            });
        }
    }

    public boolean select(Integer position) {
        return !selected.contains(position) && selected.add(position);
    }

    public void clearSelection() {
        selected.clear();
    }

    @NonNull
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
        @BindView(R.id.noteSwipeable)
        public View noteSwipeable;
        View noteSwipeFrame;
        ImageView noteFavoriteLeft, noteDeleteRight;
        TextView noteTitle;
        @BindView(R.id.noteCategory)
        TextView noteCategory;
        @BindView(R.id.noteExcerpt)
        TextView noteExcerpt;
        @BindView(R.id.noteStatus)
        ImageView noteStatus;
        @BindView(R.id.noteFavorite)
        ImageView noteFavorite;

        private NoteViewHolder(View v) {
            super(v);
            this.noteSwipeFrame = v.findViewById(R.id.noteSwipeFrame);
            this.noteSwipeable = v.findViewById(R.id.noteSwipeable);
            this.noteFavoriteLeft = v.findViewById(R.id.noteFavoriteLeft);
            this.noteDeleteRight = v.findViewById(R.id.noteDeleteRight);
            this.noteTitle = v.findViewById(R.id.noteTitle);
            this.noteCategory = v.findViewById(R.id.noteCategory);
            this.noteExcerpt = v.findViewById(R.id.noteExcerpt);
            this.noteStatus = v.findViewById(R.id.noteStatus);
            this.noteFavorite = v.findViewById(R.id.noteFavorite);
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
            noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
            noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
            noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sectionTitle)
        TextView sectionTitle;

        private SectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}