/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter.holder;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.resources.shares.OCShare;

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

    public void bind(OCShare share, ShareeListAdapterListener listener) {
        binding.copyInternalLinkIcon
                .getBackground()
                .setColorFilter(ResourcesCompat.getColor(context.getResources(),
                                R.color.widget_foreground,
                                null),
                        PorterDuff.Mode.SRC_IN);
        binding.copyInternalLinkIcon
                .getDrawable()
                .mutate()
                .setColorFilter(ResourcesCompat.getColor(context.getResources(),
                                R.color.fg_contrast,
                                null),
                        PorterDuff.Mode.SRC_IN);

        if (share.isFolder()) {
            binding.shareInternalLinkText.setText(context.getString(R.string.share_internal_link_to_folder_text));
        } else {
            binding.shareInternalLinkText.setText(context.getString(R.string.share_internal_link_to_file_text));
        }

        binding.copyInternalContainer.setOnClickListener(l -> listener.copyInternalLink());
    }
}
