package it.niedermann.owncloud.notes.share.adapter.holder;


import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.owncloud.android.lib.resources.shares.OCShare;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemShareShareBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;
import it.niedermann.owncloud.notes.shared.user.User;

public class ShareViewHolder extends RecyclerView.ViewHolder {
    private ItemShareShareBinding binding;
    private User user;
    private Account account;
    private Context context;

    public ShareViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public ShareViewHolder(ItemShareShareBinding binding,
                           Account account,
                           User user,
                           Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.user = user;
        this.account = account;
        this.context = context;
    }

    public void bind(OCShare share,
                     ShareeListAdapterListener listener) {
        String userId = String.valueOf(account.getId());
        String name = share.getSharedWithDisplayName();
        binding.icon.setTag(null);

        switch (share.getShareType()) {
            case GROUP:
                name = context.getString(R.string.share_group_clarification, name);
                // viewThemeUtils.files.createAvatar(share.getShareType(), binding.icon, context);
                break;
            case ROOM:
                name = context.getString(R.string.share_room_clarification, name);
                // viewThemeUtils.files.createAvatar(share.getShareType(), binding.icon, context);
                break;
            case CIRCLE:
                // viewThemeUtils.files.createAvatar(share.getShareType(), binding.icon, context);
                break;
            case FEDERATED:
                name = context.getString(R.string.share_remote_clarification, name);
                setImage(binding.icon, share.getSharedWithDisplayName(), R.drawable.ic_account_circle_grey_24dp);
                break;
            case USER:
                binding.icon.setTag(share.getShareWith());

                Glide.with(context)
                        .load(new SingleSignOnUrl(account.getAccountName(), account.getUrl() + "/index.php/avatar/" + Uri.encode(account.getUserName()) + "/64"))
                        .placeholder(R.drawable.ic_account_circle_grey_24dp)
                        .error(R.drawable.ic_account_circle_grey_24dp)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.icon);

                binding.icon.setOnClickListener(v -> listener.showProfileBottomSheet(user, share.getShareWith()));
            default:
                setImage(binding.icon, name, R.drawable.ic_account_circle_grey_24dp);
                break;
        }

        binding.name.setText(name);

        if (share.getShareWith().equalsIgnoreCase(userId) || share.getUserId().equalsIgnoreCase(userId)) {
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
            // avatar.setImageDrawable(TextDrawable.createNamedAvatar(name, avatarRadiusDimension));
        } catch (StringIndexOutOfBoundsException e) {
            avatar.setImageResource(fallback);
        }
    }
}
