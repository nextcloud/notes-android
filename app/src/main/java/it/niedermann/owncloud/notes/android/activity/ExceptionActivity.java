package it.niedermann.owncloud.notes.android.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import it.niedermann.nextcloud.exception.ExceptionUtil;
import it.niedermann.owncloud.notes.R;

import static it.niedermann.nextcloud.exception.ExceptionHandler.KEY_THROWABLE;

public class ExceptionActivity extends AppCompatActivity {

    Throwable throwable;

    private TextView stacktrace;

    @SuppressLint("SetTextI18n") // only used for logging
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_exception);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView message = findViewById(R.id.message);
        stacktrace = findViewById(R.id.stacktrace);

        findViewById(R.id.copy).setOnClickListener((v) -> copyStacktraceToClipboard());
        findViewById(R.id.close).setOnClickListener((v) -> close());

        setSupportActionBar(toolbar);
        throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));
        throwable.printStackTrace();
        toolbar.setTitle(getString(R.string.simple_error));
        message.setText(throwable.getMessage());
        stacktrace.setText(ExceptionUtil.getDebugInfos(this, throwable));
    }


    void copyStacktraceToClipboard() {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + this.stacktrace.getText() + "\n```");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    void close() {
        finish();
    }
}
