package it.niedermann.owncloud.notes.shared.util;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the NoteUtil
 * Created by stefan on 06.10.15.
 */
public class MarkDownUtilTest extends TestCase {

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

    public void testGetEndOfLine() {
        //language=md
        StringBuilder test = new StringBuilder(
                "# Test-Note\n" + // line 0 - 11
                        "\n" + // line 12 - 12
                        "- [ ] this is a test note\n" + // line 13 - 38
                        "- [x] test\n" + // line start 39 - 49
                        "[test](https://example.com)\n" + // line 50 - 77
                        "\n" + // line 77 - 78
                        "\n" // line 78 - 79
        );

        for (int i = 0; i < test.length(); i++) {
            int endOfLine = MarkDownUtil.getEndOfLine(test, i);
            if (i <= 11) {
                assertEquals(11, endOfLine);
            } else if (i <= 12) {
                assertEquals(12, endOfLine);
            } else if (i <= 38) {
                assertEquals(38, endOfLine);
            } else if (i <= 49) {
                assertEquals(49, endOfLine);
            } else if (i <= 77) {
                assertEquals(77, endOfLine);
            } else if (i <= 78) {
                assertEquals(78, endOfLine);
            } else if (i <= 79) {
                assertEquals(79, endOfLine);
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