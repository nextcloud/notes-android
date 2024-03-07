/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class AppendToNoteActivity extends MainActivity {

    private static final String TAG = AppendToNoteActivity.class.getSimpleName();

    String receivedText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receivedText = ShareUtil.extractSharedText(getIntent());
        @Nullable final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.append_to_note);
        } else {
            Log.e(TAG, "SupportActionBar is null. Expected toolbar to be present to set a title.");
        }
        binding.activityNotesListView.searchToolbar.setSubtitle(receivedText);
    }

    @Override
    public void onNoteClick(int position, View v) {
        if (!TextUtils.isEmpty(receivedText)) {
            final var fullNote$ = mainViewModel.getFullNote$(((Note) adapter.getItem(position)).getId());
            fullNote$.observe(this, (fullNote) -> {
                fullNote$.removeObservers(this);
                final String oldContent = fullNote.getContent();
                String newContent;
                if (!TextUtils.isEmpty(oldContent)) {
                    newContent = oldContent + "\n\n" + receivedText;
                } else {
                    newContent = receivedText;
                }
                final var updateLiveData = mainViewModel.updateNoteAndSync(fullNote, newContent, null);
                updateLiveData.observe(this, (next) -> {
                    Toast.makeText(this, getString(R.string.added_content, receivedText), Toast.LENGTH_SHORT).show();
                    updateLiveData.removeObservers(this);
                });
            });
        } else {
            Toast.makeText(this, R.string.shared_text_empty, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
