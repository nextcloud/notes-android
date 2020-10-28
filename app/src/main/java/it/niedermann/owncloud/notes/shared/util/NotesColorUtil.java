package it.niedermann.owncloud.notes.shared.util;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.HashMap;
import java.util.Map;

import it.niedermann.android.util.ColorUtil;

public final class NotesColorUtil {

    private static final Map<ColorPair, Boolean> CONTRAST_RATIO_SUFFICIENT_CACHE = new HashMap<>();

    private NotesColorUtil() {
    }

    public static boolean contrastRatioIsSufficient(@ColorInt int colorOne, @ColorInt int colorTwo) {
        ColorPair key = new ColorPair(colorOne, colorTwo);
        Boolean ret = CONTRAST_RATIO_SUFFICIENT_CACHE.get(key);
        if (ret == null) {
            ret = ColorUtil.INSTANCE.getContrastRatio(colorOne, colorTwo) > 3d;
            CONTRAST_RATIO_SUFFICIENT_CACHE.put(key, ret);
            return ret;
        }
        return ret;
    }

    private static class ColorPair extends Pair<Integer, Integer> {

        private ColorPair(@Nullable Integer first, @Nullable Integer second) {
            super(first, second);
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass", "NumberEquality"})
        @Override
        public boolean equals(Object o) {
            final ColorPair colorPair = (ColorPair) o;
            if (first != colorPair.first) return false;
            return second == colorPair.second;
        }

        @Override
        public int hashCode() {
            int result = first;
            result = 31 * result + second;
            return result;
        }
    }
}
