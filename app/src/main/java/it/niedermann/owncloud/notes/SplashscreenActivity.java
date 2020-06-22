package it.niedermann.owncloud.notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import it.niedermann.owncloud.notes.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.main.MainActivity;


/**
 * Created by stefan on 18.04.17.
 */
public class SplashscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}