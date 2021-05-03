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
    private final Consumer<Account> onAccountClick;
    @NonNull
    private final Consumer<Account> onAccountDelete;
    @NonNull
    Consumer<Account> onChangeNotesPath;
    @NonNull
    Consumer<Account> onChangeFileSuffix;

    public ManageAccountAdapter(@NonNull Consumer<Account> onAccountClick,
                                @NonNull Consumer<Account> onAccountDelete,
                                @NonNull Consumer<Account> onChangeNotesPath,
                                @NonNull Consumer<Account> onChangeFileSuffix) {
        this.onAccountClick = onAccountClick;
        this.onAccountDelete = onAccountDelete;
        this.onChangeNotesPath = onChangeNotesPath;
        this.onChangeFileSuffix = onChangeFileSuffix;
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
        final Account localAccount = localAccounts.get(position);
        holder.bind(localAccount, (localAccountClicked) -> {
            setCurrentLocalAccount(localAccountClicked);
            onAccountClick.accept(localAccountClicked);
        }, onAccountDelete, onChangeNotesPath, onChangeFileSuffix, currentLocalAccount != null && currentLocalAccount.getId() == localAccount.getId());
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
