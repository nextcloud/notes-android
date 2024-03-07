/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.navigation;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.main.MainActivity;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationViewHolder> {

    @NonNull
    private final Context context;
    @ColorInt
    private int color;
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

    @NonNull
    private List<NavigationItem> items = new ArrayList<>();
    private String selectedItem = null;
    @NonNull
    private final NavigationClickListener navigationClickListener;

    public NavigationAdapter(@NonNull Context context, @NonNull NavigationClickListener navigationClickListener) {
        this.context = context;
        this.color = BrandingUtil.readBrandMainColor(context);
        this.navigationClickListener = navigationClickListener;
    }

    public void applyBrand(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NavigationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NavigationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false), navigationClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationViewHolder holder, int position) {
        holder.bind(items.get(position), color, selectedItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<NavigationItem> items) {
        for (final var item : items) {
            if (TextUtils.isEmpty(item.label)) {
                item.id = MainActivity.ADAPTER_KEY_UNCATEGORIZED;
                item.label = context.getString(R.string.action_uncategorized);
                item.icon = NavigationAdapter.ICON_NOFOLDER;
                item.type = UNCATEGORIZED;
                break;
            }
        }
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelectedItem(String id) {
        selectedItem = id;
        notifyDataSetChanged();
    }
}
