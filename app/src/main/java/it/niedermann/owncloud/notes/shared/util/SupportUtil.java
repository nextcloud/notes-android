/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class SupportUtil {

    private SupportUtil() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    public static SpannableString strong(@NonNull CharSequence text) {
        final var spannable = new SpannableString(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), 0);
        return spannable;
    }

    public static SpannableString url(@NonNull CharSequence text, @NonNull String target) {
        final var spannable = new SpannableString(text);
        spannable.setSpan(new URLSpan(target), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static void setTextWithURL(@NonNull TextView textView, @NonNull Resources resources, @StringRes int containerTextId, @StringRes int linkLabelId, @StringRes int urlId) {
        final String linkLabel = resources.getString(linkLabelId);
        setTextWithURL(textView, resources, containerTextId, linkLabel, urlId);
    }

    public static void setTextWithURL(@NonNull TextView textView, @NonNull Resources resources, @StringRes int containerTextId,  final String linkLabel, @StringRes int urlId) {
        final String finalText = resources.getString(containerTextId, linkLabel);
        final var spannable = new SpannableString(finalText);
        spannable.setSpan(new URLSpan(resources.getString(urlId)), finalText.indexOf(linkLabel), finalText.indexOf(linkLabel) + linkLabel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
        textView.setMovementMethod(new LinkMovementMethod());
    }
}
