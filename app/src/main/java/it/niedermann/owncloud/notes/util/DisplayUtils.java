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
package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;

public class DisplayUtils {

    private DisplayUtils() {

    }

    public static Spannable searchAndColor(Spannable spannable, CharSequence searchText, Context context, @Nullable Integer current) {
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
            spannable.setSpan(new SearchSpan(context, (current != null && i == current)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            i++;
        }

        return spannable;
    }


    static class SearchSpan extends MetricAffectingSpan {

        private final boolean current;
        private final int bgColorPrimary;
        private final int bgColorSecondary;

        SearchSpan(Context context, boolean current) {
            this.current = current;
            this.bgColorPrimary = context.getResources().getColor(R.color.bg_search_primary);
            this.bgColorSecondary = context.getResources().getColor(R.color.bg_search_secondary);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.bgColor = current ? bgColorPrimary : bgColorSecondary;
            tp.setColor(current ? (getForeground(Integer.toHexString(tp.bgColor)) ? Color.WHITE : Color.BLACK) : bgColorPrimary);
            tp.setFakeBoldText(true);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint tp) {
            tp.setFakeBoldText(true);
        }
    }

    public static boolean getForeground(String backgroundColorHex) {
        return ((float) (
                0.2126 * Integer.valueOf(backgroundColorHex.substring(1, 3), 16)
                        + 0.7152 * Integer.valueOf(backgroundColorHex.substring(3, 5), 16)
                        + 0.0722 * Integer.valueOf(backgroundColorHex.substring(5, 7), 16)
        ) < 140);
    }
}
