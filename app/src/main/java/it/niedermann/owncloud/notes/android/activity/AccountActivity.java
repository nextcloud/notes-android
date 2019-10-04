package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ExceptionHandler;

/**
 * Allows switching the account
 * Created by stefan on 22.09.15.
 */
public class AccountActivity extends AppCompatActivity {
    public static final String SETTINGS_KEY_ETAG = "notes_last_etag";
    public static final String SETTINGS_KEY_LAST_MODIFIED = "notes_last_modified";

    private NoteSQLiteOpenHelper db;

    @BindView(R.id.accountsLayout)
    LinearLayout accountsLayout;

    @BindView(R.id.addAccount)
    Button addAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);

        db = NoteSQLiteOpenHelper.getInstance(this);

        addAccount.setOnClickListener((btn) -> {
            try {
                AccountImporter.pickNewAccount(this);
            } catch (NextcloudFilesAppNotInstalledException e1) {
                UiExceptionManager.showDialogForException(this, e1);
                Log.w(NotesListViewActivity.class.toString(), "=============================================================");
                Log.w(NotesListViewActivity.class.toString(), "Nextcloud app is not installed. Cannot choose account");
                e1.printStackTrace();
            } catch (AndroidGetAccountsPermissionNotGranted e2) {
                AccountImporter.requestAndroidAccountPermissionsAndPickAccount(this);
            }
        });

        for(LocalAccount account: db.getAccounts()) {
            View v = getLayoutInflater().inflate(R.layout.item_account, null);
            ((TextView) v.findViewById(R.id.accountItemLabel)).setText(account.getUserName());
            v.setOnClickListener(generateOnClickListenerFor(account));
            accountsLayout.addView(v);
        }
    }

    private View.OnClickListener generateOnClickListenerFor(LocalAccount account) {
        return v -> {
            SingleAccountHelper.setCurrentAccount(getApplicationContext(), account.getAccountName());
            db.getNoteServerSyncHelper().updateAccount();
            finish();
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        AccountImporter.onActivityResult(requestCode, resultCode, data, this, (SingleSignOnAccount account) -> {
            Log.v("Notes", "Added account: " + "name:" + account.name + ", " + account.url + ", userId" + account.userId);
            ;
            LocalAccount generatedAccount = db.getAccount(db.addAccount(account.url, account.userId, account.name));
            SingleAccountHelper.setCurrentAccount(getApplicationContext(), account.name);
            db.getNoteServerSyncHelper().updateAccount();

            View v = getLayoutInflater().inflate(R.layout.item_account, null);
            ((TextView) v.findViewById(R.id.accountItemLabel)).setText(generatedAccount.getUserName());
            v.setOnClickListener(generateOnClickListenerFor(generatedAccount));
            accountsLayout.addView(v);
        });
    }
}
