/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;

public class NoteShoppingPreviewFragment extends NotePreviewFragment {

    private static final Pattern CHECKED_CHECKBOX_LINE_PATTERN = Pattern.compile(
            "^\\s*(?:[-*+]\\s+|\\d+[.)]\\s+)\\[[xX]\\]\\s*.*$"
    );

    private final List<HiddenLine> hiddenLines = new ArrayList<>();

    private static class HiddenLine {
        private final int visibleIndex;
        @NonNull
        private final String content;

        private HiddenLine(int visibleIndex, @NonNull String content) {
            this.visibleIndex = visibleIndex;
            this.content = content;
        }

        private int getVisibleIndex() {
            return visibleIndex;
        }

        @NonNull
        private String getContent() {
            return content;
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(true);
        menu.findItem(R.id.menu_preview).setVisible(false);
        menu.findItem(R.id.menu_shopping).setVisible(false);
    }

    @Override
    @NonNull
    protected String mapContentForDisplay(@NonNull String content) {
        hiddenLines.clear();

        final var visibleLines = new ArrayList<String>();
        int visibleIndex = 0;

        for (final String line : splitLines(content)) {
            if (isCheckedCheckboxLine(line)) {
                hiddenLines.add(new HiddenLine(visibleIndex, line));
            } else {
                visibleLines.add(line);
                visibleIndex++;
            }
        }

        return joinLines(visibleLines);
    }

    @Override
    @NonNull
    protected String mapContentForSaving(@NonNull String content) {
        if (hiddenLines.isEmpty()) {
            return content;
        }

        final List<String> editedVisibleLines = splitLines(content);
        final var mergedLines = new ArrayList<String>();

        int hiddenCursor = 0;
        for (int i = 0; i <= editedVisibleLines.size(); i++) {
            while (hiddenCursor < hiddenLines.size() && hiddenLines.get(hiddenCursor).getVisibleIndex() == i) {
                mergedLines.add(hiddenLines.get(hiddenCursor).getContent());
                hiddenCursor++;
            }

            if (i < editedVisibleLines.size()) {
                mergedLines.add(editedVisibleLines.get(i));
            }
        }

        while (hiddenCursor < hiddenLines.size()) {
            mergedLines.add(hiddenLines.get(hiddenCursor).getContent());
            hiddenCursor++;
        }

        return joinLines(mergedLines);
    }

    private boolean isCheckedCheckboxLine(@NonNull String line) {
        return CHECKED_CHECKBOX_LINE_PATTERN.matcher(line).matches();
    }

    @NonNull
    private List<String> splitLines(@NonNull String content) {
        final String[] lines = content.split("\\R", -1);
        final var result = new ArrayList<String>(lines.length);
        for (final String line : lines) {
            result.add(line);
        }
        return result;
    }

    @NonNull
    private String joinLines(@NonNull List<String> lines) {
        return String.join("\n", lines);
    }

    public static BaseNoteFragment newInstance(long accountId, long noteId) {
        final var fragment = new NoteShoppingPreviewFragment();
        final var args = new Bundle();
        args.putLong(PARAM_NOTE_ID, noteId);
        args.putLong(PARAM_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }
}
