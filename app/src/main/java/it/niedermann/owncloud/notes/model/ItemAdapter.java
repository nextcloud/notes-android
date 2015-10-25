package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.niedermann.owncloud.notes.R;

public class ItemAdapter extends ArrayAdapter<Item> {
	/**
	 * Sections and Note-Items
	 */
	private static final int count_types = 2;
	private static final int section_type = 0;
	private static final int note_type = 1;
	private List<Item> itemList = null;

	public ItemAdapter(Context context, List<Item> itemList) {
		super(context, android.R.layout.simple_list_item_1, itemList);
		this.itemList = itemList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Item item = itemList.get(position);
		if (item.isSection()) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.fragment_notes_list_section_item, null);
			}
			SectionItem section = (SectionItem) item;
			TextView sectionTitle = (TextView) convertView.findViewById(R.id.sectionTitle);
			if (sectionTitle != null) {
				sectionTitle.setText(section.geTitle());
			}
		} else {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.fragment_notes_list_note_item, null);
			}
			Note note = (Note) item;
			((TextView) convertView.findViewById(R.id.noteTitle)).setText(note.getTitle());
			((TextView) convertView.findViewById(R.id.noteExcerpt)).setText(note.getExcerpt());
		}
		return convertView;
	}

	/**
	 * @return count_types
	 */
	@Override
	public int getViewTypeCount() {
		return count_types;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isSection() ? section_type : note_type;
	}
}