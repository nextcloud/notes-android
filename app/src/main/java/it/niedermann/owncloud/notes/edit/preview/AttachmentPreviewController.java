/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.preview;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.AttachmentUrlUtil;

/**
 * Scans an editable note's markdown for Nextcloud attachment images and replaces
 * each image's markdown with the rendered image, via {@link AttachmentImagePreviewSpan}.
 *
 * <p>Does not modify the editor's text content. Re-scans on a debounce when the
 * user edits, so spans stay reconciled with the current markdown.</p>
 */
public class AttachmentPreviewController {

    private static final String TAG = AttachmentPreviewController.class.getSimpleName();
    private static final int MAX_HEIGHT_DP = 200;
    private static final int PLACEHOLDER_DP = 100;
    private static final int HORIZONTAL_CHROME_DP = 48;
    private static final long DEBOUNCE_MS = 500;
    private static final String NOTES_ROOT = "Notes";

    private final EditText editor;
    private final Account account;
    private final int maxWidthPx;
    private final int maxHeightPx;
    private final int placeholderSizePx;
    private final int targetWidthPx;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());

    private final List<CustomTarget<Drawable>> activeTargets = new ArrayList<>();
    private final List<AttachmentImagePreviewSpan> liveSpans = new ArrayList<>();

    @Nullable
    private String category;
    private boolean watcherAttached = false;

    private final Runnable rescanRunnable = this::rescan;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // no-op
        }

        @Override
        public void afterTextChanged(Editable s) {
            debounceHandler.removeCallbacks(rescanRunnable);
            debounceHandler.postDelayed(rescanRunnable, DEBOUNCE_MS);
        }
    };

    public AttachmentPreviewController(@NonNull EditText editor, @NonNull Account account) {
        this.editor = editor;
        this.account = account;
        final var dm = editor.getResources().getDisplayMetrics();
        this.maxHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MAX_HEIGHT_DP, dm);
        this.placeholderSizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, PLACEHOLDER_DP, dm);
        final int horizontalChrome = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, HORIZONTAL_CHROME_DP, dm);
        this.maxWidthPx = Math.max(1, dm.widthPixels - horizontalChrome);
        this.targetWidthPx = dm.widthPixels;
    }

    /**
     * Performs first-time setup: stores the note category, registers the
     * debounced re-scan watcher, and runs the initial scan.
     */
    public void attach(@NonNull Note note) {
        this.category = note.getCategory();
        if (!watcherAttached) {
            editor.addTextChangedListener(textWatcher);
            watcherAttached = true;
        }
        rescan();
    }

    /**
     * Tears down: cancels pending loads and re-scans, removes the watcher, and
     * detaches span callbacks so an in-flight load cannot touch a dead view.
     */
    public void detach() {
        debounceHandler.removeCallbacks(rescanRunnable);
        if (watcherAttached) {
            editor.removeTextChangedListener(textWatcher);
            watcherAttached = false;
        }
        clearSpansAndLoads();
    }

    private void clearSpansAndLoads() {
        for (CustomTarget<Drawable> target : activeTargets) {
            Glide.with(editor).clear(target);
        }
        activeTargets.clear();

        for (AttachmentImagePreviewSpan span : liveSpans) {
            span.setOnLoadedCallback(null);
        }
        liveSpans.clear();

        final Editable text = editor.getText();
        if (text != null) {
            final AttachmentImagePreviewSpan[] existing =
                    text.getSpans(0, text.length(), AttachmentImagePreviewSpan.class);
            for (AttachmentImagePreviewSpan span : existing) {
                text.removeSpan(span);
            }
        }
    }

    /**
     * Re-applies the span so a layout that already happened with the placeholder
     * size is redone now that the image (and its real box size) is available.
     * Re-setting a span fires the layout's SpanWatcher, which reflows the range.
     */
    private void reflowSpan(@NonNull AttachmentImagePreviewSpan span) {
        final Editable text = editor.getText();
        if (text == null) {
            return;
        }
        final int start = text.getSpanStart(span);
        final int end = text.getSpanEnd(span);
        if (start >= 0 && end > start) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        editor.invalidate();
    }

    private void rescan() {
        final Editable text = editor.getText();
        if (text == null) {
            return;
        }

        clearSpansAndLoads();

        final String username = account.getUserName();
        final Matcher matcher = AttachmentUrlUtil.ATTACHMENT_PATTERN.matcher(text);
        while (matcher.find()) {
            final int matchStart = matcher.start();
            final int matchEnd = matcher.end();
            final String attachmentPath = matcher.group(2);
            if (attachmentPath == null) {
                continue;
            }
            final String webdavRelative = AttachmentUrlUtil.transformAttachmentPath(
                    attachmentPath, username, NOTES_ROOT, category);

            final AttachmentImagePreviewSpan span = new AttachmentImagePreviewSpan(
                    maxWidthPx, maxHeightPx, placeholderSizePx);
            span.setOnLoadedCallback(() -> reflowSpan(span));
            text.setSpan(span, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            liveSpans.add(span);

            // Decode the image downsampled to roughly the thumbnail size. Without
            // a size constraint Glide decodes at full resolution, which can
            // produce a bitmap too large for the hardware canvas to draw.
            final CustomTarget<Drawable> target =
                    new CustomTarget<Drawable>(targetWidthPx, maxHeightPx) {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource,
                                                    @Nullable Transition<? super Drawable> transition) {
                            span.setLoadedDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.w(TAG, "Failed to load attachment image: " + webdavRelative);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // no-op; the span keeps its last drawable until rescan replaces it
                        }
                    };
            activeTargets.add(target);

            Glide.with(editor)
                    .load(new SingleSignOnUrl(account.getAccountName(),
                            account.getUrl() + webdavRelative))
                    .downsample(DownsampleStrategy.AT_MOST)
                    .into(target);
        }
    }
}
