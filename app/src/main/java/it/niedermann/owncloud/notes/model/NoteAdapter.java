package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.niedermann.owncloud.notes.R;

public class NoteAdapter extends ArrayAdapter<Note> {
    private List<Note> noteList = null;

	public NoteAdapter(Context context,
			List<Note> noteList) {
		super(context, android.R.layout.simple_list_item_1, noteList);
		this.noteList = noteList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.fragment_notes_list_view, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this
		 * method. The variable simply refers to the position of the current
		 * object in the list. (The ArrayAdapter iterates through the list we
		 * sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Note note = noteList.get(position);

		if (note != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView noteTitle = (TextView) v.findViewById(R.id.noteTitle);
            TextView noteExcerpt = (TextView) v
                    .findViewById(R.id.noteExcerpt);
			TextView noteModified = (TextView) v.findViewById(R.id.noteModified);

			noteTitle.setText(note.getTitle());
			noteExcerpt.setText(note.getExcerpt());
			noteModified.setText(DateUtils.getRelativeDateTimeString(getContext(), note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
		}

		// the view must be returned to our activity
		return v;
	}
}