package it.niedermann.owncloud.notes.android.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.ExceptionHandler;
import it.niedermann.owncloud.notes.util.Notes;

public abstract class LockedActivity extends AppCompatActivity {

    private static final String TAG = LockedActivity.class.getCanonicalName();

    private static final int REQUEST_CODE_UNLOCK = 100;

    @Override
    protected void onResume() {
        super.onResume();

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (Notes.isLocked()) {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    Intent i = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.unlock_notes), null);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(i, REQUEST_CODE_UNLOCK);
                } else {
                    Log.e(TAG, "Keyguard manager is null");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_UNLOCK: {
                if (resultCode == RESULT_OK) {
                    Log.v(TAG, "Successfully unlocked device");
                    Notes.unlock();
                } else {
                    finish();
                }
                break;
            }
        }
    }
}
