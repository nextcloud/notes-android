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
import android.text.style.BulletSpan;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import it.niedermann.android.markdown.model.EListType;
import it.niedermann.android.markdown.model.SearchSpan;
import it.niedermann.android.util.DimensionUtil;

public class MarkdownUtil {

    private static final String TAG = MarkdownUtil.class.getSimpleName();

    private final static Parser parser = Parser.builder().build();
    private final static HtmlRenderer renderer = HtmlRenderer.builder().softbreak("<br>").build();

    private static final Pattern PATTERN_LISTS = Pattern.compile("^\\s*[*+-]\\s+", Pattern.MULTILINE);
    private static final Pattern PATTERN_HEADINGS = Pattern.compile("^#+\\s+(.*?)\\s*#*$", Pattern.MULTILINE);
    private static final Pattern PATTERN_HEADING_LINE = Pattern.compile("^(?:=*|-*)$", Pattern.MULTILINE);
    private static final Pattern PATTERN_EMPHASIS = Pattern.compile("(\\*+|_+)(.*?)\\1", Pattern.MULTILINE);
    private static final Pattern PATTERN_SPACE_1 = Pattern.compile("^\\s+", Pattern.MULTILINE);
    private static final Pattern PATTERN_SPACE_2 = Pattern.compile("\\s+$", Pattern.MULTILINE);

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
        final String html = renderer.render(parser.parse(replaceCheckboxesWithEmojis(content)));
        return enhanceRenderedResult(context, HtmlCompat.fromHtml(html, 0));
    }

    private static Spanned enhanceRenderedResult(@NonNull Context context, @NonNull Spanned input) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final QuoteSpan[] originalQuoteSpans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
            @ColorInt final int colorBlockQuote = ContextCompat.getColor(context, R.color.block_quote);
            for (QuoteSpan originalQuoteSpan : originalQuoteSpans) {
                final int start = ssb.getSpanStart(originalQuoteSpan);
                final int end = ssb.getSpanEnd(originalQuoteSpan);
                ssb.removeSpan(originalQuoteSpan);
                ssb.setSpan(new QuoteSpan(colorBlockQuote, 5, 30), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            final BulletSpan[] originalBulletSpans = ssb.getSpans(0, ssb.length(), BulletSpan.class);
            @ColorInt final int colorBulletPoint = ContextCompat.getColor(context, R.color.bullet_point);
            for (BulletSpan originalQuoteSpan : originalBulletSpans) {
                final int start = ssb.getSpanStart(originalQuoteSpan);
                final int end = ssb.getSpanEnd(originalQuoteSpan);
                ssb.removeSpan(originalQuoteSpan);
                ssb.setSpan(new BulletSpan(16, colorBulletPoint, 8), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        if (Build.MANUFACTURER != null && Build.MANUFACTURER.toLowerCase().contains("samsung")) {
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
        return (trimmedLine.startsWith(listType.checkboxUnchecked) || trimmedLine.startsWith(listType.checkboxChecked));
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
        switch (punctuation) {
            case "**":
            case "__":
            case "*":
            case "_":
            case "~~": {
                final boolean selectionIsSurroundedByPunctuation = selectionIsSurroundedByPunctuation(editable, selectionStart, selectionEnd, punctuation);
                if (selectionIsSurroundedByPunctuation) {
                    editable.delete(selectionEnd, selectionEnd + punctuation.length());
                    editable.delete(selectionStart - punctuation.length(), selectionStart);
                    return selectionEnd - punctuation.length();
                } else {
                    final int containedPunctuationCount = getContainedPunctuationCount(editable, selectionStart, selectionEnd, punctuation);
                    if (containedPunctuationCount == 0) {
                        editable.insert(selectionEnd, punctuation);
                        editable.insert(selectionStart, punctuation);
                        return selectionEnd + punctuation.length() * 2;
                    } else if (containedPunctuationCount % 2 > 0) {
                        return selectionEnd;
                    } else {
                        removeContainingPunctuation(editable, selectionStart, selectionEnd, punctuation);
                        return selectionEnd - containedPunctuationCount * punctuation.length();
                    }
                }
            }
            default:
                throw new UnsupportedOperationException("This kind of punctuation is not yet supported: " + punctuation);
        }
    }

    /**
     * Inserts a link into the given {@param editable} from {@param selectionStart} to {@param selectionEnd} and uses the {@param clipboardUrl} if available.
     *
     * @return the new cursor position
     */
    public static int insertLink(@NonNull Editable editable, int selectionStart, int selectionEnd, @Nullable String clipboardUrl) {
        if (selectionStart == selectionEnd) {
            editable.insert(selectionStart, "[](" + (clipboardUrl == null ? "" : clipboardUrl) + ")");
            return selectionStart + 1;
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

    /**
     * @return whether or not the selection of {@param text} from {@param start} to {@param end} is
     * surrounded or not by the given {@param punctuation}.
     */
    private static boolean selectionIsSurroundedByPunctuation(@NonNull CharSequence text, int start, int end, @NonNull String punctuation) {
        if (text.length() < end + punctuation.length()) {
            return false;
        }
        if (start - punctuation.length() < 0 || end + punctuation.length() > text.length()) {
            return false;
        }
        return punctuation.contentEquals(text.subSequence(start - punctuation.length(), start))
                && punctuation.contentEquals(text.subSequence(end, end + punctuation.length()));
    }

    private static int getContainedPunctuationCount(@NonNull CharSequence text, int start, int end, @NonNull String punctuation) {
        final Matcher matcher = Pattern.compile(Pattern.quote(punctuation)).matcher(text.subSequence(start, end));
        int counter = 0;
        while (matcher.find()) {
            counter++;
        }
        return counter;
    }

    private static void removeContainingPunctuation(@NonNull Editable editable, int start, int end, @NonNull String punctuation) {
        final Matcher matcher = Pattern.compile(Pattern.quote(punctuation)).matcher(editable.subSequence(start, end));
        int countDeletedPunctuations = 0;
        while (matcher.find()) {
            editable.delete(start + matcher.start() - countDeletedPunctuations * punctuation.length(), start + matcher.end() - countDeletedPunctuations * punctuation.length());
            countDeletedPunctuations++;
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
        if (s == null)
            return "";
        // TODO maybe we can utilize the markwon renderer?

        for (EListType listType : EListType.values()) {
            s = s.replace(listType.checkboxChecked, "");
            s = s.replace(listType.checkboxUnchecked, "");
            s = s.replace(listType.listSymbolWithTrailingSpace, "");
        }
        s = PATTERN_LISTS.matcher(s).replaceAll("");
        s = PATTERN_HEADINGS.matcher(s).replaceAll("$1");
        s = PATTERN_HEADING_LINE.matcher(s).replaceAll("");
        s = PATTERN_EMPHASIS.matcher(s).replaceAll("$2");
        s = PATTERN_SPACE_1.matcher(s).replaceAll("");
        s = PATTERN_SPACE_2.matcher(s).replaceAll("");
        return s;
    }
}
