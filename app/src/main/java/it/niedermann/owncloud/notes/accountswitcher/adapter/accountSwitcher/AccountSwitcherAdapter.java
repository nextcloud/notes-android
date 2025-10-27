/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher.adapter.accountSwitcher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.entity.Account;

public class AccountSwitcherAdapter extends RecyclerView.Adapter<AccountSwitcherViewHolder> {

    @NonNull
    private final List<Account> localAccounts = new ArrayList<>();
    @NonNull
    private final Consumer<Account> onAccountClick;

    public AccountSwitcherAdapter(@NonNull Consumer<Account> onAccountClick) {
        this.onAccountClick = onAccountClick;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return localAccounts.get(position).getId();
    }

    @NonNull
    @Override
    public AccountSwitcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountSwitcherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountSwitcherViewHolder holder, int position) {
        holder.bind(localAccounts.get(position), onAccountClick);
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
}
