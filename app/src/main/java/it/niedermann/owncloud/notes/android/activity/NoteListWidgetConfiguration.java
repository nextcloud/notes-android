package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;

public class NoteListWidgetConfiguration extends AppCompatActivity {
    private static final String TAG = Activity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();

        if (intent.getExtras() == null) {
            finish();
            return;
        }

        int mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                        AppWidgetManager.INVALID_APPWIDGET_ID);

        if (!(NoteServerSyncHelper.isConfigured(this))) {
            Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show();

            // TODO Present user with app login screen
            Log.w(TAG, "onCreate: user not logged in");
        } else {
            Intent retIntent = new Intent(this, NoteListWidget.class);
            retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            retIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            sendBroadcast(retIntent);
            setResult(RESULT_OK, retIntent);
        }

        finish();
    }
}
