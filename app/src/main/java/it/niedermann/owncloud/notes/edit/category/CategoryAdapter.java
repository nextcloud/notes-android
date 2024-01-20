package it.niedermann.owncloud.notes.edit.category;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemCategoryBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String clearItemId = "clear_item";
    private static final String addItemId = "add_item";
    @NonNull
    private final List<NavigationItem> categories = new ArrayList<>();
    @NonNull
    private final CategoryListener listener;
    private final Context context;

    CategoryAdapter(@NonNull Context context, @NonNull CategoryListener categoryListener) {
        this.context = context;
        this.listener = categoryListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final var category = categories.get(position);
        final var categoryViewHolder = (CategoryViewHolder) holder;

        switch (category.id) {
            case addItemId -> {
                final var wrapDrawable = DrawableCompat.wrap(
                        Objects.requireNonNull(ContextCompat.getDrawable(
                                context, category.icon)));
                DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context, R.color.icon_color_default));
                categoryViewHolder.getIcon().setImageDrawable(wrapDrawable);
                categoryViewHolder.getCategoryWrapper().setOnClickListener((v) -> listener.onCategoryAdded());
            }
            case clearItemId -> {
                categoryViewHolder.getIcon().setImageDrawable(ContextCompat.getDrawable(context, category.icon));
                categoryViewHolder.getCategoryWrapper().setOnClickListener((v) -> listener.onCategoryCleared());
            }
            default -> {
                categoryViewHolder.getIcon().setImageDrawable(ContextCompat.getDrawable(context, category.icon));
                categoryViewHolder.getCategoryWrapper().setOnClickListener((v) -> listener.onCategoryChosen(category.label));
            }
        }
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

    void setCategoryList(List<NavigationItem.CategoryNavigationItem> categories, @Nullable String currentSearchString) {
        this.categories.clear();
        this.categories.addAll(categories);
        final NavigationItem clearItem = new NavigationItem(clearItemId, context.getString(R.string.no_category), 0, R.drawable.ic_clear_grey_24dp);
        this.categories.add(0, clearItem);
        if (currentSearchString != null && currentSearchString.trim().length() > 0) {
            boolean currentSearchStringIsInCategories = false;
            for (final var category : categories) {
                if (currentSearchString.equals(category.label)) {
                    currentSearchStringIsInCategories = true;
                    break;
                }
            }
            if (!currentSearchStringIsInCategories) {
                final var addItem = new NavigationItem(addItemId, context.getString(R.string.add_category, currentSearchString.trim()), 0, R.drawable.ic_add_blue_24dp);
                this.categories.add(addItem);
            }
        }
        notifyDataSetChanged();
    }

    public interface CategoryListener {
        void onCategoryChosen(String category);

        void onCategoryAdded();

        void onCategoryCleared();
    }
}
