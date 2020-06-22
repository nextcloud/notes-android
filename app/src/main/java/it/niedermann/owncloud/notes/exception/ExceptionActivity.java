package it.niedermann.owncloud.notes.exception;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ActivityExceptionBinding;

import static it.niedermann.owncloud.notes.exception.ExceptionHandler.KEY_THROWABLE;


public class ExceptionActivity extends AppCompatActivity {

    private ActivityExceptionBinding binding;

    @SuppressLint("SetTextI18n") // only used for logging
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityExceptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.copy.setOnClickListener((v) -> copyStacktraceToClipboard());
        binding.close.setOnClickListener((v) -> close());

        setSupportActionBar(binding.toolbar);
        Throwable throwable = (Throwable) Objects.requireNonNull(getIntent().getSerializableExtra(KEY_THROWABLE));
        throwable.printStackTrace();
        binding.toolbar.setTitle(getString(R.string.simple_error));
        binding.message.setText(throwable.getMessage());
        binding.stacktrace.setText(ExceptionUtil.getDebugInfos(this, throwable));
    }


    private void copyStacktraceToClipboard() {
        final ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(getSystemService(CLIPBOARD_SERVICE));
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + binding.stacktrace.getText() + "\n```");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void close() {
        finish();
    }
}
