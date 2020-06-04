package it.niedermann.owncloud.notes.accountswitcher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    @Nullable
    private final Consumer<LocalAccount> onAccountDelete;

    public AccountSwitcherAdapter(@NonNull Consumer<LocalAccount> onAccountClick, @Nullable Consumer<LocalAccount> onAccountDelete) {
        this.onAccountClick = onAccountClick;
        this.onAccountDelete = onAccountDelete;
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
        holder.bind(localAccounts.get(position), onAccountClick, (localAccount -> {
            if (onAccountDelete != null) {
                for (int i = 0; i < localAccounts.size(); i++) {
                    if (localAccounts.get(i).getId() == localAccount.getId()) {
                        localAccounts.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
                onAccountDelete.accept(localAccount);
            }
        }));
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
