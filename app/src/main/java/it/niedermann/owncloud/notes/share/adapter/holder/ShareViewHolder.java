package it.niedermann.owncloud.notes.share.adapter.holder;


import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.resources.shares.OCShare;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemShareShareBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;
import it.niedermann.owncloud.notes.shared.util.FilesSpecificViewThemeUtils;

public class ShareViewHolder extends RecyclerView.ViewHolder {
    private ItemShareShareBinding binding;
    private Account account;
    private Context context;

    public ShareViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public ShareViewHolder(ItemShareShareBinding binding,
                           Account account,
                           Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.account = account;
        this.context = context;
    }

    public void bind(OCShare share, ShareeListAdapterListener listener) {
        String accountName = account.getDisplayName();
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
                break;
            case ROOM:
                name = context.getString(R.string.share_room_clarification, name);
                viewThemeUtils.createAvatar(share.getShareType(), binding.icon, context);
                break;
            case CIRCLE:
                viewThemeUtils.createAvatar(share.getShareType(), binding.icon, context);
                break;
            case FEDERATED:
                name = context.getString(R.string.share_remote_clarification, name);
                setImage(binding.icon, share.getSharedWithDisplayName(), R.drawable.ic_account_circle_grey_24dp);
                break;
            case USER:
                binding.icon.setTag(share.getShareWith());

                if (share.getSharedWithDisplayName() != null) {
                    AvatarLoader.INSTANCE.load(context, binding.icon, account, share.getSharedWithDisplayName());
                }

                // binding.icon.setOnClickListener(v -> listener.showProfileBottomSheet(user, share.getShareWith()));
            default:
                setImage(binding.icon, name, R.drawable.ic_account_circle_grey_24dp);
                break;
        }

        binding.name.setText(name);

        if (accountName == null) {
            binding.overflowMenu.setVisibility(View.GONE);
            return;
        }

        if (accountName.equalsIgnoreCase(share.getShareWith()) || accountName.equalsIgnoreCase(share.getUserId())) {
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
            AvatarLoader.INSTANCE.load(context, avatar, account);
        } catch (StringIndexOutOfBoundsException e) {
            avatar.setImageResource(fallback);
        }
    }
}
