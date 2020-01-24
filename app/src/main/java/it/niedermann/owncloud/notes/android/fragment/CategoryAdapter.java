package it.niedermann.owncloud.notes.android.fragment;

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
import it.niedermann.owncloud.notes.android.fragment.CategoryDialogFragment.CategoryDialogListener;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private List<NavigationAdapter.NavigationItem> categories = new ArrayList<>();
    @NonNull
    private CategoryDialogListener categoryListener;

    CategoryAdapter(@NonNull CategoryDialogListener categoryListener) {
        this.categoryListener = categoryListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NavigationAdapter.NavigationItem category = categories.get(position);
        CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
        categoryViewHolder.category.setOnClickListener((v) -> categoryListener.onCategoryChosen(NoteUtil.extendCategory(category.label)));
        categoryViewHolder.category.setText(category.label);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category)
        TextView category;

        private CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    void setCategoryList(List<NavigationAdapter.NavigationItem> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
}
