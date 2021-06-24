package it.niedermann.android.markdown;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import it.niedermann.android.markdown.model.EListType;
import it.niedermann.android.markdown.model.SearchSpan;

public class MarkdownUtil {

    private static final String TAG = MarkdownUtil.class.getSimpleName();

    private final static Parser parser = Parser.builder().build();
    private final static HtmlRenderer renderer = HtmlRenderer.builder().softbreak("<br>").build();

    private static final Pattern PATTERN_CODE_FENCE = Pattern.compile("^(`{3,})");
    private static final Pattern PATTERN_ORDERED_LIST_ITEM = Pattern.compile("^(\\d+).\\s.+$");
    private static final Pattern PATTERN_ORDERED_LIST_ITEM_EMPTY = Pattern.compile("^(\\d+).\\s$");
    private static final Pattern PATTERN_MARKDOWN_LINK = Pattern.compile("\\[(.+)?]\\(([^ ]+?)?( \"(.+)\")?\\)");

    @Nullable
    private static final String checkboxCheckedEmoji = getCheckboxEmoji(true);
    @Nullable
    private static final String checkboxUncheckedEmoji = getCheckboxEmoji(false);

    private MarkdownUtil() {
        // Util class
    }

    /**
     * {@link RemoteView}s have a limited subset of supported classes to maintain compatibility with many different launchers.
     * <p>
     * Since {@link Markwon} makes heavy use of custom spans, this won't look nice e. g. at app widgets, because they simply won't be rendered.
     * Therefore we currently use {@link HtmlCompat} to filter supported spans from the output of {@link HtmlRenderer} as an intermediate step.
     */
    public static CharSequence renderForRemoteView(@NonNull Context context, @NonNull String content) {
        // Create HTML string from Markup
        final String html = renderer.render(parser.parse(replaceCheckboxesWithEmojis(content)));

        // Create Spanned from HTML, with special handling for ordered list items
        final Spanned spanned = HtmlCompat.fromHtml(ListTagHandler.prepareTagHandling(html), 0, null, new ListTagHandler());

        // Enhance colors and margins of the Spanned
        return customizeQuoteSpanAppearance(context, spanned, 5, 30);
    }

    @SuppressWarnings("SameParameterValue")
    private static Spanned customizeQuoteSpanAppearance(@NonNull Context context, @NonNull Spanned input, int stripeWidth, int gapWidth) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final QuoteSpan[] originalQuoteSpans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
            @ColorInt final int colorBlockQuote = ContextCompat.getColor(context, R.color.block_quote);
            for (QuoteSpan originalQuoteSpan : originalQuoteSpans) {
                final int start = ssb.getSpanStart(originalQuoteSpan);
                final int end = ssb.getSpanEnd(originalQuoteSpan);
                ssb.removeSpan(originalQuoteSpan);
                ssb.setSpan(new QuoteSpan(colorBlockQuote, stripeWidth, gapWidth), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }

    @NonNull
    public static String replaceCheckboxesWithEmojis(@NonNull String content) {
        return runForEachCheckbox(content, (line) -> {
            for (EListType listType : EListType.values()) {
                if (checkboxCheckedEmoji != null) {
                    line = line.replace(listType.checkboxChecked, checkboxCheckedEmoji);
                    line = line.replace(listType.checkboxCheckedUpperCase, checkboxCheckedEmoji);
                }
                if (checkboxUncheckedEmoji != null) {
                    line = line.replace(listType.checkboxUnchecked, checkboxUncheckedEmoji);
                }
            }
            return line;
        });
    }

    @Nullable
    private static String getCheckboxEmoji(boolean checked) {
        final String[] checkedEmojis;
        final String[] uncheckedEmojis;
        // Seriously what the fuck, Samsung?
        // https://emojipedia.org/ballot-box-with-x/
        if (Build.MANUFACTURER != null && Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("samsung")) {
            checkedEmojis = new String[]{"✅", "☑️", "✔️"};
            uncheckedEmojis = new String[]{"❌", "\uD83D\uDD32️", "☐️"};
        } else {
            checkedEmojis = new String[]{"☒", "✅", "☑️", "✔️"};
            uncheckedEmojis = new String[]{"☐", "❌", "\uD83D\uDD32️", "☐️"};
        }
        final Paint paint = new Paint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String emoji : checked ? checkedEmojis : uncheckedEmojis) {
                if (paint.hasGlyph(emoji)) {
                    return emoji;
                }
            }
        }
        return null;
    }

    /**
     * Performs the given {@param map} function for each line which contains a checkbox
     */
    @NonNull
    private static String runForEachCheckbox(@NonNull String markdownString, @NonNull Function<String, String> map) {
        final String[] lines = markdownString.split("\n");
        boolean isInFencedCodeBlock = false;
        int fencedCodeBlockSigns = 0;
        for (int i = 0; i < lines.length; i++) {
            final Matcher matcher = PATTERN_CODE_FENCE.matcher(lines[i]);
            if (matcher.find()) {
                final String fence = matcher.group(1);
                if (fence != null) {
                    int currentFencedCodeBlockSigns = fence.length();
                    if (isInFencedCodeBlock) {
                        if (currentFencedCodeBlockSigns == fencedCodeBlockSigns) {
                            isInFencedCodeBlock = false;
                            fencedCodeBlockSigns = 0;
                        }
                    } else {
                        isInFencedCodeBlock = true;
                        fencedCodeBlockSigns = currentFencedCodeBlockSigns;
                    }
                }
            }
            if (!isInFencedCodeBlock) {
                if (lineStartsWithCheckbox(lines[i]) && lines[i].trim().length() > EListType.DASH.checkboxChecked.length()) {
                    lines[i] = map.apply(lines[i]);
                }
            }
        }
        return TextUtils.join("\n", lines);
    }

    public static int getStartOfLine(@NonNull CharSequence s, int cursorPosition) {
        int startOfLine = cursorPosition;
        while (startOfLine > 0 && s.charAt(startOfLine - 1) != '\n') {
            startOfLine--;
        }
        return startOfLine;
    }

    public static int getEndOfLine(@NonNull CharSequence s, int cursorPosition) {
        int nextLinebreak = s.toString().indexOf('\n', cursorPosition);
        if (nextLinebreak > -1) {
            return nextLinebreak;
        }
        return cursorPosition;
    }

    public static String getListItemIfIsEmpty(@NonNull String line) {
        for (EListType listType : EListType.values()) {
            if (line.equals(listType.checkboxUncheckedWithTrailingSpace)) {
                return listType.checkboxUncheckedWithTrailingSpace;
            } else if (line.equals(listType.listSymbolWithTrailingSpace)) {
                return listType.listSymbolWithTrailingSpace;
            }
        }
        final Matcher matcher = PATTERN_ORDERED_LIST_ITEM_EMPTY.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static CharSequence setCheckboxStatus(@NonNull String markdownString, int targetCheckboxIndex, boolean newCheckedState) {
        final String[] lines = markdownString.split("\n");
        int checkboxIndex = 0;
        boolean isInFencedCodeBlock = false;
        int fencedCodeBlockSigns = 0;
        for (int i = 0; i < lines.length; i++) {
            final Matcher matcher = PATTERN_CODE_FENCE.matcher(lines[i]);
            if (matcher.find()) {
                final String fence = matcher.group(1);
                if (fence != null) {
                    int currentFencedCodeBlockSigns = fence.length();
                    if (isInFencedCodeBlock) {
                        if (currentFencedCodeBlockSigns == fencedCodeBlockSigns) {
                            isInFencedCodeBlock = false;
                            fencedCodeBlockSigns = 0;
                        }
                    } else {
                        isInFencedCodeBlock = true;
                        fencedCodeBlockSigns = currentFencedCodeBlockSigns;
                    }
                }
            }
            if (!isInFencedCodeBlock) {
                if (lineStartsWithCheckbox(lines[i]) && lines[i].trim().length() > EListType.DASH.checkboxChecked.length()) {
                    if (checkboxIndex == targetCheckboxIndex) {
                        final int indexOfStartingBracket = lines[i].indexOf("[");
                        final String toggledLine = lines[i].substring(0, indexOfStartingBracket + 1) +
                                (newCheckedState ? 'x' : ' ') +
                                lines[i].substring(indexOfStartingBracket + 2);
                        lines[i] = toggledLine;
                        break;
                    }
                    checkboxIndex++;
                }
            }
        }
        return TextUtils.join("\n", lines);
    }

    public static boolean lineStartsWithCheckbox(@NonNull String line) {
        for (EListType listType : EListType.values()) {
            if (lineStartsWithCheckbox(line, listType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean lineStartsWithCheckbox(@NonNull String line, @NonNull EListType listType) {
        final String trimmedLine = line.trim();
        return (trimmedLine.startsWith(listType.checkboxUnchecked) || trimmedLine.startsWith(listType.checkboxChecked) || trimmedLine.startsWith(listType.checkboxCheckedUpperCase));
    }

    /**
     * @return the number of the ordered list item if the line is an ordered list, otherwise -1.
     */
    public static int getOrderedListNumber(@NonNull String line) {
        final Matcher matcher = PATTERN_ORDERED_LIST_ITEM.matcher(line);
        if (matcher.find()) {
            final String groupNumber = matcher.group(1);
            if (groupNumber != null) {
                try {
                    return Integer.parseInt(groupNumber);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Modifies the {@param editable} and adds the given {@param punctuation} from
     * {@param selectionStart} to {@param selectionEnd} or removes the {@param punctuation} in case
     * it already is around the selected part.
     *
     * @return the new cursor position
     */
    public static int togglePunctuation(@NonNull Editable editable, int selectionStart, int selectionEnd, @NonNull String punctuation) {
        final String initialString = editable.toString();
        if (selectionStart < 0 || selectionStart > initialString.length() || selectionEnd < 0 || selectionEnd > initialString.length()) {
            return 0;
        }

        // handle special case: italic (that damn thing will match like ANYTHING (regarding bold / bold+italic)....)
        final boolean isItalic = punctuation.length() == 1 && punctuation.charAt(0) == '*';
        if (isItalic) {
            final Optional<Integer> result = handleItalicEdgeCase(editable, initialString, selectionStart, selectionEnd);
            // The result is only present if this actually was an edge case
            if (result.isPresent()) {
                return result.get();
            }
        }

        // handle the simple cases
        final String wildcardRex = "([^" + punctuation.charAt(0) + "])+";
        final String punctuationRex = Pattern.quote(punctuation);
        final String pattern = isItalic
                // in this case let's make optional asterisks around it, so it wont match anything between two (bold+italic)s
                ? "\\*?\\*?" + punctuationRex + wildcardRex + punctuationRex + "\\*?\\*?"
                : punctuationRex + wildcardRex + punctuationRex;
        final Pattern searchPattern = Pattern.compile(pattern);
        final int relevantStart = selectionStart - 2;
        final int relevantEnd = selectionEnd + 2;
        final Matcher matcher = searchPattern.matcher(initialString).region(Math.max(relevantStart, 0), Math.min(relevantEnd, initialString.length()));

        // if the matcher matches, it's a remove
        if (matcher.find()) {
            matcher.reset();
            final int punctuationLength = punctuation.length();
            final List<Pair<Integer, Integer>> startEnd = new LinkedList<>();
            int removedCount = 0;
            while (matcher.find()) {
                startEnd.add(new Pair<>(matcher.start(), matcher.end()));
                removedCount += punctuationLength;
            }
            // start from the end
            Collections.reverse(startEnd);
            for (Pair<Integer, Integer> item : startEnd) {
                deletePunctuation(editable, punctuationLength, item.first, item.second);
            }
            int offsetAtEnd = 0;
            // depending on if the user has selected the markdown chars, we might need to add an offset to the resulting cursor position
            if (initialString.substring(Math.max(selectionEnd - punctuationLength + 1, 0), Math.min(selectionEnd + 1, initialString.length())).equals(punctuation) ||
                    initialString.substring(selectionEnd, Math.min(selectionEnd + punctuationLength, initialString.length())).equals(punctuation)) {
                offsetAtEnd = punctuationLength;
            }
            return selectionEnd - removedCount * 2 + offsetAtEnd;
            //                                 ^
            //         start+end, need to double
        }

        // do nothing when punctuation is contained only once
        if (Pattern.compile(punctuationRex).matcher(initialString).region(selectionStart, selectionEnd).find()) {
            return selectionEnd;
        }

        // nothing returned so far, so it has to be an insertion
        return insertPunctuation(editable, selectionStart, selectionEnd, punctuation);
    }

    private static void deletePunctuation(Editable editable, int punctuationLength, int start, int end) {
        editable.delete(end - punctuationLength, end);
        editable.delete(start, start + punctuationLength);
    }

    /**
     * @return an {@link Optional<Integer>} of the new cursor position.
     * The return value is only {@link Optional#isPresent()}, if this is an italic edge case.
     */
    @NonNull
    private static Optional<Integer> handleItalicEdgeCase(Editable editable, String editableAsString, int selectionStart, int selectionEnd) {
        // look if selection is bold, this is the only edge case afaik
        final String punctuationRex = Pattern.quote("**");
        final Pattern searchPattern = Pattern.compile("[^*]" + punctuationRex + "([^*])*" + punctuationRex + "[^*]");
        // look the selection expansion by 1 is intended, so the NOT '*' has a chance to match. we don't want to match ***blah***
        final Matcher matcher = searchPattern.matcher(editableAsString).region(Math.max(selectionStart - 1, 0), Math.min(selectionEnd + 1, editableAsString.length()));
        if (matcher.find()) {
            return Optional.of(insertPunctuation(editable, selectionStart, selectionEnd, "*"));
        }
        // look around (3 chars) (NOT '*' + "**"). User might have selected the text only
        if (matcher.region(Math.max(selectionStart - 3, 0), Math.min(selectionEnd + 3, editableAsString.length())).find()) {
            return Optional.of(insertPunctuation(editable, selectionStart, selectionEnd, "*"));
        }
        return Optional.empty();
    }

    private static int insertPunctuation(Editable editable, int firstPosition, int secondPosition, String punctuation) {
        editable.insert(secondPosition, punctuation);
        editable.insert(firstPosition, punctuation);
        return secondPosition + punctuation.length();
    }

    /**
     * Inserts a link into the given {@param editable} from {@param selectionStart} to {@param selectionEnd} and uses the {@param clipboardUrl} if available.
     *
     * @return the new cursor position
     */
    public static int insertLink(@NonNull Editable editable, int selectionStart, int selectionEnd, @Nullable String clipboardUrl) {
        if (selectionStart == selectionEnd) {
            if (selectionStart > 0 && selectionEnd < editable.length()) {
                char start = editable.charAt(selectionStart - 1);
                char end = editable.charAt(selectionEnd);
                if (start == ' ' || end == ' ') {
                    if (start != ' ') {
                        editable.insert(selectionStart, " ");
                        selectionStart += 1;
                    }
                    if (end != ' ') {
                        editable.insert(selectionEnd, " ");
                    }
                    editable.insert(selectionStart, "[](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                    return selectionStart + 1;

                } else {
                    while (start != ' ') {
                        selectionStart--;
                        start = editable.charAt(selectionStart);
                    }
                    selectionStart++;
                    while (end != ' ') {
                        selectionEnd++;
                        end = editable.charAt(selectionEnd);
                    }
                    selectionEnd++;
                    editable.insert(selectionStart, "[");
                    editable.insert(selectionEnd, "](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                    if (clipboardUrl != null) {
                        selectionEnd += clipboardUrl.length();
                    }
                    return selectionEnd + 2;
                }
            } else {
                editable.insert(selectionStart, "[](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
                return selectionStart + 1;
            }

        } else {
            final boolean textToFormatIsLink = TextUtils.indexOf(editable.subSequence(selectionStart, selectionEnd), "http") == 0;
            if (textToFormatIsLink) {
                if (clipboardUrl == null) {
                    editable.insert(selectionEnd, ")");
                    editable.insert(selectionStart, "[](");
                } else {
                    editable.insert(selectionEnd, "](" + clipboardUrl + ")");
                    editable.insert(selectionStart, "[");
                    selectionEnd += clipboardUrl.length();
                }
            } else {
                if (clipboardUrl == null) {
                    editable.insert(selectionEnd, "]()");
                } else {
                    editable.insert(selectionEnd, "](" + clipboardUrl + ")");
                    selectionEnd += clipboardUrl.length();
                }
                editable.insert(selectionStart, "[");
            }
            return textToFormatIsLink && clipboardUrl == null
                    ? selectionStart + 1
                    : selectionEnd + 3;
        }
    }


    public static boolean selectionIsInLink(@NonNull CharSequence text, int start, int end) {
        final Matcher matcher = PATTERN_MARKDOWN_LINK.matcher(text);
        while (matcher.find()) {
            if ((start >= matcher.start() && start < matcher.end()) || (end > matcher.start() && end <= matcher.end())) {
                return true;
            }
        }
        return false;
    }

    public static void searchAndColor(@NonNull Spannable editable, @Nullable CharSequence searchText, @Nullable Integer current, @ColorInt int mainColor, @ColorInt int highlightColor, boolean darkTheme) {
        if (searchText != null) {
            final Matcher m = Pattern
                    .compile(searchText.toString(), Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                    .matcher(editable);

            int i = 1;
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                editable.setSpan(new SearchSpan(mainColor, highlightColor, (current != null && i == current), darkTheme), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                i++;
            }
        }
    }

    /**
     * Removes all spans of {@param spanType} from {@param spannable}.
     */
    public static <T> void removeSpans(@NonNull Spannable spannable, @SuppressWarnings("SameParameterValue") Class<T> spanType) {
        for (T span : spannable.getSpans(0, spannable.length(), spanType)) {
            spannable.removeSpan(span);
        }
    }

    /**
     * @return When the content of the {@param textView} is already of type {@link Spannable}, it will cast and return it directly.
     * Otherwise it will create a new {@link SpannableString} from the content, set this as new content of the {@param textView} and return it.
     */
    public static Spannable getContentAsSpannable(@NonNull TextView textView) {
        final CharSequence content = textView.getText();
        if (content.getClass() == SpannableString.class || content instanceof Spannable) {
            return (Spannable) content;
        } else {
            Log.w(TAG, "Expected " + TextView.class.getSimpleName() + " content to be of type " + Spannable.class.getSimpleName() + ", but was of type " + content.getClass() + ". Search highlighting will be not performant.");
            final Spannable spannableContent = new SpannableString(content);
            textView.setText(spannableContent, TextView.BufferType.SPANNABLE);
            return spannableContent;
        }
    }

    public static String getMarkdownLink(@NonNull String text, @NonNull String url) {
        return "[" + text + "](" + url + ")";
    }

    /**
     * Strips all Markdown from {@param s}
     *
     * @param s Markdown string
     * @return Plain text string
     */
    @NonNull
    public static String removeMarkdown(@Nullable String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        assert s != null;
        final String html = renderer.render(parser.parse(replaceCheckboxesWithEmojis(s)));
        final Spanned spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT);
        return spanned.toString().trim();
    }
}
