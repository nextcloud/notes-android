/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.account;

import android.net.Uri;

import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;

public class AccountChooserViewHolder extends RecyclerView.ViewHolder {
    private final ItemAccountChooseBinding binding;

    protected AccountChooserViewHolder(ItemAccountChooseBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Account localAccount, Consumer<Account> targetAccountConsumer) {
        AvatarLoader.INSTANCE.load(binding.accountItemAvatar.getContext(), binding.accountItemAvatar, localAccount);
        binding.accountLayout.setOnClickListener((v) -> targetAccountConsumer.accept(localAccount));
        binding.accountName.setText(localAccount.getDisplayName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
    }
}