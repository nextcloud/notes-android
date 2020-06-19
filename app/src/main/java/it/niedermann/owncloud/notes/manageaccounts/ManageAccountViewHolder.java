package it.niedermann.owncloud.notes.manageaccounts;

import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.glide.SingleSignOnOriginHeader;
import it.niedermann.owncloud.notes.model.LocalAccount;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.branding.BrandingUtil.applyBrandToLayerDrawable;

public class ManageAccountViewHolder extends RecyclerView.ViewHolder {

    private ItemAccountChooseBinding binding;

    public ManageAccountViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemAccountChooseBinding.bind(itemView);
    }

    public void bind(@NonNull LocalAccount localAccount, @NonNull Consumer<LocalAccount> onAccountClick, @Nullable Consumer<LocalAccount> onAccountDelete, boolean isCurrentAccount) {
        binding.accountName.setText(localAccount.getUserName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        Glide.with(itemView.getContext())
                .load(new GlideUrl(localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUserName()) + "/64", new SingleSignOnOriginHeader(localAccount)))
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.accountItemAvatar);
        itemView.setOnClickListener((v) -> onAccountClick.accept(localAccount));
        if (onAccountDelete == null) {
            binding.delete.setVisibility(GONE);
        } else {
            binding.delete.setVisibility(VISIBLE);
            binding.delete.setOnClickListener((v) -> onAccountDelete.accept(localAccount));
        }
        if (isCurrentAccount) {
            binding.currentAccountIndicator.setVisibility(VISIBLE);
            applyBrandToLayerDrawable((LayerDrawable) binding.currentAccountIndicator.getDrawable(), R.id.area, localAccount.getColor());
        } else {
            binding.currentAccountIndicator.setVisibility(GONE);
        }
    }
}
