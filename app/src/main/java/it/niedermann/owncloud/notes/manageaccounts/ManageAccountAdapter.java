/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.manageaccounts;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.entity.Account;

public class ManageAccountAdapter extends RecyclerView.Adapter<ManageAccountViewHolder> {

    @Nullable
    private Account currentLocalAccount = null;
    @NonNull
    private final List<Account> localAccounts = new ArrayList<>();
    @NonNull
    private final IManageAccountsCallback callback;

    public ManageAccountAdapter(@NonNull IManageAccountsCallback callback) {
        this.callback = callback;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return localAccounts.get(position).getId();
    }

    @NonNull
    @Override
    public ManageAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ManageAccountViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ManageAccountViewHolder holder, int position) {
        final var localAccount = localAccounts.get(position);
        holder.bind(localAccount, callback, currentLocalAccount != null && currentLocalAccount.getId() == localAccount.getId());
    }

    @Override
    public int getItemCount() {
        return localAccounts.size();
    }

    public void setLocalAccounts(@NonNull List<Account> localAccounts) {
        this.localAccounts.clear();
        this.localAccounts.addAll(localAccounts);
        notifyDataSetChanged();
    }

    public void setCurrentLocalAccount(@Nullable Account currentLocalAccount) {
        this.currentLocalAccount = currentLocalAccount;
        notifyDataSetChanged();
    }
}
