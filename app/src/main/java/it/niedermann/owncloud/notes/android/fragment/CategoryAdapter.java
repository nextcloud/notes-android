package it.niedermann.owncloud.notes.android.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.NavigationAdapter.NavigationItem;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String clearItemId = "clear_item";
    private static final String addItemId = "add_item";
    @NonNull
    private List<NavigationItem> categories = new ArrayList<>();
    @NonNull
    private CategoryListener listener;
    private Context context;

    CategoryAdapter(@NonNull Context context, @NonNull CategoryListener categoryListener) {
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
        NavigationItem category = categories.get(position);
        CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;

        switch (category.id) {
            case addItemId: {
                Drawable wrapDrawable = DrawableCompat.wrap(context.getResources().getDrawable(category.icon));
                DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(R.color.icon_color_default));
                categoryViewHolder.icon.setImageDrawable(wrapDrawable);
                categoryViewHolder.categoryWrapper.setOnClickListener((v) -> listener.onCategoryAdded());
                break;
            }
            case clearItemId: {
                categoryViewHolder.icon.setImageDrawable(context.getResources().getDrawable(category.icon));
                categoryViewHolder.categoryWrapper.setOnClickListener((v) -> listener.onCategoryCleared());
                break;
            }
            default: {
                categoryViewHolder.icon.setImageDrawable(context.getResources().getDrawable(category.icon));
                categoryViewHolder.categoryWrapper.setOnClickListener((v) -> listener.onCategoryChosen(category.label));
            }
        }
        categoryViewHolder.category.setText(NoteUtil.extendCategory(category.label));
        if (category.count > 0) {
            categoryViewHolder.count.setText(String.valueOf(category.count));
        } else {
            categoryViewHolder.count.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.categoryWrapper)
        View categoryWrapper;

        @BindView(R.id.icon)
        AppCompatImageView icon;

        @BindView(R.id.category)
        TextView category;

        @BindView(R.id.count)
        TextView count;

        private CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    void setCategoryList(List<NavigationItem> categories, String currentSearchString) {
        this.categories = categories;
        NavigationItem clearItem = new NavigationItem(clearItemId, context.getString(R.string.no_category), 0, R.drawable.ic_clear_grey_24dp);
        this.categories.add(0, clearItem);
        if (currentSearchString != null && currentSearchString.trim().length() > 0) {
            boolean currentSearchStringIsInCategories = false;
            for (NavigationItem category : categories.subList(1, categories.size())) {
                if (currentSearchString.equals(category.label)) {
                    currentSearchStringIsInCategories = true;
                    break;
                }
            }
            if (!currentSearchStringIsInCategories) {
                NavigationItem addItem = new NavigationItem(addItemId, context.getString(R.string.add_category, currentSearchString.trim()), 0, R.drawable.ic_add_blue_24dp);
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
