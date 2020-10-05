/*
 * Nextcloud Notes application
 *
 * @author Mario Danic
 * Copyright (C) 2018 Mario Danic <mario@lovelyhq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;

import static it.niedermann.owncloud.notes.shared.util.ColorUtil.isColorDark;

public class DisplayUtils {

    private DisplayUtils() {

    }

    public static Spannable searchAndColor(Spannable spannable, CharSequence searchText, @NonNull Context context, @Nullable Integer current, @ColorInt int mainColor, @ColorInt int textColor) {
        CharSequence text = spannable.toString();

        Object[] spansToRemove = spannable.getSpans(0, text.length(), Object.class);
        for (Object span : spansToRemove) {
            if (span instanceof SearchSpan)
                spannable.removeSpan(span);
        }

        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(searchText)) {
            return spannable;
        }

        Matcher m = Pattern.compile(searchText.toString(), Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                .matcher(text);

        int i = 1;
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            spannable.setSpan(new SearchSpan(context, mainColor, textColor, (current != null && i == current)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            i++;
        }

        return spannable;
    }


    static class SearchSpan extends MetricAffectingSpan {

        private final boolean current;
        @NonNull
        Context context;
        @ColorInt
        private final int mainColor;
        @ColorInt
        private final int textColor;
        @ColorInt
        private final int highlightColor;

        SearchSpan(@NonNull Context context, @ColorInt int mainColor, @ColorInt int textColor, boolean current) {
            this.context = context;
            this.mainColor = mainColor;
            this.textColor = textColor;
            this.current = current;
            this.highlightColor = context.getResources().getColor(R.color.bg_highlighted);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            if (current) {
                if (NotesApplication.isDarkThemeActive(context)) {
                    if (isColorDark(mainColor)) {
                        tp.bgColor = Color.WHITE;
                        tp.setColor(mainColor);
                    } else {
                        tp.bgColor = mainColor;
                        tp.setColor(Color.BLACK);
                    }
                } else {
                    if (isColorDark(mainColor)) {
                        tp.bgColor = mainColor;
                        tp.setColor(Color.WHITE);
                    } else {
                        if (ColorUtil.contrastRatioIsSufficient(mainColor, highlightColor)) {
                            tp.bgColor = highlightColor;
                        } else {
                            tp.bgColor = Color.BLACK;
                        }
                        tp.setColor(mainColor);
                    }
                }
            } else {
                tp.bgColor = highlightColor;
                tp.setColor(BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, mainColor));
            }
            tp.setFakeBoldText(true);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint tp) {
            tp.setFakeBoldText(true);
        }
    }
}
