package it.niedermann.nextcloud.deck.util;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.shared.util.ColorUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ColorUtilTest {

    @ColorInt
    private static final int[] DARK_COLORS = new int[]{
            Color.BLACK,
            Color.parseColor("#0082C9"), // "Nextcloud-Blue"
            Color.parseColor("#007676")
    };

    @ColorInt
    private static final int[] LIGHT_COLORS = new int[]{
            Color.WHITE,
            Color.YELLOW
    };

    @Test
    public void testGetForegroundColorForBackgroundColor() {
        for (@ColorInt int color : DARK_COLORS) {
            assertEquals(
                    "Expect foreground color for " + String.format("#%06X", (0xFFFFFF & color)) + " to be " + String.format("#%06X", (0xFFFFFF & Color.WHITE)),
                    Color.WHITE, ColorUtil.getForegroundColorForBackgroundColor(color)
            );
        }
        for (@ColorInt int color : LIGHT_COLORS) {
            assertEquals(
                    "Expect foreground color for " + String.format("#%06X", (0xFFFFFF & color)) + " to be " + String.format("#%06X", (0xFFFFFF & Color.BLACK)),
                    Color.BLACK, ColorUtil.getForegroundColorForBackgroundColor(color)
            );
        }
        assertEquals(
                "Expect foreground color for " + String.format("#%06X", (0xFFFFFF & Color.TRANSPARENT)) + " to be " + String.format("#%06X", (0xFFFFFF & Color.BLACK)),
                Color.BLACK, ColorUtil.getForegroundColorForBackgroundColor(Color.TRANSPARENT)
        );
    }

    @Test
    public void testIsColorDark() {
        for (@ColorInt int color : DARK_COLORS) {
            assertTrue(
                    "Expect " + String.format("#%06X", (0xFFFFFF & color)) + " to be a dark color",
                    ColorUtil.isColorDark(color)
            );
        }
        for (@ColorInt int color : LIGHT_COLORS) {
            assertFalse(
                    "Expect " + String.format("#%06X", (0xFFFFFF & color)) + " to be a light color",
                    ColorUtil.isColorDark(color)
            );
        }
    }

    @Test
    public void testContrastRatioIsSufficient() {
        final List<Pair<Integer, Integer>> sufficientContrastColorPairs = new ArrayList<>();
        sufficientContrastColorPairs.add(new Pair<>(Color.BLACK, Color.WHITE));
        sufficientContrastColorPairs.add(new Pair<>(Color.WHITE, Color.parseColor("#0082C9")));

        for (Pair<Integer, Integer> colorPair : sufficientContrastColorPairs) {
            assert colorPair.first != null;
            assert colorPair.second != null;
            assertTrue(
                    "Expect contrast between " + String.format("#%06X", (0xFFFFFF & colorPair.first)) + " and " + String.format("#%06X", (0xFFFFFF & colorPair.second)) + " to be sufficient",
                    ColorUtil.contrastRatioIsSufficient(colorPair.first, colorPair.second)
            );
        }

        final List<Pair<Integer, Integer>> insufficientContrastColorPairs = new ArrayList<>();
        insufficientContrastColorPairs.add(new Pair<>(Color.WHITE, Color.WHITE));
        insufficientContrastColorPairs.add(new Pair<>(Color.BLACK, Color.BLACK));

        for (Pair<Integer, Integer> colorPair : insufficientContrastColorPairs) {
            assert colorPair.first != null;
            assert colorPair.second != null;
            assertFalse(
                    "Expect contrast between " + String.format("#%06X", (0xFFFFFF & colorPair.first)) + " and " + String.format("#%06X", (0xFFFFFF & colorPair.second)) + " to be insufficient",
                    ColorUtil.contrastRatioIsSufficient(colorPair.first, colorPair.second)
            );
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetCleanHexaColorString() {
        final List<Pair<String, String>> validColors = new ArrayList<>();
        validColors.add(new Pair<>("#0082C9", "#0082C9"));
        validColors.add(new Pair<>("0082C9", "#0082C9"));
        validColors.add(new Pair<>("#CCC", "#CCCCCC"));
        validColors.add(new Pair<>("ccc", "#cccccc"));
        validColors.add(new Pair<>("af0", "#aaff00"));
        validColors.add(new Pair<>("#af0", "#aaff00"));
        for (Pair<String, String> color : validColors) {
            assertEquals("Expect " + color.first + " to be cleaned up to " + color.second, color.second, ColorUtil.formatColorToParsableHexString(color.first));
        }

        final String[] invalidColors = new String[]{null, "", "cc", "c", "#a", "#55L", "55L"};
        for (String color : invalidColors) {
            exception.expect(IllegalArgumentException.class);
            ColorUtil.formatColorToParsableHexString(color);
        }
    }
}
