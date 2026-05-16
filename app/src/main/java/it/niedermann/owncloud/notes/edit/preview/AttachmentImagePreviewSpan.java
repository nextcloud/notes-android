/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.preview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Visually replaces a Nextcloud attachment image's markdown
 * ({@code ![alt](.attachments.XXX/file.ext)}) with the rendered image inside an
 * editable EditText.
 *
 * <p>This is a {@link ReplacementSpan}: it occupies a self-sized box and draws
 * the image into it. The markdown characters remain in the {@code Editable} and
 * are merely hidden behind the box, so the saved note content is unaffected.</p>
 *
 * <p>The image loads asynchronously. Until it arrives a faint placeholder box is
 * drawn; once loaded, {@link #setLoadedDrawable} fires a callback so the host can
 * re-measure (the box may change size).</p>
 */
public class AttachmentImagePreviewSpan extends ReplacementSpan {

    private final int maxWidthPx;
    private final int maxHeightPx;
    private final int placeholderSizePx;
    private final Paint placeholderPaint;

    @Nullable
    private Drawable loadedDrawable;
    @Nullable
    private Runnable onLoadedCallback;

    public AttachmentImagePreviewSpan(int maxWidthPx, int maxHeightPx, int placeholderSizePx) {
        this.maxWidthPx = Math.max(1, maxWidthPx);
        this.maxHeightPx = Math.max(1, maxHeightPx);
        this.placeholderSizePx = Math.max(1, placeholderSizePx);
        this.placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.placeholderPaint.setStyle(Paint.Style.STROKE);
        this.placeholderPaint.setStrokeWidth(2f);
        this.placeholderPaint.setColor(Color.argb(80, 128, 128, 128));
    }

    /**
     * Supplies the image once it has loaded and notifies the host so it can
     * re-measure the (possibly resized) box.
     */
    public void setLoadedDrawable(@NonNull Drawable drawable) {
        this.loadedDrawable = drawable;
        if (onLoadedCallback != null) {
            onLoadedCallback.run();
        }
    }

    /**
     * Sets (or, with null, clears) the callback invoked when the image loads.
     * The controller clears this on detach so a late load cannot touch a dead
     * view.
     */
    public void setOnLoadedCallback(@Nullable Runnable callback) {
        this.onLoadedCallback = callback;
    }

    private float scaleFactor(int dw, int dh) {
        final float scale = Math.min((float) maxWidthPx / dw, (float) maxHeightPx / dh);
        return scale > 1f ? 1f : scale;
    }

    private int boxWidth() {
        if (loadedDrawable == null) {
            return placeholderSizePx;
        }
        final int dw = loadedDrawable.getIntrinsicWidth();
        final int dh = loadedDrawable.getIntrinsicHeight();
        if (dw <= 0 || dh <= 0) {
            return placeholderSizePx;
        }
        return Math.max(1, Math.round(dw * scaleFactor(dw, dh)));
    }

    private int boxHeight() {
        if (loadedDrawable == null) {
            return placeholderSizePx;
        }
        final int dw = loadedDrawable.getIntrinsicWidth();
        final int dh = loadedDrawable.getIntrinsicHeight();
        if (dw <= 0 || dh <= 0) {
            return placeholderSizePx;
        }
        return Math.max(1, Math.round(dh * scaleFactor(dw, dh)));
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        final int h = boxHeight();
        if (fm != null) {
            // Occupy the full box height above the baseline.
            fm.ascent = -h;
            fm.top = -h;
            fm.descent = 0;
            fm.bottom = 0;
        }
        return boxWidth();
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        final int w = boxWidth();
        final int h = boxHeight();
        final int left = Math.round(x);
        if (loadedDrawable != null) {
            loadedDrawable.setBounds(left, top, left + w, top + h);
            loadedDrawable.draw(canvas);
        } else {
            final float pad = 2f;
            canvas.drawRect(left + pad, top + pad, left + w - pad, top + h - pad, placeholderPaint);
        }
    }
}
