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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;

public class AccountChooserViewHolder extends RecyclerView.ViewHolder {
    private final ItemAccountChooseBinding binding;

    protected AccountChooserViewHolder(ItemAccountChooseBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Account localAccount, Consumer<Account> targetAccountConsumer) {
        Glide
                .with(binding.accountItemAvatar.getContext())
                .load(new SingleSignOnUrl(localAccount.getAccountName(), localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUserName()) + "/64"))
                .placeholder(R.drawable.ic_account_circle_grey_24dp)
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.accountItemAvatar);

        binding.accountLayout.setOnClickListener((v) -> targetAccountConsumer.accept(localAccount));
        binding.accountName.setText(localAccount.getDisplayName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
    }
}