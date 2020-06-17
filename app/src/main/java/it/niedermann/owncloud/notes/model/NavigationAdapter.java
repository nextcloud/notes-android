package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemNavigationBinding;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    @NonNull
    private final Context context;
    @ColorInt
    private int mainColor;
    @DrawableRes
    public static final int ICON_FOLDER = R.drawable.ic_folder_grey600_24dp;
    @DrawableRes
    public static final int ICON_NOFOLDER = R.drawable.ic_folder_open_grey600_24dp;
    @DrawableRes
    public static final int ICON_SUB_FOLDER = R.drawable.ic_folder_grey600_18dp;
    @DrawableRes
    public static final int ICON_MULTIPLE = R.drawable.ic_create_new_folder_grey600_24dp;
    @DrawableRes
    public static final int ICON_MULTIPLE_OPEN = R.drawable.ic_folder_grey600_24dp;
    @DrawableRes
    public static final int ICON_SUB_MULTIPLE = R.drawable.ic_create_new_folder_grey600_18dp;

    public void applyBrand(int mainColor, int textColor) {
        this.mainColor = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, mainColor);
        notifyDataSetChanged();
    }

    public static class NavigationItem {
        @NonNull
        public String id;
        @NonNull
        public String label;
        @DrawableRes
        public int icon;
        @Nullable
        public Integer count;

        public NavigationItem(@NonNull String id, @NonNull String label, @Nullable Integer count, @DrawableRes int icon) {
            this.id = id;
            this.label = label;
            this.count = count;
            this.icon = icon;
        }
    }

    public static class CategoryNavigationItem extends NavigationItem {
        @NonNull
        public Long categoryId;

        public CategoryNavigationItem(@NonNull String id, @NonNull String label, @Nullable Integer count, @DrawableRes int icon, @NonNull Long categoryId) {
            super(id, label, count, icon);
            this.categoryId = categoryId;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final View view;

        @NonNull
        private final TextView name;
        @NonNull
        private final TextView count;
        @NonNull
        private final ImageView icon;

        private NavigationItem currentItem;

        ViewHolder(@NonNull View itemView, @NonNull final ClickListener clickListener) {
            super(itemView);
            view = itemView;
            ItemNavigationBinding binding = ItemNavigationBinding.bind(view);
            this.name = binding.navigationItemLabel;
            this.count = binding.navigationItemCount;
            this.icon = binding.navigationItemIcon;
            icon.setOnClickListener(view -> clickListener.onIconClick(currentItem));
            itemView.setOnClickListener(view -> clickListener.onItemClick(currentItem));
        }

        private void bind(@NonNull NavigationItem item) {
            currentItem = item;
            boolean isSelected = item.id.equals(selectedItem);
            name.setText(NoteUtil.extendCategory(item.label));
            count.setVisibility(item.count == null ? View.GONE : View.VISIBLE);
            count.setText(String.valueOf(item.count));
            if (item.icon > 0) {
                icon.setImageDrawable(DrawableCompat.wrap(icon.getResources().getDrawable(item.icon)));
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
            int textColor = isSelected ? mainColor : view.getResources().getColor(R.color.fg_default);

            name.setTextColor(textColor);
            count.setTextColor(textColor);
            icon.setColorFilter(isSelected ? textColor : 0);

            view.setSelected(isSelected);
        }
    }

    public interface ClickListener {
        void onItemClick(NavigationItem item);

        void onIconClick(NavigationItem item);
    }

    @NonNull
    private List<NavigationItem> items = new ArrayList<>();
    private String selectedItem = null;
    @NonNull
    private final ClickListener clickListener;

    public NavigationAdapter(@NonNull Context context, @NonNull ClickListener clickListener) {
        this.context = context;
        this.mainColor = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, BrandingUtil.readBrandMainColor(context));
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<NavigationItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelectedItem(String id) {
        selectedItem = id;
        notifyDataSetChanged();
    }

    public String getSelectedItem() {
        return selectedItem;
    }
}
