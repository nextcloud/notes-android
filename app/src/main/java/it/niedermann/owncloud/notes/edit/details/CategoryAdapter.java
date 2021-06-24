package it.niedermann.owncloud.notes.edit.details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemCategoryBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private final List<NavigationItem> categories = new ArrayList<>();
    @NonNull
    private final CategoryListener listener;
    private final Context context;


    public CategoryAdapter(@NonNull Context context, @NonNull CategoryListener categoryListener) {
        this.context = context;
        this.listener = categoryListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final NavigationItem category = categories.get(position);
        final CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
        categoryViewHolder.getIcon().setImageDrawable(ContextCompat.getDrawable(context, category.icon));
        categoryViewHolder.getCategoryWrapper().setOnClickListener((v) -> listener.onCategoryChosen(category.label));
        categoryViewHolder.getCategory().setText(NoteUtil.extendCategory(category.label));
        if (category.count != null && category.count > 0) {
            categoryViewHolder.getCount().setText(String.valueOf(category.count));
        } else {
            categoryViewHolder.getCount().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        private CategoryViewHolder(View view) {
            super(view);
            binding = ItemCategoryBinding.bind(view);
        }

        private View getCategoryWrapper() {
            return binding.categoryWrapper;
        }

        private AppCompatImageView getIcon() {
            return binding.icon;
        }

        private TextView getCategory() {
            return binding.category;
        }

        private TextView getCount() {
            return binding.count;
        }
    }

    /**
     * @deprecated use {@link #setCategoryList(List)}
     */
    public void setCategoryList(List<NavigationItem.CategoryNavigationItem> categories, @Nullable String currentSearchString) {
        setCategoryList(categories);
    }

    public void setCategoryList(@NonNull List<NavigationItem.CategoryNavigationItem> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
        notifyDataSetChanged();
    }

    public interface CategoryListener {
        void onCategoryChosen(String category);

        default void onCategoryAdded() {
        }

        default void onCategoryCleared() {
        }
    }
}
