package it.niedermann.owncloud.notes.manageaccounts;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.List;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.databinding.ActivityManageAccountsBinding;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

public class ManageAccountsActivity extends LockedActivity {

    private ActivityManageAccountsBinding binding;
    private ManageAccountAdapter adapter;
    private NotesDatabase db = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAccountsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = NotesDatabase.getInstance(this);

        List<LocalAccount> localAccounts = db.getAccounts();

        adapter = new ManageAccountAdapter((localAccount) -> SingleAccountHelper.setCurrentAccount(getApplicationContext(), localAccount.getAccountName()), (localAccount) -> {
            db.deleteAccount(localAccount);
            for (LocalAccount temp : localAccounts) {
                if (temp.getId() == localAccount.getId()) {
                    localAccounts.remove(temp);
                    break;
                }
            }
            if (localAccounts.size() > 0) {
                SingleAccountHelper.setCurrentAccount(getApplicationContext(), localAccounts.get(0).getAccountName());
                adapter.setCurrentLocalAccount(localAccounts.get(0));
            } else {
                setResult(AppCompatActivity.RESULT_FIRST_USER);
                finish();
            }
        });
        adapter.setLocalAccounts(localAccounts);
        try {
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
            if (ssoAccount != null) {
                adapter.setCurrentLocalAccount(db.getLocalAccountByAccountName(ssoAccount.name));
            }
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
        binding.accounts.setAdapter(adapter);
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
    }
}
