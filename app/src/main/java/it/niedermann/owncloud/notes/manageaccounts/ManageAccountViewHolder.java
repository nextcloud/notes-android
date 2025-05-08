/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.manageaccounts;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.shared.util.ApiVersionUtil.getPreferredApiVersion;

import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;

public class ManageAccountViewHolder extends RecyclerView.ViewHolder {

    private final ItemAccountChooseBinding binding;

    public ManageAccountViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemAccountChooseBinding.bind(itemView);
    }

    public void bind(
            @NonNull Account localAccount,
            @NonNull IManageAccountsCallback callback,
            boolean isCurrentAccount
    ) {
        binding.accountName.setText(localAccount.getUserName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        AvatarLoader.INSTANCE.load(itemView.getContext(), binding.accountItemAvatar, localAccount);
        itemView.setOnClickListener((v) -> callback.onSelect(localAccount));
        binding.accountContextMenu.setVisibility(VISIBLE);
        binding.accountContextMenu.setOnClickListener((v) -> {
            final var popup = new PopupMenu(itemView.getContext(), v);
            popup.inflate(R.menu.menu_account);

            final var preferredApiVersion = getPreferredApiVersion(localAccount.getApiVersion());

            if (preferredApiVersion == null || !preferredApiVersion.supportsFileSuffixChange()) {
                popup.getMenu().removeItem(popup.getMenu().findItem(R.id.file_suffix).getItemId());
            }

            if (preferredApiVersion == null || !preferredApiVersion.supportsNotesPathChange()) {
                popup.getMenu().removeItem(popup.getMenu().findItem(R.id.notes_path).getItemId());
            }

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.notes_path) {
                    callback.onChangeNotesPath(localAccount);
                    return true;
                } else if (item.getItemId() == R.id.file_suffix) {
                    callback.onChangeFileSuffix(localAccount);
                    return true;
                } else if (item.getItemId() == R.id.delete) {
                    callback.onDelete(localAccount);
                    return true;
                }
                return false;
            });
            popup.show();
        });
        if (isCurrentAccount) {
            binding.currentAccountIndicator.setVisibility(VISIBLE);
            final var util = BrandingUtil.of(localAccount.getColor(), itemView.getContext());
            util.notes.colorLayerDrawable((LayerDrawable) binding.currentAccountIndicator.getDrawable(), R.id.area, localAccount.getColor());
        } else {
            binding.currentAccountIndicator.setVisibility(GONE);
        }
    }
}
