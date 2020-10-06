package it.niedermann.owncloud.notes.accountswitcher;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import it.niedermann.android.glidesso.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;

public class AccountSwitcherViewHolder extends RecyclerView.ViewHolder {

    ItemAccountChooseBinding binding;

    public AccountSwitcherViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemAccountChooseBinding.bind(itemView);
    }

    public void bind(@NonNull LocalAccountEntity localAccount, @NonNull Consumer<LocalAccountEntity> onAccountClick) {
        binding.accountName.setText(localAccount.getUsername());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        Glide.with(itemView.getContext())
                .load(new SingleSignOnUrl(localAccount.getAccountName(), localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUsername()) + "/64"))
                .placeholder(R.drawable.ic_account_circle_grey_24dp)
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.accountItemAvatar);
        itemView.setOnClickListener((v) -> onAccountClick.accept(localAccount));
        binding.delete.setVisibility(View.GONE);
    }
}
