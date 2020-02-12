package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import it.niedermann.nextcloud.exception.ExceptionHandler;


/**
 * Created by stefan on 18.04.17.
 */
public class SplashscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this, ExceptionActivity.class));

        Intent intent = new Intent(this, NotesListViewActivity.class);
        startActivity(intent);
        finish();
    }
}