/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.account;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;

public class AccountChooserAdapter extends RecyclerView.Adapter<AccountChooserViewHolder> {

    @NonNull
    private final List<Account> localAccounts;
    @NonNull
    private final Consumer<Account> targetAccountConsumer;

    public AccountChooserAdapter(@NonNull List<Account> localAccounts, @NonNull Consumer<Account> targetAccountConsumer) {
        super();
        this.localAccounts = localAccounts;
        this.targetAccountConsumer = targetAccountConsumer;
    }

    @NonNull
    @Override
    public AccountChooserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountChooserViewHolder(ItemAccountChooseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountChooserViewHolder holder, int position) {
        holder.bind(localAccounts.get(position), targetAccountConsumer);
    }

    @Override
    public int getItemCount() {
        return localAccounts.size();
    }

}
