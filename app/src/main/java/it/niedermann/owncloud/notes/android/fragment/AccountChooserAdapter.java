package it.niedermann.owncloud.notes.android.fragment;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LocalAccount;

public class AccountChooserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private final List<LocalAccount> localAccounts;
    @NonNull
    private final AccountChooserListener accountChooserListener;
    @NonNull
    private final Context context;

    AccountChooserAdapter(@NonNull List<LocalAccount> localAccounts, @NonNull AccountChooserListener accountChooserListener, @NonNull Context context) {
        super();
        this.localAccounts = localAccounts;
        this.accountChooserListener = accountChooserListener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_choose, parent, false);
        return new AccountChooserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LocalAccount localAccount = localAccounts.get(position);
        AccountChooserViewHolder accountChooserViewHolder = (AccountChooserViewHolder) holder;
        accountChooserViewHolder.accountLayout.setOnClickListener((v) -> accountChooserListener.onAccountChosen(localAccount));

        Glide
                .with(context)
                .load(localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUserName()) + "/64")
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(accountChooserViewHolder.avatar);

        accountChooserViewHolder.username.setText(localAccount.getAccountName());
    }

    @Override
    public int getItemCount() {
        return localAccounts.size();
    }

    static class AccountChooserViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.accountLayout)
        LinearLayout accountLayout;
        @BindView(R.id.accountItemAvatar)
        ImageView avatar;
        @BindView(R.id.accountItemLabel)
        TextView username;

        private AccountChooserViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface AccountChooserListener {
        void onAccountChosen(LocalAccount account);
    }
}
