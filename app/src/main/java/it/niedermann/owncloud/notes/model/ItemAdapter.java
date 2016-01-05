package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.NoteViewHolder>{


	public void insert(Note createdNote, int i) {
        itemList.add(i,createdNote);
	}

	public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,View.OnClickListener{
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

        public void setPosition(int pos){
            position =pos;
        }

        @Override
        public void onClick(View v) {
            noteClickListener.onNoteClick(position,v);
        }

        @Override
        public boolean onLongClick(View v) {
            return noteClickListener.onNoteLongClick(position,v);
        }
    }
    public interface NoteClickListener{
        public void onNoteClick(int position,View v);
        public boolean onNoteLongClick(int position,View v);

    }


	/**
	 * Sections and Note-Items
	 */
	private static final int count_types = 2;
	private static final int section_type = 0;
	private static final int note_type = 1;
	private List<Item> itemList = null;
    private List<Integer> selected= null;
    private static NoteClickListener noteClickListener;


    public ItemAdapter(Context context, List<Item> itemList) {
		//super(context, android.R.layout.simple_list_item_1, itemList);
		super();
		this.itemList = itemList;
        this.selected = new ArrayList<Integer>();
	}

    public static void setNoteClickListener(NoteClickListener noteClickListener) {
        ItemAdapter.noteClickListener = noteClickListener;
    }

    public void add(Note createdNote) {
        itemList.add(createdNote);
    }
	// Create new views (invoked by the layout manager)
	@Override
	public NoteViewHolder onCreateViewHolder(ViewGroup parent,
												   int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.fragment_notes_list_note_item, parent, false);

		NoteViewHolder vh = new NoteViewHolder(v);

		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(NoteViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		Item item = itemList.get(position);
		Note note = (Note) item;
		holder.noteTitle.setText(note.getTitle());
		holder.noteExcerpt.setText(note.getExcerpt());
        holder.setPosition(position);
	}
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * select an Item of the List
     * @param position the position of the Item to select
     * @return if it is a new Item which was selected
     */
    public boolean select(int position){
        for (Integer pos:selected) {
            if(pos.intValue() == position){
                return false;
            }
        }
        selected.add(position);
        return true;
    }

    public void clearSelection(){
        selected.clear();
    }

	public List<Integer> getSelected() {
		return selected;
	}

	public boolean deselect(Integer position){
        for (int i=0;i<selected.size();i++ ) {
            if(selected.get(i)==position){
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
    }


}