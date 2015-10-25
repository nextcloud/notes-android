package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Collection;

import it.niedermann.owncloud.notes.R;

public class NoteAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = NoteAdapter.class.getSimpleName();

    private SortedList<Note> notes;
    private Context context;
    private NoteClickListener listener;

    @IntDef({
            RECENTLY,
            TODAY,
            YESTERDAY,
            THIS_WEEK,
            THIS_MONTH,
            EARLIER,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Timeframe {}
    public static final int RECENTLY = 0;
    public static final int TODAY = 1;
    public static final int YESTERDAY = 2;
    public static final int THIS_WEEK = 3;
    public static final int THIS_MONTH = 4;
    public static final int EARLIER = 5;

    @IntDef({
            TYPE_NOTE,
            TYPE_SECTION,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType {}
    public static final int TYPE_NOTE = 0;
    public static final int TYPE_SECTION = 1;

    public NoteAdapter(Context context, NoteClickListener listener) {
        notes = new SortedList<>(Note.class, new SortedList.Callback<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                // Reverse Calendar.compareTo result to sort most recent first
                return -o1.getModified().compareTo(o2.getModified());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Note oldItem, Note newItem) {
                return oldItem.getModified().equals(newItem.getModified()) &&
                        oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getContent().equals(newItem.getContent());
            }

            @Override
            public boolean areItemsTheSame(Note item1, Note item2) {
                return item1.equals(item2);
            }
        });
        this.context = context;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ItemType int viewType) {
        switch (viewType) {
            case TYPE_NOTE:
                return new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notes_list_note_item, parent, false), listener);

            case TYPE_SECTION:
                return new SectionHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notes_list_section_item, parent, false));

            default:
                // Unreachable
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NoteHolder) {
            ((NoteHolder) holder).bind(notes.get(position), position);
        } else if (holder instanceof SectionHolder) {
            // TODO: bind holder
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public @ItemType int getItemViewType(int position) {
        // TODO: handle sections
        return TYPE_NOTE;
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public void replaceNote(int position, Note note) {
        notes.updateItemAt(position, note);
    }

    public void replaceNotes(Collection<Note> notes) {
        this.notes.beginBatchedUpdates();
        this.notes.clear();
        this.notes.addAll(notes);
        this.notes.endBatchedUpdates();
    }

    public void removeNotes(Collection<Note> notes) {
        this.notes.beginBatchedUpdates();
        for (Note note : notes) {
            this.notes.remove(note);
        }
        this.notes.endBatchedUpdates();
    }

    public Note getNote(int position) {
        return notes.get(position);
    }

    private static @Timeframe int getNoteTimeframe(Note note) {
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

        long noteModified = note.getModified().getTimeInMillis();

        if (recent.getTimeInMillis() - noteModified < 600000) {
            return RECENTLY;
        } else if (recent.getTimeInMillis() - noteModified >= 600000 && noteModified >= today.getTimeInMillis()) {
            return TODAY;
        } else if (noteModified < today.getTimeInMillis() && noteModified >= yesterday.getTimeInMillis()) {
            return YESTERDAY;
        } else if (noteModified < yesterday.getTimeInMillis() && noteModified >= week.getTimeInMillis()) {
            return THIS_WEEK;
        } else if (noteModified < week.getTimeInMillis() && noteModified >= month.getTimeInMillis()) {
            return THIS_MONTH;
        } else {
            return EARLIER;
        }
    }

    public interface NoteClickListener {
        void onNoteClicked(int position);
        boolean onNoteLong(int position);
    }

    private final class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View root;
        private TextView title;
        private TextView excerpt;
        private TextView modified;
        private NoteClickListener listener;

        public NoteHolder(View itemView, NoteClickListener listener) {
            super(itemView);

            this.listener = listener;
            root = itemView;
            title = (TextView) itemView.findViewById(R.id.noteTitle);
            excerpt = (TextView) itemView.findViewById(R.id.noteExcerpt);
            modified = (TextView) itemView.findViewById(R.id.noteModified);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onNoteClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return listener != null && listener.onNoteLong(getAdapterPosition());

        }

        protected void bind(Note note, int position) {
            root.setSelected(isSelected(position));
            title.setText(note.getTitle());
            excerpt.setText(note.getExcerpt());
            modified.setText(DateUtils.getRelativeDateTimeString(context, note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
        }
    }

    private final class SectionHolder extends RecyclerView.ViewHolder {
        private TextView title;

        public SectionHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.sectionTitle);
        }

        protected void bind(@Timeframe int timeframe) {
            int strId;
            switch (timeframe) {
                case RECENTLY:
                    strId = R.string.listview_updated_recent;
                    break;

                case TODAY:
                    strId = R.string.listview_updated_today;
                    break;

                case YESTERDAY:
                    strId = R.string.listview_updated_yesterday;
                    break;

                case THIS_WEEK:
                    strId = R.string.listview_updated_this_week;
                    break;

                case THIS_MONTH:
                    strId = R.string.listview_updated_this_month;
                    break;

                default:
                case EARLIER:
                    strId = R.string.listview_updated_earlier;
                    break;
            }
            title.setText(context.getString(strId));
        }
    }
}
