package it.niedermann.owncloud.notes.manageaccounts;

import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemAccountChooseBinding;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.branding.BrandingUtil.applyBrandToLayerDrawable;

public class ManageAccountViewHolder extends RecyclerView.ViewHolder {

    private final ItemAccountChooseBinding binding;

    public ManageAccountViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemAccountChooseBinding.bind(itemView);
    }

    public void bind(
            @NonNull LocalAccount localAccount,
            @NonNull Consumer<LocalAccount> onAccountClick,
            @NonNull Consumer<LocalAccount> onAccountDelete,
            @NonNull Consumer<LocalAccount> onChangeNotesPath,
            @NonNull Consumer<LocalAccount> onChangeFileSuffix,
            boolean isCurrentAccount
    ) {
        binding.accountName.setText(localAccount.getUserName());
        binding.accountHost.setText(Uri.parse(localAccount.getUrl()).getHost());
        Glide.with(itemView.getContext())
                .load(new SingleSignOnUrl(localAccount.getAccountName(), localAccount.getUrl() + "/index.php/avatar/" + Uri.encode(localAccount.getUserName()) + "/64"))
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.accountItemAvatar);
        itemView.setOnClickListener((v) -> onAccountClick.accept(localAccount));
        binding.accountContextMenu.setVisibility(VISIBLE);
        binding.accountContextMenu.setOnClickListener((v) -> {
            final PopupMenu popup = new PopupMenu(itemView.getContext(), v);
            popup.inflate(R.menu.menu_account);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.notes_path) {
                    onChangeNotesPath.accept(localAccount);
                    return true;
                } else if (item.getItemId() == R.id.file_suffix) {
                    onChangeFileSuffix.accept(localAccount);
                    return true;
                } else if (item.getItemId() == R.id.delete) {
                    onAccountDelete.accept(localAccount);
                    return true;
                }
                return false;
            });
            popup.show();
        });
        if (isCurrentAccount) {
            binding.currentAccountIndicator.setVisibility(VISIBLE);
            applyBrandToLayerDrawable((LayerDrawable) binding.currentAccountIndicator.getDrawable(), R.id.area, localAccount.getColor());
        } else {
            binding.currentAccountIndicator.setVisibility(GONE);
        }
    }
}
