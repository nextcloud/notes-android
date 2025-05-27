/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * @author TSI-mc
 *
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 * Copyright (C) 2021 TSI-mc
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.share.adapter.holder;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedViewHolder;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemShareLinkShareBinding;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

public class LinkShareViewHolder extends BrandedViewHolder {
    private ItemShareLinkShareBinding binding;
    private Context context;

    public LinkShareViewHolder(@NonNull View itemView) {
        super(itemView);
        bindBranding();
    }

    public LinkShareViewHolder(ItemShareLinkShareBinding binding, Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.context = context;
        bindBranding();
    }

    public void bind(OCShare publicShare, ShareeListAdapterListener listener) {
        if (publicShare.getShareType() != null && ShareType.EMAIL == publicShare.getShareType()) {
            binding.name.setText(publicShare.getSharedWithDisplayName());
            binding.icon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.ic_email,
                    null));
            binding.copyLink.setVisibility(View.GONE);

            binding.icon.getBackground().setColorFilter(context.getResources().getColor(R.color.nc_grey),
                    PorterDuff.Mode.SRC_IN);
            binding.icon.getDrawable().mutate().setColorFilter(context.getResources().getColor(R.color.icon_on_nc_grey),
                    PorterDuff.Mode.SRC_IN);
        } else {
            if (!TextUtils.isEmpty(publicShare.getLabel())) {
                String text = String.format(context.getString(R.string.share_link_with_label), publicShare.getLabel());
                binding.name.setText(text);
            } else {
                if (SharingMenuHelper.isSecureFileDrop(publicShare)) {
                    binding.name.setText(context.getResources().getString(R.string.share_permission_secure_file_drop));
                } else {
                    binding.name.setText(R.string.share_link);
                }
            }
        }

        binding.subline.setVisibility(View.GONE);

        String permissionName = SharingMenuHelper.getPermissionName(context, publicShare);
        setPermissionName(publicShare, permissionName);

        binding.overflowMenu.setOnClickListener(v -> listener.showSharingMenuActionSheet(publicShare));
        if (!SharingMenuHelper.isSecureFileDrop(publicShare)) {
            binding.shareByLinkContainer.setOnClickListener(v -> listener.showPermissionsDialog(publicShare));
        }

        binding.copyLink.setOnClickListener(v -> listener.copyLink(publicShare));
    }

    private void setPermissionName(OCShare publicShare, String permissionName) {
        if (!TextUtils.isEmpty(permissionName) && !SharingMenuHelper.isSecureFileDrop(publicShare)) {
            binding.permissionName.setText(permissionName);
            binding.permissionName.setVisibility(View.VISIBLE);
        } else {
            binding.permissionName.setVisibility(View.GONE);
        }
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, context);
        if (binding != null) {
            util.androidx.colorPrimaryTextViewElement(binding.permissionName);
            util.platform.colorImageViewBackgroundAndIcon(binding.icon);
        }
    }
}
