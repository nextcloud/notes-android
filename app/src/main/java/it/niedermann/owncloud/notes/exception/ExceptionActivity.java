/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.exception;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;

import it.niedermann.android.util.ClipboardUtil;
import it.niedermann.nextcloud.exception.ExceptionUtil;
import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ActivityExceptionBinding;
import it.niedermann.owncloud.notes.exception.tips.TipsAdapter;

public class ExceptionActivity extends AppCompatActivity {

    private static final String KEY_THROWABLE = "throwable";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final var binding = ActivityExceptionBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        var throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));

        if (throwable == null) {
            throwable = new Exception("Could not get exception");
        }

        final var adapter = new TipsAdapter(this::startActivity);
        final String debugInfos = ExceptionUtil.getDebugInfos(this, throwable, BuildConfig.FLAVOR);

        binding.tips.setAdapter(adapter);
        binding.tips.setNestedScrollingEnabled(false);
        binding.toolbar.setTitle(getString(R.string.simple_error));
        binding.message.setText(throwable.getMessage());
        binding.stacktrace.setText(debugInfos);
        binding.copy.setOnClickListener((v) -> ClipboardUtil.copyToClipboard(this, getString(R.string.simple_exception), "```\n" + debugInfos + "\n```"));
        binding.close.setOnClickListener((v) -> finish());

        NotesApplication.brandingUtil().platform.themeStatusBar(this);
        NotesApplication.brandingUtil().material.themeToolbar(binding.toolbar);
        NotesApplication.brandingUtil().material.colorMaterialButtonPrimaryBorderless(binding.close);
        NotesApplication.brandingUtil().material.colorMaterialButtonPrimaryFilled(binding.copy);

        adapter.setThrowables(Collections.singletonList(throwable));
    }

    @NonNull
    public static Intent createIntent(@NonNull Context context, Throwable throwable) {
        final var args = new Bundle();
        args.putSerializable(KEY_THROWABLE, throwable);
        return new Intent(context, ExceptionActivity.class)
                .putExtras(args)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
