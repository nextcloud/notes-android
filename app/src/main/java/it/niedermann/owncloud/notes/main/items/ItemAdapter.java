package it.niedermann.owncloud.notes.main.items;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
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
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Branded {

    private static final String TAG = ItemAdapter.class.getSimpleName();

    public static final int TYPE_SECTION = 0;
    public static final int TYPE_NOTE_WITH_EXCERPT = 1;
    public static final int TYPE_NOTE_WITHOUT_EXCERPT = 2;
    public static final int TYPE_NOTE_ONLY_TITLE = 3;

    private final NoteClickListener noteClickListener;
    private final boolean gridView;
    @NonNull
    private final List<Item> itemList = new ArrayList<>();
    private boolean showCategory = true;
    private CharSequence searchQuery;
    private SelectionTracker<Long> tracker = null;
    @Px
    private final float fontSize;
    private final boolean monospace;
    @ColorInt
    private int color;
    @Nullable
    private Integer swipedPosition;

    public <T extends Context & NoteClickListener> ItemAdapter(@NonNull T context, boolean gridView) {
        this.noteClickListener = context;
        this.gridView = gridView;
        this.color = ContextCompat.getColor(context, R.color.defaultBrand);
        final var sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.fontSize = getFontSizeFromPreferences(context, sp);
        this.monospace = sp.getBoolean(context.getString(R.string.pref_key_font), false);
        setHasStableIds(true);
    }


    // FIXME this causes {@link it.niedermann.owncloud.notes.noteslist.items.list.NotesListViewItemTouchHelper} to not call clearView anymore → After marking a note as favorite, it stays yellow.
    @Override
    public long getItemId(int position) {
        return getItemViewType(position) == TYPE_SECTION
                ? ((SectionItem) getItem(position)).getTitle().hashCode() * -1
                : ((Note) getItem(position)).getId();
    }

    /**
     * Updates the item list and notifies respective view to update.
     *
     * @param itemList List of items to be set
     */
    public void setItemList(@NonNull List<Item> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        this.swipedPosition = null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (gridView) {
            switch (viewType) {
                case TYPE_SECTION -> {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(inflater));
                }
                case TYPE_NOTE_ONLY_TITLE -> {
                    return new NoteViewGridHolderOnlyTitle(ItemNotesListNoteItemGridOnlyTitleBinding.inflate(inflater, parent, false), noteClickListener, monospace, fontSize);
                }
                case TYPE_NOTE_WITH_EXCERPT, TYPE_NOTE_WITHOUT_EXCERPT -> {
                    return new NoteViewGridHolder(ItemNotesListNoteItemGridBinding.inflate(inflater, parent, false), noteClickListener, monospace, fontSize);
                }
                default -> {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
            }
        } else {
            switch (viewType) {
                case TYPE_SECTION -> {
                    return new SectionViewHolder(ItemNotesListSectionItemBinding.inflate(inflater));
                }
                case TYPE_NOTE_WITH_EXCERPT -> {
                    return new NoteViewHolderWithExcerpt(ItemNotesListNoteItemWithExcerptBinding.inflate(inflater, parent, false), noteClickListener);
                }
                case TYPE_NOTE_ONLY_TITLE, TYPE_NOTE_WITHOUT_EXCERPT -> {
                    return new NoteViewHolderWithoutExcerpt(ItemNotesListNoteItemWithoutExcerptBinding.inflate(inflater, parent, false), noteClickListener);
                }
                default -> {
                    throw new IllegalArgumentException("Not supported viewType: " + viewType);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        boolean isSelected = false;
        if (tracker != null) {
            final Long itemId = getItemId(position);
            if (tracker.isSelected(itemId)) {
                tracker.select(itemId);
                isSelected = true;
            } else {
                tracker.deselect(itemId);
            }
        }
        switch (getItemViewType(position)) {
            case TYPE_SECTION ->
                    ((SectionViewHolder) holder).bind((SectionItem) itemList.get(position));
            case TYPE_NOTE_WITH_EXCERPT,
                    TYPE_NOTE_WITHOUT_EXCERPT,
                    TYPE_NOTE_ONLY_TITLE ->
                    ((NoteViewHolder) holder).bind(isSelected, (Note) itemList.get(position), showCategory, color, searchQuery);
        }
    }

    public void setTracker(SelectionTracker<Long> tracker) {
        this.tracker = tracker;
    }

    public Item getItem(int notePosition) {
        return itemList.get(notePosition);
    }

    public boolean hasItemPosition(int notePosition) {
        return notePosition >= 0 && notePosition < itemList.size();
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
        final var item = getItem(position);
        if (item == null) {
            throw new IllegalArgumentException("Item at position " + position + " must not be null");
        }
        if (getItem(position).isSection()) return TYPE_SECTION;
        final var note = (Note) getItem(position);
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
    public void applyBrand(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    public void setHighlightSearchQuery(CharSequence searchQuery) {
        this.searchQuery = searchQuery;
        notifyDataSetChanged();
    }

    /**
     * @return the position of the first {@link Item} which matches the given viewtype, -1 if not available
     */
    public int getFirstPositionOfViewType(@IntRange(from = 0, to = 3) int viewType) {
        for (int i = 0; i < itemList.size(); i++) {
            if (getItemViewType(i) == viewType) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public Integer getSwipedPosition() {
        return swipedPosition;
    }

    public void setSwipedPosition(@Nullable Integer swipedPosition) {
        this.swipedPosition = swipedPosition;
    }
}