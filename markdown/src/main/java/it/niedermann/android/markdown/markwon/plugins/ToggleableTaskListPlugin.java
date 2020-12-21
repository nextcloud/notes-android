package it.niedermann.android.markdown.markwon.plugins;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.ext.tasklist.TaskListItem;
import io.noties.markwon.ext.tasklist.TaskListSpan;

public class ToggleableTaskListPlugin extends AbstractMarkwonPlugin {

    private final String originalNoteContent;
    private final ToggleListener toggleListener;
    public static final String CHECKBOX_UNCHECKED_PLUS = "+ [ ]";
    public static final String CHECKBOX_UNCHECKED_MINUS = "- [ ]";
    public static final String CHECKBOX_UNCHECKED_STAR = "* [ ]";
    public static final String CHECKBOX_CHECKED_PLUS = "+ [x]";
    public static final String CHECKBOX_CHECKED_MINUS = "- [x]";
    public static final String CHECKBOX_CHECKED_STAR = "* [x]";

    public ToggleableTaskListPlugin(String originalNoteContent, ToggleListener toggleListener) {
        this.originalNoteContent = originalNoteContent;
        this.toggleListener = toggleListener;
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        SpanFactory origin = builder.getFactory(TaskListItem.class);

        builder.setFactory(TaskListItem.class, (configuration, props) -> {
            TaskListSpan span = (TaskListSpan) origin.getSpans(configuration, props);
            ClickableSpan c = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Log.v("checkbox", "abcdef");
                    span.setDone(!span.isDone());
                    widget.invalidate();

                    // it must be a TextView
                    final TextView textView = (TextView) widget;
                    // it must be spanned
                    final Spanned spanned = (Spanned) textView.getText();

                    // actual text of the span (this can be used along with the  `span`)
                    final CharSequence task = spanned.subSequence(
                            spanned.getSpanStart(this),
                            spanned.getSpanEnd(this)
                    );

                    int lineNumber = 0;

                    CharSequence textBeforeTask = spanned.subSequence(0, spanned.getSpanStart(this));
                    for (int i = 0; i < textBeforeTask.length(); i++) {
                        if (textBeforeTask.charAt(i) == '\n')
                            lineNumber++;
                    }

                    // Work on the original content now, because the previous stuff is rendered and inline markdown might be removed at this point

                    String[] lines = TextUtils.split(originalNoteContent, "\\r?\\n");
                    /*
                     * When (un)checking a checkbox in a note which contains code-blocks, the "`"-characters get stripped out in the TextView and therefore the given lineNumber is wrong
                     * Find number of lines starting with ``` before lineNumber
                     */
                    // TODO Maybe one can simpliy write i < lineNumber?
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].startsWith("```")) {
                            lineNumber++;
                        }
                        if (i == lineNumber) {
                            break;
                        }
                    }

                    if (lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_MINUS) || lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_STAR) || lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_PLUS)) {
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_MINUS, CHECKBOX_CHECKED_MINUS);
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_STAR, CHECKBOX_CHECKED_STAR);
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_PLUS, CHECKBOX_CHECKED_PLUS);
                    } else {
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_MINUS, CHECKBOX_UNCHECKED_MINUS);
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_STAR, CHECKBOX_UNCHECKED_STAR);
                        lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_PLUS, CHECKBOX_UNCHECKED_PLUS);
                    }

                    toggleListener.onToggled(TextUtils.join("\n", lines));
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    //NoOp
                }
            };
            return new Object[]{span, c};
        });
    }

    public interface ToggleListener {
        public void onToggled(String newCompmleteText);
    }
}
