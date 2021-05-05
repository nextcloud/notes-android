package it.niedermann.android.markdown;

import android.text.Editable;
import android.text.Html;
import android.text.style.BulletSpan;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.XMLReader;

import java.util.Stack;

/**
 * Adds <code>•</code> to unordered list items and a counter to ordered list items.
 * Call {@link #prepareTagHandling(String)}, so the default handler of {@link Html#fromHtml(String, int)} does not prevent the handling.
 */
public class ListTagHandler implements Html.TagHandler {

    private static final String X_OL = "x-ol";
    private static final String X_UL = "x-ul";
    private static final String X_LI = "x-li";

    private final Stack<String> parents = new Stack<>();
    private final Stack<Integer> listItemIndex = new Stack<>();

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (X_OL.equals(tag)) {
            if (opening) {
                parents.push(X_OL);
                listItemIndex.push(1);
            } else {
                parents.pop();
                listItemIndex.pop();
            }
        } else if (X_UL.equals(tag)) {
            if (opening) {
                parents.push(X_UL);
            } else {
                parents.pop();
            }
        } else if (X_LI.equals(tag)) {
            if (X_OL.equals(parents.peek())) {
                if (opening) {
                    output.append("\n");
                    for (int nestingLevel = 1; nestingLevel < parents.size(); nestingLevel++) {
                        output.append("\t\t");
                    }
                    output.append(String.valueOf(listItemIndex.peek())).append(". ");
                    listItemIndex.push(listItemIndex.pop() + 1);
                }
            } else if (X_UL.equals(parents.peek())) {
                if (opening) {
                    output.append("\n");
                    for (int nestingLevel = 1; nestingLevel < parents.size(); nestingLevel++) {
                        output.append("\t\t");
                    }
                    output.append("•");
                }
            }
        }
    }

    /**
     * Replace the list tags with custom tags to prevent them being handeled by {@link HtmlCompat}.
     * Otherwise, all <code>li</code> tags will be replaced with {@link BulletSpan} which is not the
     * desired behavior of ordered list items.
     */
    @NonNull
    public static String prepareTagHandling(@NonNull String html) {
        final Document document = Jsoup.parse(html);
        document.getElementsByTag("ol").tagName(X_OL);
        document.getElementsByTag("ul").tagName(X_UL);
        document.getElementsByTag("li").tagName(X_LI);
        return document.outerHtml();
    }
}