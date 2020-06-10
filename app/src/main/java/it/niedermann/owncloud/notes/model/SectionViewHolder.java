package it.niedermann.owncloud.notes.model;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    private final ItemNotesListSectionItemBinding binding;

    public SectionViewHolder(ItemNotesListSectionItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        if (itemView.getLayoutParams() != null && itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
        }
    }

    public void bind(SectionItem item) {
        binding.sectionTitle.setText(item.getTitle());
    }
}