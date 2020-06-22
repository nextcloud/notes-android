package it.niedermann.owncloud.notes.shared.account;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;

public class AccountChooserAdapter extends RecyclerView.Adapter<AccountChooserViewHolder> {

    @NonNull
    private final List<LocalAccount> localAccounts;
    @NonNull
    private final Consumer<LocalAccount> targetAccountConsumer;

    public AccountChooserAdapter(@NonNull List<LocalAccount> localAccounts, @NonNull Consumer<LocalAccount> targetAccountConsumer) {
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
