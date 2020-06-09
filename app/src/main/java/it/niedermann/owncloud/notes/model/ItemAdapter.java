package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.Branded;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

import static it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithoutExcerptBinding.inflate;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Branded {

    private static final String TAG = ItemAdapter.class.getSimpleName();

    private static final int TYPE_SECTION = R.layout.item_notes_list_section_item;
    private static final int TYPE_NOTE_WITH_EXCERPT = R.layout.item_notes_list_note_item_with_excerpt;
    private static final int TYPE_NOTE_WITHOUT_EXCERPT = R.layout.item_notes_list_note_item_without_excerpt;
    private final NoteClickListener noteClickListener;
    private List<Item> itemList = new ArrayList<>();
    private boolean showCategory = true;
    private CharSequence searchQuery;
    private final List<Integer> selected = new ArrayList<>();
    @ColorInt
    private int mainColor;
    @ColorInt
    private int textColor;

    public <T extends Context & NoteClickListener> ItemAdapter(@NonNull T context) {
        this.noteClickListener = context;
        this.mainColor = context.getResources().getColor(R.color.defaultBrand);
        this.textColor = Color.WHITE;
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SECTION: {
                return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(LayoutInflater.from(parent.getContext())));
            }
            case TYPE_NOTE_WITH_EXCERPT: {
                return new NoteViewHolderWithExcerpt(ItemNotesListNoteItemWithExcerptBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener);
            }
            case TYPE_NOTE_WITHOUT_EXCERPT: {
                return new NoteViewHolderWithoutExcerpt(inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener);
            }
            default: {
                throw new IllegalArgumentException("Not supported viewType: " + viewType);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_SECTION: {
                ((SectionViewHolder) holder).bind((SectionItem) itemList.get(position));
                break;
            }
            case TYPE_NOTE_WITH_EXCERPT: {
                ((NoteViewHolderWithExcerpt) holder).bind((DBNote) itemList.get(position), showCategory, mainColor, textColor, searchQuery);
                break;
            }
            case TYPE_NOTE_WITHOUT_EXCERPT: {
                ((NoteViewHolderWithoutExcerpt) holder).bind((DBNote) itemList.get(position), showCategory, mainColor, textColor, searchQuery);
                break;
            }
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
        Item item = getItem(position);
        if (item == null) {
            throw new IllegalArgumentException("Item at position " + position + " must not be null");
        }
        return getItem(position).isSection()
                ? TYPE_SECTION
                : TextUtils.isEmpty(((DBNote) getItem(position)).getExcerpt())
                ? TYPE_NOTE_WITHOUT_EXCERPT
                : TYPE_NOTE_WITH_EXCERPT;
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        this.mainColor = mainColor;
        this.textColor = textColor;
        notifyDataSetChanged();
    }

    public void setHighlightSearchQuery(CharSequence searchQuery) {
        this.searchQuery = searchQuery;
    }
}