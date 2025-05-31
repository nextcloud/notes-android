/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemAddPublicShareBinding;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

public class NewLinkShareViewHolder extends RecyclerView.ViewHolder {
    private ItemAddPublicShareBinding binding;

    public NewLinkShareViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public NewLinkShareViewHolder(ItemAddPublicShareBinding binding) {
        this(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ShareeListAdapterListener listener) {
        binding.addNewPublicShareLink.setOnClickListener(v -> listener.createPublicShareLink());
    }
}
