/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;

public class AccountSwitcherViewHolder extends RecyclerView.ViewHolder {

    ItemAccountChooseBinding binding;

    public AccountSwitcherViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemAccountChooseBinding.bind(itemView);
    }

    public void bind(@NonNull Account localAccount, @NonNull Consumer<Account> onAccountClick) {
        binding.accountName.setText(localAccount.getDisplayName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        AvatarLoader.INSTANCE.load(itemView.getContext(), binding.accountItemAvatar, localAccount);
        itemView.setOnClickListener((v) -> onAccountClick.accept(localAccount));
        binding.accountContextMenu.setVisibility(View.GONE);
    }
}
