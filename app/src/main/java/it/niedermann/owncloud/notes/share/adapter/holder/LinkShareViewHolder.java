package it.niedermann.owncloud.notes.share.adapter.holder;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.lib.resources.files.FileDownloadLimit;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemShareLinkShareBinding;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;

public class LinkShareViewHolder extends RecyclerView.ViewHolder {
    private ItemShareLinkShareBinding binding;
    private Context context;

    public LinkShareViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public LinkShareViewHolder(ItemShareLinkShareBinding binding, Context context) {
        this(binding.getRoot());
        this.binding = binding;
        this.context = context;
    }

    public void bind(OCShare publicShare, ShareeListAdapterListener listener) {
        if (ShareType.EMAIL == publicShare.getShareType()) {
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

            // viewThemeUtils.platform.colorImageViewBackgroundAndIcon(binding.icon);
        }

        FileDownloadLimit downloadLimit = publicShare.getFileDownloadLimit();
        if (downloadLimit != null && downloadLimit.getLimit() > 0) {
            int remaining = downloadLimit.getLimit() - downloadLimit.getCount();
            String text = context.getResources().getQuantityString(R.plurals.share_download_limit_description, remaining, remaining);

            binding.subline.setText(text);
            binding.subline.setVisibility(View.VISIBLE);
        } else {
            binding.subline.setVisibility(View.GONE);
        }

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
            // viewThemeUtils.androidx.colorPrimaryTextViewElement(binding.permissionName);
        } else {
            binding.permissionName.setVisibility(View.GONE);
        }
    }
}
