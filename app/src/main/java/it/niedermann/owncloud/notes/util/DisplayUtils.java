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

import android.content.res.Resources;
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

import it.niedermann.owncloud.notes.R;

public class DisplayUtils {

    private DisplayUtils() {

    }

    public static Spannable searchAndColor(Spannable spannable, CharSequence searchText, @NonNull Resources resources, @Nullable Integer current, @ColorInt int mainColor, @ColorInt int textColor) {
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
            spannable.setSpan(new SearchSpan(resources, mainColor, textColor, (current != null && i == current)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            i++;
        }

        return spannable;
    }


    static class SearchSpan extends MetricAffectingSpan {

        private final boolean current;
        @NonNull
        Resources resources;
        @ColorInt
        private final int mainColor;
        @ColorInt
        private final int textColor;

        SearchSpan(@NonNull Resources resources, @ColorInt int mainColor, @ColorInt int textColor, boolean current) {
            this.resources = resources;
            this.mainColor = mainColor;
            this.textColor = textColor;
            this.current = current;
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.bgColor = current ? mainColor : resources.getColor(R.color.bg_highlighted);
            tp.setColor(current ? textColor : ColorUtil.contrastRatioIsSufficient(mainColor, resources.getColor(R.color.bg_highlighted)) ? mainColor : Color.BLACK);
            tp.setFakeBoldText(true);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint tp) {
            tp.setFakeBoldText(true);
        }
    }
}
