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
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.owncloud.android.lib.resources.shares.OCShare;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedViewHolder;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemShareShareBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;
import it.niedermann.owncloud.notes.shared.util.FilesSpecificViewThemeUtils;

public class ShareViewHolder extends BrandedViewHolder {
    private ItemShareShareBinding binding;
    private Account account;
    private Context context;
    @ColorInt
    private int color;

    public ShareViewHolder(@NonNull View itemView) {
        super(itemView);
        bindBranding();
    }

    public ShareViewHolder(ItemShareShareBinding binding,
                           Account account,
                           Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.account = account;
        this.context = context;
        bindBranding();
    }

    public void bind(OCShare share, ShareeListAdapterListener listener) {
        String accountUserName = account.getUserName();
        String name = share.getSharedWithDisplayName();
        binding.icon.setTag(null);
        final var shareType = share.getShareType();
        if (shareType == null) {
            return;
        }

        final var viewThemeUtils = FilesSpecificViewThemeUtils.INSTANCE;

        switch (shareType) {
            case GROUP:
                name = context.getString(R.string.share_group_clarification, name);
                viewThemeUtils.createAvatar(share.getShareType(), binding.icon, context);
                BrandingUtil.of(color, context).platform.colorImageViewBackgroundAndIcon(binding.icon);
                break;
            case ROOM:
                name = context.getString(R.string.share_room_clarification, name);
                viewThemeUtils.createAvatar(share.getShareType(), binding.icon, context);
                BrandingUtil.of(color, context).platform.colorImageViewBackgroundAndIcon(binding.icon);
                break;
            case CIRCLE:
                viewThemeUtils.createAvatar(share.getShareType(), binding.icon, context);
                BrandingUtil.of(color, context).platform.colorImageViewBackgroundAndIcon(binding.icon);
                break;
            case FEDERATED:
                name = context.getString(R.string.share_remote_clarification, name);
                setImage(binding.icon, share.getShareWith(), R.drawable.ic_account_circle_grey_24dp);
                BrandingUtil.of(color, context).platform.colorImageViewBackgroundAndIcon(binding.icon);
                break;
            case USER:
                binding.icon.setTag(share.getShareWith());

                if (share.getShareWith() != null) {
                    AvatarLoader.INSTANCE.load(context, binding.icon, account, share.getShareWith());
                }

                // binding.icon.setOnClickListener(v -> listener.showProfileBottomSheet(user, share.getShareWith()));
                break;
            default:
                setImage(binding.icon, name, R.drawable.ic_account_circle_grey_24dp);
                BrandingUtil.of(color, context).platform.colorImageViewBackgroundAndIcon(binding.icon);
                break;
        }

        binding.name.setText(name);

        if (accountUserName.equalsIgnoreCase(share.getShareWith()) ||
            accountUserName.equalsIgnoreCase(share.getUserId())) {
            binding.overflowMenu.setVisibility(View.VISIBLE);

            String permissionName = SharingMenuHelper.getPermissionName(context, share);
            setPermissionName(permissionName);

            // bind listener to edit privileges
            binding.overflowMenu.setOnClickListener(v -> listener.showSharingMenuActionSheet(share));
            binding.shareNameLayout.setOnClickListener(v -> listener.showPermissionsDialog(share));
        } else {
            binding.overflowMenu.setVisibility(View.GONE);
        }
    }

    private void setPermissionName(String permissionName) {
        if (!TextUtils.isEmpty(permissionName)) {
            binding.permissionName.setText(permissionName);
            binding.permissionName.setVisibility(View.VISIBLE);
        } else {
            binding.permissionName.setVisibility(View.GONE);
        }
    }

    private void setImage(ImageView avatar, String name, @DrawableRes int fallback) {
        try {
            AvatarLoader.INSTANCE.load(context, avatar, account, name);
        } catch (StringIndexOutOfBoundsException e) {
            avatar.setImageResource(fallback);
        }
    }

    @Override
    public void applyBrand(int color) {
        this.color = color;
        final var util = BrandingUtil.of(this.color, context);
        if (binding != null) {
            util.androidx.colorPrimaryTextViewElement(binding.permissionName);
        }
    }
}
