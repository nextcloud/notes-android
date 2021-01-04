package it.niedermann.android.markdown.markwon;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;
import it.niedermann.android.markdown.markwon.model.EListType;
import it.niedermann.android.markdown.markwon.plugins.LinkClickInterceptorPlugin;
import it.niedermann.android.markdown.markwon.plugins.NextcloudMentionsPlugin;
import it.niedermann.android.markdown.markwon.plugins.SearchHighlightPlugin;
import it.niedermann.android.markdown.markwon.plugins.ThemePlugin;
import it.niedermann.android.markdown.markwon.span.SearchSpan;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
@PrismBundle(
        include = {
                "c", "clike", "clojure", "cpp", "csharp", "css", "dart", "git", "go", "groovy", "java", "javascript", "json",
                "kotlin", "latex", "makefile", "markdown", "markup", "python", "scala", "sql", "swift", "yaml"
        },
        grammarLocatorClassName = ".MarkwonGrammarLocator"
)
public class MarkwonMarkdownUtil {

    private static final String TAG = MarkwonMarkdownUtil.class.getSimpleName();
    private static final Pattern PATTERN_ORDERED_LIST_ITEM = Pattern.compile("^(\\d+).\\s.+$");
    private static final Pattern PATTERN_ORDERED_LIST_ITEM_EMPTY = Pattern.compile("^(\\d+).\\s$");
    private static final Pattern PATTERN_MARKDOWN_LINK = Pattern.compile("\\[(.+)?]\\(([^ ]+?)?( \"(.+)\")?\\)");

    private MarkwonMarkdownUtil() {
        // Util class
    }

    public static Markwon.Builder initMarkwonEditor(@NonNull Context context) {
        return Markwon.builder(context)
                .usePlugin(ThemePlugin.create(context))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SimpleExtPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(SearchHighlightPlugin.create(context));
    }

    public static Markwon.Builder initMarkwonViewer(@NonNull Context context) {
        final Prism4j prism4j = new Prism4j(new MarkwonGrammarLocator());
        final Prism4jTheme prism4jTheme = isDarkThemeActive(context)
                ? Prism4jThemeDarkula.create()
                : Prism4jThemeDefault.create();
        return initMarkwonEditor(context)
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(LinkifyPlugin.create(true))
                .usePlugin(LinkClickInterceptorPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, prism4jTheme));
    }

    public static Markwon.Builder initMarkwonViewer(@NonNull Context context, @NonNull Map<String, String> mentions) {
        return initMarkwonViewer(context)
                .usePlugin(NextcloudMentionsPlugin.create(context, mentions));
    }

    private static boolean isDarkThemeActive(@NonNull Context context) {
        final int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
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
        for (int i = 0; i < lines.length; i++) {
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

    public static void searchAndColor(@NonNull Spannable editable, @Nullable CharSequence searchText, @NonNull Context context, @Nullable Integer current, @ColorInt int mainColor) {
        if (searchText != null) {
            final Matcher m = Pattern
                    .compile(searchText.toString(), Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
                    .matcher(editable);

            int i = 1;
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                editable.setSpan(new SearchSpan(context, mainColor, (current != null && i == current)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                i++;
            }
        }
    }

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
}
