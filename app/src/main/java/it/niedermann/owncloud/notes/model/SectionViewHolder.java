package it.niedermann.owncloud.notes.model;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    private final ItemNotesListSectionItemBinding binding;

    public SectionViewHolder(View view) {
        super(view);
        binding = ItemNotesListSectionItemBinding.bind(view);
    }

    public void bind(SectionItem item) {
        binding.sectionTitle.setText(item.getTitle());
    }
}