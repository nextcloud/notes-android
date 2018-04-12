/*
 * Copyright 2016-2017 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.Locale;

import it.niedermann.owncloud.notes.R;


/**
 * @author Davide Steduto
 * @since 27/01/2016 Created
 * <br>17/12/2017 Moved into UI package
 */
@SuppressWarnings({"WeakerAccess", "unused", "ConstantConditions"})
public final class FlexibleUtils {

    public static final String SPLIT_EXPRESSION = "([, ]+)";
    public static final int INVALID_COLOR = -1;
    public static int colorAccent = INVALID_COLOR;

    /**
     * API 26
     *
     * @see VERSION_CODES#O
     */
    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.O;
    }

    /**
     * API 24
     *
     * @see VERSION_CODES#N
     */
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.N;
    }

    /**
     * API 23
     *
     * @see VERSION_CODES#M
     */
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    /**
     * API 21
     *
     * @see VERSION_CODES#LOLLIPOP
     */
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    /**
     * API 16
     *
     * @see VERSION_CODES#JELLY_BEAN
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
    }
    
    /**
     * @return the SimpleClassName of the provided object
     * @since 5.0.0-rc1
     */
    @NonNull
    public static String getClassName(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    /**
     * Sets a spannable text with the accent color into the provided TextView.
     * <p>Multiple matches will be highlighted, but if the 2nd match is consecutive,
     * the highlight is skipped.</p>
     * Internally calls {@link #fetchAccentColor(Context, int)}.
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @see #highlightText(TextView, String, String, int)
     * @see #highlightWords(TextView, String, String)
     * @since 5.0.0-rc1 Created
     * <br>5.0.0-rc3 Multi-span
     */
    public static void highlightText(@NonNull final TextView textView,
                                     @Nullable final String originalText,
                                     @Nullable String constraint) {
        int accentColor = fetchAccentColor(textView.getContext(), 1);
        highlightText(textView, originalText, constraint, accentColor);
    }

    /**
     * Sets a spannable text with any highlight color into the provided TextView.
     * <p>Multiple matches will be highlighted, but if the 2nd match is consecutive,
     * the highlight is skipped.</p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @param color        the highlight color
     * @see #highlightText(TextView, String, String)
     * @see #highlightWords(TextView, String, String, int)
     * @since 5.0.0-rc1 Created
     * <br>5.0.0-rc3 Multi-span
     */
    public static void highlightText(@NonNull final TextView textView,
                                     @Nullable final String originalText,
                                     @Nullable String constraint,
                                     @ColorInt int color) {
        constraint = toLowerCase(constraint);
        int start = toLowerCase(originalText).indexOf(constraint);
        if (start != -1) {
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
            spanText(originalText, constraint, color, start, spanText);
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    /**
     * Sets a spannable text with the accent color for <u>each word</u> provided by the
     * constraint text into the provided TextView.
     * <p><b>Note:</b>
     * <ul>
     * <li>Words are automatically split by {@code "([, ]+)"} regular expression.</li>
     * <li>To Actually see the text highlighted, the filter must check words too.</li>
     * <li>Internally calls {@link #fetchAccentColor(Context, int)}.</li></ul></p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraints  the multiple words to highlight
     * @see #highlightWords(TextView, String, String, int)
     * @see #highlightText(TextView, String, String)
     * @since 5.0.0-rc3
     */
    public static void highlightWords(@NonNull final TextView textView,
                                      @Nullable final String originalText,
                                      @Nullable String constraints) {
        int accentColor = fetchAccentColor(textView.getContext(), 1);
        highlightWords(textView, originalText, constraints, accentColor);
    }

    /**
     * Sets a spannable text with any highlight color for <u>each word</u> provided by the
     * constraint text into the provided TextView.
     * <p><b>Note:</b>
     * <ul><li>Words are automatically split by {@code "([, ]+)"} regular expression.</li>
     * <li>To Actually see the text highlighted, the filter must check words too.</li></ul></p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraints  the multiple words to highlight
     * @param color        the highlight color
     * @see #highlightWords(TextView, String, String)
     * @see #highlightText(TextView, String, String, int)
     * @since 5.0.0-rc3
     */
    public static void highlightWords(@NonNull final TextView textView,
                                      @Nullable final String originalText,
                                      @Nullable String constraints,
                                      @ColorInt int color) {
        constraints = toLowerCase(constraints);
        Spannable spanText = null;

        for (String constraint : constraints.split(SPLIT_EXPRESSION)) {
            int start = toLowerCase(originalText).indexOf(constraint);
            if (start != -1) {
                if (spanText == null) {
                    spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                }
                spanText(originalText, constraint, color, start, spanText);
            }
        }

        if (spanText != null) {
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    private static void spanText(@NonNull final String originalText,
                                 @NonNull String constraint,
                                 @ColorInt int color, int start,
                                 @NonNull final Spannable spanText) {
        do {
            int end = start + constraint.length();
            spanText.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = toLowerCase(originalText).indexOf(constraint, end + 1); // +1 skips the consecutive span
        } while (start != -1);
    }

    public static String toLowerCase(@Nullable String text) {
        if (text == null) text = "";
        return text.toLowerCase(Locale.getDefault());
    }

    /*------------------------------*/
    /* ACCENT COLOR UTILITY METHODS */
    /*------------------------------*/

    /**
     * Reset the internal accent color to {@link #INVALID_COLOR}, to give the possibility
     * to re-fetch it at runtime, since once it is fetched it cannot be changed.
     */
    public static void resetAccentColor() {
        colorAccent = INVALID_COLOR;
    }

    /**
     * Optimized method to fetch the accent color on devices with at least Lollipop.
     * <p>If accent color has been already fetched it is simply returned.</p>
     *
     * @param context  context
     * @param defColor value to return if the accentColor cannot be found
     */
    public static int fetchAccentColor(Context context, @ColorInt int defColor) {
        if (colorAccent == INVALID_COLOR) {
            int attr = R.attr.colorAccent;
            if (hasLollipop()) attr = android.R.attr.colorAccent;
            TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{attr});
            colorAccent = androidAttr.getColor(0, defColor);
            androidAttr.recycle();
        }
        return colorAccent;
    }

}