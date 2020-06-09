package it.niedermann.owncloud.notes.model;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    private final ItemNotesListSectionItemBinding binding;

    public SectionViewHolder(View view) {
        super(view);
        binding = ItemNotesListSectionItemBinding.bind(view);
    }

    public void bind(SectionItem item) {
        binding.sectionTitle.setText(item.getTitle());
        setPaddingTop(binding.sectionTitle, TextUtils.isEmpty(item.getTitle()) ? 0 : binding.sectionTitle.getContext().getResources().getDimensionPixelSize(R.dimen.item_section_padding_top));
    }

    private void setPaddingTop(@NonNull TextView textView, @Px int paddingTop) {
        textView.setPadding(textView.getPaddingLeft(), paddingTop, textView.getPaddingRight(), textView.getPaddingBottom());
    }
}