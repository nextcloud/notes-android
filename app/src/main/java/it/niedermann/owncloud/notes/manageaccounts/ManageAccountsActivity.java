package it.niedermann.owncloud.notes.manageaccounts;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.databinding.ActivityManageAccountsBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.persistence.NotesClient;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;
import it.niedermann.owncloud.notes.shared.model.ServerSettings;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

public class ManageAccountsActivity extends LockedActivity {

    private ActivityManageAccountsBinding binding;
    private ManageAccountAdapter adapter;
    private NotesDatabase db = null;
    private final List<LocalAccount> localAccounts = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAccountsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = NotesDatabase.getInstance(this);

        localAccounts.clear();
        localAccounts.addAll(db.getAccounts());

        adapter = new ManageAccountAdapter(
                (localAccount) -> SingleAccountHelper.setCurrentAccount(getApplicationContext(), localAccount.getAccountName()),
                this::onAccountDelete,
                this::onChangeNotesPath,
                this::onChangeFileSuffix
        );
        adapter.setLocalAccounts(localAccounts);
        try {
            final SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
            if (ssoAccount != null) {
                adapter.setCurrentLocalAccount(db.getLocalAccountByAccountName(ssoAccount.name));
            }
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
        binding.accounts.setAdapter(adapter);
    }

    private void onAccountDelete(@NonNull LocalAccount localAccount) {
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
    }

    private void onChangeNotesPath(@NonNull LocalAccount localAccount) {
        final NotesClient client = NotesClient.newInstance(localAccount.getPreferredApiVersion(), getApplicationContext());
        final EditText editText = new EditText(this);
        editText.setEnabled(false);
        final View wrapper = createDialogViewWrapper(editText);
        final AlertDialog dialog = new BrandedAlertDialogBuilder(this)
                .setTitle(R.string.settings_notes_path)
                .setMessage("Folder to store your notes in your  Nextcloud")
                .setView(wrapper)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_edit_save, (v, d) -> new Thread(() -> {
                    try {
                        final ServerSettings newSettings = client.putServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), new ServerSettings(editText.getText().toString(), null));
                        Toast.makeText(this, "New notes path: " + newSettings.getNotesPath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                    }
                }).start())
                .show();
        new Thread(() -> {
            try {
                final ServerSettings oldSettings = client.getServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()));
                editText.setText(oldSettings.getNotesPath());
                editText.setEnabled(true);
            } catch (Exception e) {
                dialog.dismiss();
                ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
            }
        }).start();
    }

    private void onChangeFileSuffix(@NonNull LocalAccount localAccount) {
        final NotesClient client = NotesClient.newInstance(localAccount.getPreferredApiVersion(), getApplicationContext());
        final Spinner spinner = new Spinner(this);
        spinner.setEnabled(false);
        final View wrapper = createDialogViewWrapper(spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.settings_file_suffixes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final AlertDialog dialog = new BrandedAlertDialogBuilder(this)
                .setTitle(R.string.settings_file_suffix)
                .setMessage("File extension for new notes in your Nextcloud")
                .setView(wrapper)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton("Save", (v, d) -> new Thread(() -> {
                    try {
                        final ServerSettings newSettings = client.putServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), new ServerSettings(null, spinner.getSelectedItem().toString()));
                        Toast.makeText(this, "New file suffix: " + newSettings.getNotesPath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                    }
                }).start())
                .show();
        new Thread(() -> {
            try {
                final ServerSettings oldSettings = client.getServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()));
                // TODO
//                spinner.setSelection(adapteroldSettings.getNotesPath());
                spinner.setEnabled(true);
            } catch (Exception e) {
                dialog.dismiss();
                ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
            }
        }).start();
    }

    @NonNull
    private View createDialogViewWrapper(@NonNull View view) {
        final FrameLayout wrapper = new FrameLayout(this);
        final int paddingVertical = getResources().getDimensionPixelSize(R.dimen.spacer_1x);
        final int paddingHorizontal = SDK_INT >= LOLLIPOP_MR1
                ? getDimensionFromAttribute(android.R.attr.dialogPreferredPadding)
                : getResources().getDimensionPixelSize(R.dimen.spacer_2x);
        wrapper.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        wrapper.addView(view);
        return wrapper;
    }

    @Px
    private int getDimensionFromAttribute(@SuppressWarnings("SameParameterValue") @AttrRes int attr) {
        final TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(attr, typedValue, true))
            return TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        else {
            return 0;
        }
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
    }
}
