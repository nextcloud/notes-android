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
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.glide.SingleSignOnOriginHeader;
import it.niedermann.owncloud.notes.model.LocalAccount;

import static it.niedermann.owncloud.notes.android.fragment.AccountChooserAdapter.AccountChooserViewHolder;

public class AccountChooserAdapter extends RecyclerView.Adapter<AccountChooserViewHolder> {

    @NonNull
    private final List<LocalAccount> localAccounts;
    @NonNull
    private final MoveAccountListener moveAccountListener;

    AccountChooserAdapter(@NonNull List<LocalAccount> localAccounts, @NonNull MoveAccountListener moveAccountListener) {
        super();
        this.localAccounts = localAccounts;
        this.moveAccountListener = moveAccountListener;
    }

    @NonNull
    @Override
    public AccountChooserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_choose, parent, false);
        return new AccountChooserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountChooserViewHolder holder, int position) {
        holder.bind(localAccounts.get(position), moveAccountListener);
    }

    @Override
    public int getItemCount() {
        return localAccounts.size();
    }

    static class AccountChooserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAccountChooseBinding binding;

        private AccountChooserViewHolder(View view) {
            super(view);
            binding = ItemAccountChooseBinding.bind(view);
        }

        public void bind(LocalAccount localAccount, MoveAccountListener moveAccountListener) {
            Glide
                    .with(binding.accountItemAvatar.getContext())
                    .load(new GlideUrl(localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUserName()) + "/64", new SingleSignOnOriginHeader(localAccount)))
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.accountItemAvatar);

            binding.accountLayout.setOnClickListener((v) -> moveAccountListener.moveToAccount(localAccount));
            binding.accountName.setText(localAccount.getUserName());
            binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        }
    }

    public interface MoveAccountListener {
        void moveToAccount(LocalAccount account);
    }
}
