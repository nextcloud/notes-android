package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.SyncService;
import it.niedermann.owncloud.notes.util.ICallback;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Sections and Note-Items
     */
    private static final int count_types = 2;
    private static final int section_type = 0;
    private static final int note_type = 1;
    private static NoteClickListener noteClickListener;
    private List<Item> itemList = null;
    private List<Integer> selected = null;
    private Context context = null;

    /*
        public ItemAdapter(final List<Note> noteList, Context context) {
            super();
            this.context = context;
            this.itemList = new ArrayList<>();
            this.selected = new ArrayList<>();
        }
    */
    public ItemAdapter(final Context context) {
        super();
        this.context = context;
        this.itemList = new ArrayList<>();
        this.selected = new ArrayList<>();
    }

    /*
     *   Initial fill
     */

    public void fillItemList(List<Note> noteList) {
        itemList.clear();
        // #12 Create Sections depending on Time
        // NO-TODO Move to ItemAdapter? Yes :-)
        boolean todaySet, yesterdaySet, weekSet, monthSet, earlierSet;
        todaySet = yesterdaySet = weekSet = monthSet = earlierSet = false;
        Calendar recent = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        Calendar week = Calendar.getInstance();
        week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
        week.set(Calendar.HOUR_OF_DAY, 0);
        week.set(Calendar.MINUTE, 0);
        week.set(Calendar.SECOND, 0);
        week.set(Calendar.MILLISECOND, 0);
        Calendar month = Calendar.getInstance();
        month.set(Calendar.DAY_OF_MONTH, 0);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < noteList.size(); i++) {
            Note currentNote = noteList.get(i);
            if (!todaySet && recent.getTimeInMillis() - currentNote.getModified().getTimeInMillis() >= 600000 && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                // < 10 minutes but after 00:00 today
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_today)));
                }
                todaySet = true;
            } else if (!yesterdaySet && currentNote.getModified().getTimeInMillis() < today.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= yesterday.getTimeInMillis()) {
                // between today 00:00 and yesterday 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_yesterday)));
                }
                yesterdaySet = true;
            } else if (!weekSet && currentNote.getModified().getTimeInMillis() < yesterday.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= week.getTimeInMillis()) {
                // between yesterday 00:00 and start of the week 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_this_week)));
                }
                weekSet = true;
            } else if (!monthSet && currentNote.getModified().getTimeInMillis() < week.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= month.getTimeInMillis()) {
                // between start of the week 00:00 and start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_this_month)));
                }
                monthSet = true;
            } else if (!earlierSet && currentNote.getModified().getTimeInMillis() < month.getTimeInMillis()) {
                // before start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_earlier)));
                }
                earlierSet = true;
            }
            itemList.add(currentNote);
        }
    }

    public void add(Note createdNote) {
        itemList.add(0, createdNote);
        notifyDataSetChanged();
    }
            /*
     *    Manipulating Notes
     */

    public void remove(Item item) {
        if (!item.isSection()) {
            final int index = itemList.indexOf(item);
            SyncService.addCallback(new ICallback() {
                @Override
                public void onFinish() {
                    itemList.remove(index);
                }
            });
            Note noteItem = (Note) item;
            SyncService.startActionDeleteNoteAndSync(context, noteItem.getId());
        } else {
            itemList.remove(item);
        }
    }

    public void editNote(Note note) {
        //TODO correct sections
        // this.add(createdNote);
        SyncService.startActionEditNote(context, note);
        SyncService.addCallback(new ICallback() {
            @Override
            public void onFinish() {
                itemList.add(0, SyncService.getCreatedNote());
            }
        });
    }

    public void add(String createdNote) {
        //TODO correct sections
        SyncService.startActionCreateNote(context, createdNote);
        SyncService.addCallback(new ICallback() {
            @Override
            public void onFinish() {
                itemList.add(0, SyncService.getCreatedNote());
            }
        });
    }

    /*
     *
     * Handle the selection
     *
     */

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
            if (selected.get(i) == position) {
                //position was selected and removed
                selected.remove(i);
                return true;
            }
        }
        // position was not selected
        return false;
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

    public static void setNoteClickListener(NoteClickListener noteClickListener) {
        ItemAdapter.noteClickListener = noteClickListener;
    }


    public Item getItem(int notePosition) {
        return itemList.get(notePosition);
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