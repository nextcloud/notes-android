package it.niedermann.owncloud.notes.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> categoryList;

    public CategoryAdapter() {
        this.categoryList = new ArrayList<>();
    }

    /**
     * Updates the item list and notifies respective view to update.
     *
     * @param categoryList List of items to be set
     */
    public void setCategoryList(@NonNull List<String> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    /**
     * Adds the given note to the top of the list.
     *
     * @param category Category that should be added.
     */
    public void add(@NonNull String category) {
        categoryList.add(0, category);
        notifyItemInserted(0);
        notifyItemChanged(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_change_category_single, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((CategoryViewHolder) holder).title.setText(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}