/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.nextcloud.android.common.ui.theme.utils.ColorRole;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedViewHolder;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemShareLinkShareBinding;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

public class LinkShareViewHolder extends BrandedViewHolder {
    private ItemShareLinkShareBinding binding;
    private Context context;

    private BrandingUtil brandingUtil;

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
            if (publicShare.getLabel() != null && !publicShare.getLabel().isEmpty()) {
                brandingUtil.platform.colorTextView(binding.name, ColorRole.ON_SURFACE_VARIANT);
                binding.label.setText(publicShare.getLabel());
                binding.label.setVisibility(View.VISIBLE);
            } else {
                brandingUtil.platform.colorTextView(binding.name, ColorRole.ON_SURFACE);
                binding.label.setVisibility(View.GONE);
            }
            binding.copyLink.setVisibility(View.GONE);
        } else {
            brandingUtil.platform.colorTextView(binding.name, ColorRole.ON_SURFACE);
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

        if (publicShare.getExpirationDate() > 0) {
            String expirationDescription = context.getString(
                R.string.share_expires,
                SimpleDateFormat.getDateInstance().format(new Date(publicShare.getExpirationDate()))
            );
            binding.expirationStatus.setContentDescription(expirationDescription);
            binding.expirationStatus.setVisibility(View.VISIBLE);
            binding.shareIconContainer.setOnClickListener(
                v -> listener.showShareExpirationSnackbar(publicShare)
            );
        } else {
            binding.expirationStatus.setContentDescription(null);
            binding.expirationStatus.setVisibility(View.GONE);
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
        brandingUtil = BrandingUtil.of(color, context);
        if (binding != null) {
            brandingUtil.androidx.colorPrimaryTextViewElement(binding.permissionName);
            brandingUtil.platform.colorTextView(binding.label, ColorRole.ON_SURFACE);
            brandingUtil.platform.colorImageViewBackgroundAndIcon(binding.icon);
            brandingUtil.platform.colorImageView(binding.expirationStatus, ColorRole.ON_PRIMARY_CONTAINER);
        }
    }
}
