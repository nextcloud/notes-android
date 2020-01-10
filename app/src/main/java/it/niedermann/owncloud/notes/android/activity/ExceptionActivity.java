package it.niedermann.owncloud.notes.android.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.ExceptionUtil;

public class ExceptionActivity extends AppCompatActivity {

    Throwable throwable;

    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.stacktrace)
    TextView stacktrace;

    public static final String KEY_THROWABLE = "T";

    @SuppressLint("SetTextI18n") // only used for logging
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_exception);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));
        throwable.printStackTrace();
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.simple_error));
        this.message.setText(throwable.getMessage());
        this.stacktrace.setText(ExceptionUtil.getDebugInfos(this, throwable));
    }

    @OnClick(R.id.copy)
    void copyStacktraceToClipboard() {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + this.stacktrace.getText() + "\n```");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.close)
    void close() {
        finish();
    }
}
