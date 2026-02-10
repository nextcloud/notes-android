/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter.holder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemInternalShareLinkBinding;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

public class InternalShareViewHolder extends RecyclerView.ViewHolder {
    private ItemInternalShareLinkBinding binding;
    private Context context;

    public InternalShareViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public InternalShareViewHolder(ItemInternalShareLinkBinding binding, Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.context = context;
    }

    public void bind(ShareeListAdapterListener listener) {
        binding.shareInternalLinkText.setText(context.getString(R.string.share_internal_link_to_file_text));
        binding.copyInternalContainer.setOnClickListener(l -> listener.copyInternalLink());
    }
}
