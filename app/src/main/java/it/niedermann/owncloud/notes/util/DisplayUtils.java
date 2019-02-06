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

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.ColorInt;

public class DisplayUtils {

    public static Spannable searchAndColor(String text, Spannable spannable, String searchText, @ColorInt int color) {

        Object spansToRemove[] = spannable.getSpans(0, text.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                spannable.removeSpan(span);
        }

        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(searchText)) {
            return spannable;
        }

        Matcher m = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                .matcher(text);


        while (m.find()) {
            int start = m.start();
            int end = m.end();
            spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

}
