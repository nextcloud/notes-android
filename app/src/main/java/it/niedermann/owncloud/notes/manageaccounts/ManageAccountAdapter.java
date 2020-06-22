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
import it.niedermann.owncloud.notes.shared.model.LocalAccount;

public class ManageAccountAdapter extends RecyclerView.Adapter<ManageAccountViewHolder> {

    @Nullable
    private LocalAccount currentLocalAccount = null;
    @NonNull
    private final List<LocalAccount> localAccounts = new ArrayList<>();
    @NonNull
    private final Consumer<LocalAccount> onAccountClick;
    @Nullable
    private final Consumer<LocalAccount> onAccountDelete;

    public ManageAccountAdapter(@NonNull Consumer<LocalAccount> onAccountClick, @Nullable Consumer<LocalAccount> onAccountDelete) {
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
    public ManageAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ManageAccountViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ManageAccountViewHolder holder, int position) {
        final LocalAccount localAccount = localAccounts.get(position);
        holder.bind(localAccount, (localAccountClicked) -> {
            setCurrentLocalAccount(localAccountClicked);
            onAccountClick.accept(localAccountClicked);
        }, (localAccountToDelete -> {
            if (onAccountDelete != null) {
                for (int i = 0; i < localAccounts.size(); i++) {
                    if (localAccounts.get(i).getId() == localAccountToDelete.getId()) {
                        localAccounts.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
                onAccountDelete.accept(localAccountToDelete);
            }
        }), currentLocalAccount != null && currentLocalAccount.getId() == localAccount.getId());
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

    public void setCurrentLocalAccount(@Nullable LocalAccount currentLocalAccount) {
        this.currentLocalAccount = currentLocalAccount;
        notifyDataSetChanged();
    }
}
