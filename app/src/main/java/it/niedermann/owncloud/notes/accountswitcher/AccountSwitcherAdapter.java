package it.niedermann.owncloud.notes.accountswitcher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LocalAccount;

public class AccountSwitcherAdapter extends RecyclerView.Adapter<AccountSwitcherViewHolder> {

    @NonNull
    private final List<LocalAccount> localAccounts = new ArrayList<>();
    @NonNull
    private final Consumer<LocalAccount> onAccountClick;

    public AccountSwitcherAdapter(@NonNull Consumer<LocalAccount> onAccountClick) {
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

    public void setLocalAccounts(@NonNull List<LocalAccount> localAccounts) {
        this.localAccounts.clear();
        this.localAccounts.addAll(localAccounts);
        notifyDataSetChanged();
    }
}
