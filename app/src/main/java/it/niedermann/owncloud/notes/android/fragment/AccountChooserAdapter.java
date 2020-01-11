package it.niedermann.owncloud.notes.android.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LocalAccount;

public class AccountChooserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private List<LocalAccount> localAccounts;
    @NonNull
    private AccountChooserDialogFragment.AccountChooserListener accountChooserListener;
    @Nullable
    private Context context;

    AccountChooserAdapter(@NonNull List<LocalAccount> localAccounts, @NonNull AccountChooserDialogFragment.AccountChooserListener accountChooserListener, @Nullable Context context) {
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
        accountChooserViewHolder.accountLayout.setOnClickListener((v) -> {
            accountChooserListener.onAccountChosen(localAccount);
        });

//            if (context != null) {
//                try {
////                    ViewUtil.addAvatar(context, acHolder.avatar, SingleAccountHelper.getCurrentSingleSignOnAccount(context).url, ac.getUser().getUid(), R.drawable.ic_person_grey600_24dp);
//                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
//                    e.printStackTrace();
//                }
//            }

        accountChooserViewHolder.username.setText(localAccount.getUserName() + localAccount.getUrl());
    }

    @Override
    public int getItemCount() {
        return localAccounts.size();
    }

    static class AccountChooserViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.accountLayout)
        RelativeLayout accountLayout;
        @BindView(R.id.accountItemAvatar)
        ImageView avatar;
        @BindView(R.id.accountItemLabel)
        TextView username;

        private AccountChooserViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
