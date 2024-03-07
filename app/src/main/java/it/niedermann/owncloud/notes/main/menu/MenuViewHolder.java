/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.menu;

import static android.view.View.GONE;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemNavigationBinding;

public class MenuViewHolder extends RecyclerView.ViewHolder {

    private final ItemNavigationBinding binding;

    public MenuViewHolder(@NonNull ItemNavigationBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(@NonNull MenuItem menuItem, @ColorInt int color, @NonNull Consumer<MenuItem> onClick) {
        @NonNull Context context = itemView.getContext();

        binding.navigationItemIcon.setImageDrawable(ContextCompat.getDrawable(context, menuItem.getDrawableResource()));
        binding.navigationItemLabel.setText(context.getString(menuItem.getLabelResource()));
        binding.navigationItemCount.setVisibility(GONE);
        binding.getRoot().setOnClickListener((v) -> onClick.accept(menuItem));

        final var util = BrandingUtil.of(color, binding.getRoot().getContext());

        util.notes.colorNavigationViewItem(binding.getRoot());
        util.notes.colorNavigationViewItemIcon(binding.navigationItemIcon);
        util.notes.colorNavigationViewItemText(binding.navigationItemCount);
        util.notes.colorNavigationViewItemText(binding.navigationItemLabel);
    }
}
