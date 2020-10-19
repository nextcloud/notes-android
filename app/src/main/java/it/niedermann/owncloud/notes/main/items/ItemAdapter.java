package it.niedermann.owncloud.notes.main.items;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
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
import it.niedermann.owncloud.notes.main.items.grid.NoteViewGridHolder;
import it.niedermann.owncloud.notes.main.items.grid.NoteViewGridHolderOnlyTitle;
import it.niedermann.owncloud.notes.main.items.list.NoteViewHolderWithExcerpt;
import it.niedermann.owncloud.notes.main.items.list.NoteViewHolderWithoutExcerpt;
import it.niedermann.owncloud.notes.main.items.section.SectionItem;
import it.niedermann.owncloud.notes.main.items.section.SectionViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
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
    private SelectionTracker<Long> tracker = null;
    //    private final List<Integer> selected = new ArrayList<>();
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
        setHasStableIds(true);
    }


    // FIXME this causes {@link it.niedermann.owncloud.notes.noteslist.items.list.NotesListViewItemTouchHelper} to not call clearView anymore → After marking a note as favorite, it stays yellow.
    @Override
    public long getItemId(int position) {
        return getItemViewType(position) == TYPE_SECTION
                ? ((SectionItem) getItem(position)).getTitle().hashCode() * -1
                : ((NoteWithCategory) getItem(position)).getId();
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (gridView) {
            switch (viewType) {
                case TYPE_SECTION: {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(inflater));
                }
                case TYPE_NOTE_ONLY_TITLE: {
                    return new NoteViewGridHolderOnlyTitle(ItemNotesListNoteItemGridOnlyTitleBinding.inflate(inflater, parent, false), noteClickListener, monospace, fontSize);
                }
                case TYPE_NOTE_WITH_EXCERPT:
                case TYPE_NOTE_WITHOUT_EXCERPT: {
                    return new NoteViewGridHolder(ItemNotesListNoteItemGridBinding.inflate(inflater, parent, false), noteClickListener, monospace, fontSize);
                }
                default: {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
            }
        } else {
            switch (viewType) {
                case TYPE_SECTION: {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(inflater));
                }
                case TYPE_NOTE_WITH_EXCERPT: {
                    return new NoteViewHolderWithExcerpt(ItemNotesListNoteItemWithExcerptBinding.inflate(inflater, parent, false), noteClickListener);
                }
                case TYPE_NOTE_ONLY_TITLE:
                case TYPE_NOTE_WITHOUT_EXCERPT: {
                    return new NoteViewHolderWithoutExcerpt(ItemNotesListNoteItemWithoutExcerptBinding.inflate(inflater, parent, false), noteClickListener);
                }
                default: {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        boolean isSelected = false;
        if(tracker != null) {
            Long itemId = getItemId(position);
            if(tracker.isSelected(itemId)) {
                tracker.select(itemId);
                isSelected = true;
            } else {
                tracker.deselect(itemId);
            }
        }
        switch (getItemViewType(position)) {
            case TYPE_SECTION: {
                ((SectionViewHolder) holder).bind((SectionItem) itemList.get(position));
                break;
            }
            case TYPE_NOTE_WITH_EXCERPT:
            case TYPE_NOTE_WITHOUT_EXCERPT:
            case TYPE_NOTE_ONLY_TITLE: {
                ((NoteViewHolder) holder).bind(new ItemDetailsLookup.ItemDetails<Long>() {
                    @Override
                    public int getPosition() {
                        return position;
                    }

                    @Override
                    public Long getSelectionKey() {
                        return getItemId(position);
                    }
                }, isSelected, (NoteWithCategory) itemList.get(position), showCategory, mainColor, textColor, searchQuery);
                break;
            }
        }
    }

    // --------------------------------------------------------------------------------------------


    public void setTracker(SelectionTracker<Long> tracker) {
        this.tracker = tracker;
    }

    public static class ItemIdKeyProvider extends ItemKeyProvider<Long> {
        private final RecyclerView recyclerView;

        public ItemIdKeyProvider(RecyclerView recyclerView) {
            super(SCOPE_MAPPED);
            this.recyclerView = recyclerView;
        }

        @Nullable
        @Override
        public Long getKey(int position) {
            final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter == null) {
                throw new IllegalStateException("RecyclerView adapter is not set!");
            }
            return adapter.getItemId(position);
        }

        @Override
        public int getPosition(@NonNull Long key) {
            final RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(key);
            return viewHolder == null ? NO_POSITION : viewHolder.getLayoutPosition();
        }
    }

    public static class ItemLookup extends ItemDetailsLookup<Long> {

        @NonNull
        private final RecyclerView rv;

        public ItemLookup(@NonNull RecyclerView recyclerView) {
            this.rv = recyclerView;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View view = rv.findChildViewUnder(e.getX(), e.getY());
            if(view != null) {
                return ((NoteViewHolder) rv.getChildViewHolder(view))
                        .getItemDetails();
            }
            return null;
        }
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
        NoteWithCategory note = (NoteWithCategory) getItem(position);
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
        notifyDataSetChanged();
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