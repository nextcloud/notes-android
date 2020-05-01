package it.niedermann.owncloud.notes.util;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.HashMap;
import java.util.Map;

public final class ColorUtil {

    private static final Map<ColorPair, Boolean> CONTRAST_RATIO_SUFFICIENT_CACHE = new HashMap<>();
    private static final Map<Integer, Integer> FOREGROUND_CACHE = new HashMap<>();
    private static final Map<Integer, Boolean> IS_DARK_COLOR_CACHE = new HashMap<>();

    private ColorUtil() {
    }

    @ColorInt
    public static int getForegroundColorForBackgroundColor(@ColorInt int color) {
        Integer ret = FOREGROUND_CACHE.get(color);
        if (ret == null) {
            if (Color.TRANSPARENT == color)
                ret = Color.BLACK;
            else if (isColorDark(color))
                ret = Color.WHITE;
            else
                ret = Color.BLACK;

            FOREGROUND_CACHE.put(color, ret);
        }
        return ret;
    }

    public static boolean isColorDark(@ColorInt int color) {
        Boolean ret = IS_DARK_COLOR_CACHE.get(color);
        if (ret == null) {
            ret = getBrightness(color) < 200;
            IS_DARK_COLOR_CACHE.put(color, ret);
        }
        return ret;
    }

    private static int getBrightness(@ColorInt int color) {
        final int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        return (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);
    }

    // ---------------------------------------------------
    // Based on https://github.com/LeaVerou/contrast-ratio
    // ---------------------------------------------------

    public static boolean contrastRatioIsSufficient(@ColorInt int colorOne, @ColorInt int colorTwo) {
        ColorPair key = new ColorPair(colorOne, colorTwo);
        Boolean ret = CONTRAST_RATIO_SUFFICIENT_CACHE.get(key);
        if (ret == null) {
            ret = getContrastRatio(colorOne, colorTwo) > 3d;
            CONTRAST_RATIO_SUFFICIENT_CACHE.put(key, ret);
            return ret;
        }
        return ret;
    }

    private static double getContrastRatio(@ColorInt int colorOne, @ColorInt int colorTwo) {
        final double lum1 = getLuminanace(colorOne);
        final double lum2 = getLuminanace(colorTwo);
        final double brightest = Math.max(lum1, lum2);
        final double darkest = Math.min(lum1, lum2);
        return (brightest + 0.05) / (darkest + 0.05);
    }

    private static double getLuminanace(@ColorInt int color) {
        final int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        return getSubcolorLuminance(rgb[0]) * 0.2126 + getSubcolorLuminance(rgb[1]) * 0.7152 + getSubcolorLuminance(rgb[2]) * 0.0722;
    }

    private static double getSubcolorLuminance(@ColorInt int color) {
        final double value = color / 255d;
        return value <= 0.03928
                ? value / 12.92
                : Math.pow((value + 0.055) / 1.055, 2.4);
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

        @SuppressWarnings("ConstantConditions")
        @Override
        public int hashCode() {
            int result = first;
            result = 31 * result + second;
            return result;
        }
    }
}
