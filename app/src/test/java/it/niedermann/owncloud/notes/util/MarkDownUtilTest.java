package it.niedermann.owncloud.notes.util;

import androidx.annotation.NonNull;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the NoteUtil
 * Created by stefan on 06.10.15.
 */
public class MarkDownUtilTest extends TestCase {

    public static int getStartOfLine(@NonNull CharSequence s, int start) {
        int startOfLine = start;
        while (startOfLine > 0 && s.charAt(startOfLine - 1) != '\n') {
            startOfLine--;
        }
        return startOfLine;
    }

    public void testGetStartOfLine() {
        //language=md
        StringBuilder test = new StringBuilder(
                "# Test-Note\n" + // line start 0
                        "\n" + // line start 12
                        "- [ ] this is a test note\n" + // line start 13
                        "- [x] test\n" + // line start 39
                        "[test](https://example.com)\n" + // line start 50
                        "\n" + // line start 77
                        "\n" // line start 78
        );

        for (int i = 0; i < test.length(); i++) {
            int startOfLine = MarkDownUtil.getStartOfLine(test, i);
            if (i <= 11) {
                assertEquals(0, startOfLine);
            } else if (i <= 12) {
                assertEquals(12, startOfLine);
            } else if (i <= 38) {
                assertEquals(13, startOfLine);
            } else if (i <= 49) {
                assertEquals(39, startOfLine);
            } else if (i <= 77) {
                assertEquals(50, startOfLine);
            } else if (i <= 78) {
                assertEquals(78, startOfLine);
            } else if (i <= 79) {
                assertEquals(79, startOfLine);
            }
        }
    }

    public void testLineStartsWithCheckbox() {
        Map<String, Boolean> lines = new HashMap<>();
        lines.put("- [ ] ", true);
        lines.put("- [x] ", true);
        lines.put("* [ ] ", true);
        lines.put("* [x] ", true);
        lines.put("- [ ]", true);
        lines.put("- [x]", true);
        lines.put("* [ ]", true);
        lines.put("* [x]", true);

        lines.put("-[ ] ", false);
        lines.put("-[x] ", false);
        lines.put("*[ ] ", false);
        lines.put("*[x] ", false);
        lines.put("-[ ]", false);
        lines.put("-[x]", false);
        lines.put("*[ ]", false);
        lines.put("*[x]", false);

        lines.put("- [] ", false);
        lines.put("* [] ", false);
        lines.put("- []", false);
        lines.put("* []", false);

        lines.put("-[] ", false);
        lines.put("*[] ", false);
        lines.put("-[]", false);
        lines.put("*[]", false);

        lines.forEach((key,value) -> assertEquals(value, (Boolean) MarkDownUtil.lineStartsWithCheckbox(key)));
    }
}