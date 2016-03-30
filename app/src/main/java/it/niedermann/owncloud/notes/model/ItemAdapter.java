package it.niedermann.owncloud.notes.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int section_type = 0;
    private static final int note_type = 1;
    private static NoteClickListener noteClickListener;
    private List<Item> itemList = null;
    private List<Integer> selected = null;
    public ItemAdapter(List<Item> itemList) {
        //super(context, android.R.layout.simple_list_item_1, itemList);
        super();
        this.itemList = itemList;
        this.selected = new ArrayList<>();
    }

    /**
     * Sets the given NoteClickListener that should be notified on clicks
     * @param noteClickListener NoteClickListener
     */
    public static void setNoteClickListener(NoteClickListener noteClickListener) {
        ItemAdapter.noteClickListener = noteClickListener;
    }

    /**
     * Adds the given note to the top of the list.
     * @param note Note that should be added.
     */
    public void add(Note note) {
        itemList.add(0, note);
        notifyItemInserted(0);
        notifyItemChanged(0);
    }

    /**
     * Compares the given List of notes to the current internal holded notes and updates the list if necessairy
     * @param newNotes List of more up to date notes
     */
    public void checkForUpdates(List<Note> newNotes) {
        for(Note newNote : newNotes) {
            boolean foundNewNoteInOldList = false;
            for(Item oldItem : itemList) {
                if(!oldItem.isSection()) {
                    Note oldNote = (Note) oldItem;
                    if(newNote.getId() == oldNote.getId()) {
                        // Notes have the same id, check which is newer
                        if(newNote.getModified().after(oldNote.getModified())) {
                            // Replace old note with new note because new note has been edited more recently
                            int indexOfOldNote = itemList.indexOf(oldNote);
                            itemList.remove(indexOfOldNote);
                            itemList.add(indexOfOldNote, newNote);
                            this.notifyItemChanged(indexOfOldNote);
                        }
                        foundNewNoteInOldList = true;
                        break;
                    }
                }
            }
            if(!foundNewNoteInOldList) {
                // Add new note because it could not be found in the itemList
                add(newNote);
            }
        }
        //TODO check if a note has been deleted on server??
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Item item = itemList.get(position);
        if (item.isSection()) {
            SectionItem section = (SectionItem) item;
            ((SectionViewHolder) holder).sectionTitle.setText(section.geTitle());
            ((SectionViewHolder) holder).setPosition(position);
        } else {
            Note note = (Note) item;
            ((NoteViewHolder) holder).noteTitle.setText(note.getTitle());
            ((NoteViewHolder) holder).noteExcerpt.setText(note.getExcerpt());
            ((NoteViewHolder) holder).setPosition(position);
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
        boolean onNoteLongClick(int position, View v);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        // each data item is just a string in this case
        public TextView noteTitle;
        public TextView noteExcerpt;
        public int position = -1;

        private NoteViewHolder(View v) {
            super(v);
            this.noteTitle = (TextView) v.findViewById(R.id.noteTitle);
            this.noteExcerpt = (TextView) v.findViewById(R.id.noteExcerpt);
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