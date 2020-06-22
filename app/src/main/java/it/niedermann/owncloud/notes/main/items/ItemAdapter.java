package it.niedermann.owncloud.notes.main.items;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.Branded;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridOnlyTitleBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithoutExcerptBinding;
import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.main.items.grid.NoteViewGridHolder;
import it.niedermann.owncloud.notes.main.items.grid.NoteViewGridHolderOnlyTitle;
import it.niedermann.owncloud.notes.main.items.list.NoteViewHolderWithExcerpt;
import it.niedermann.owncloud.notes.main.items.list.NoteViewHolderWithoutExcerpt;
import it.niedermann.owncloud.notes.main.items.section.SectionItem;
import it.niedermann.owncloud.notes.main.items.section.SectionViewHolder;

import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Branded {

    private static final String TAG = ItemAdapter.class.getSimpleName();

    public static final int TYPE_SECTION = 0;
    public static final int TYPE_NOTE_WITH_EXCERPT = 1;
    public static final int TYPE_NOTE_WITHOUT_EXCERPT = 2;
    public static final int TYPE_NOTE_ONLY_TITLE = 3;

    private final NoteClickListener noteClickListener;
    private final boolean gridView;
    private List<Item> itemList = new ArrayList<>();
    private boolean showCategory = true;
    private CharSequence searchQuery;
    private final List<Integer> selected = new ArrayList<>();
    @Px
    private final float fontSize;
    private final boolean monospace;
    @ColorInt
    private int mainColor;
    @ColorInt
    private int textColor;

    public <T extends Context & NoteClickListener> ItemAdapter(@NonNull T context, boolean gridView) {
        this.noteClickListener = context;
        this.gridView = gridView;
        this.mainColor = context.getResources().getColor(R.color.defaultBrand);
        this.textColor = Color.WHITE;
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.fontSize = getFontSizeFromPreferences(context, sp);
        this.monospace = sp.getBoolean(context.getString(R.string.pref_key_font), false);
        // FIXME see getItemId()
        // setHasStableIds(true);
    }


    /*
     FIXME this causes {@link it.niedermann.owncloud.notes.noteslist.items.list.NotesListViewItemTouchHelper} to not call clearView anymore → After marking a note as favorite, it stays yellow.
     @Override
     public long getItemId(int position) {
         return getItemViewType(position) == TYPE_SECTION
                 ? ((SectionItem) getItem(position)).getTitle().hashCode() * -1
                 : ((DBNote) getItem(position)).getId();
     }
    */

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
        if (gridView) {
            switch (viewType) {
                case TYPE_SECTION: {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(LayoutInflater.from(parent.getContext())));
                }
                case TYPE_NOTE_ONLY_TITLE: {
                    return new NoteViewGridHolderOnlyTitle(ItemNotesListNoteItemGridOnlyTitleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener, monospace, fontSize);
                }
                case TYPE_NOTE_WITH_EXCERPT:
                case TYPE_NOTE_WITHOUT_EXCERPT: {
                    return new NoteViewGridHolder(ItemNotesListNoteItemGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener, monospace, fontSize);
                }
                default: {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
            }
        } else {
            switch (viewType) {
                case TYPE_SECTION: {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(LayoutInflater.from(parent.getContext())));
                }
                case TYPE_NOTE_WITH_EXCERPT: {
                    return new NoteViewHolderWithExcerpt(ItemNotesListNoteItemWithExcerptBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener);
                }
                case TYPE_NOTE_ONLY_TITLE:
                case TYPE_NOTE_WITHOUT_EXCERPT: {
                    return new NoteViewHolderWithoutExcerpt(ItemNotesListNoteItemWithoutExcerptBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), noteClickListener);
                }
                default: {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
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
            case TYPE_NOTE_WITH_EXCERPT:
            case TYPE_NOTE_WITHOUT_EXCERPT:
            case TYPE_NOTE_ONLY_TITLE: {
                ((NoteViewHolder) holder).bind((DBNote) itemList.get(position), showCategory, mainColor, textColor, searchQuery);
                break;
            }
        }
    }

    public boolean select(Integer position) {
        return !selected.contains(position) && selected.add(position);
    }

    public void clearSelection(@NonNull RecyclerView recyclerView) {
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

    @IntRange(from = 0, to = 3)
    @Override
    public int getItemViewType(int position) {
        Item item = getItem(position);
        if (item == null) {
            throw new IllegalArgumentException("Item at position " + position + " must not be null");
        }
        if (getItem(position).isSection()) return TYPE_SECTION;
        DBNote note = (DBNote) getItem(position);
        if (TextUtils.isEmpty(note.getExcerpt())) {
            if (TextUtils.isEmpty(note.getCategory())) {
                return TYPE_NOTE_ONLY_TITLE;
            } else {
                return TYPE_NOTE_WITHOUT_EXCERPT;
            }
        }
        return TYPE_NOTE_WITH_EXCERPT;
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

    /**
     * @return the position of the first item which matches the given viewtype, -1 if not available
     */
    public int getFirstPositionOfViewType(@IntRange(from = 0, to = 3) int viewType) {
        for (int i = 0; i < itemList.size(); i++) {
            if (getItemViewType(i) == viewType) {
                return i;
            }
        }
        return -1;
    }
}