package it.niedermann.android.markdown.markwon.span;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import io.noties.markwon.ext.tasklist.TaskListSpan;

public class ToggleTaskListSpan extends ClickableSpan {

    private static final String TAG = ToggleTaskListSpan.class.getSimpleName();

    final AtomicBoolean enabled;
    final BiConsumer<Integer, Boolean> toggleListener;
    final TaskListSpan span;
    final String content;

    public ToggleTaskListSpan(@NonNull AtomicBoolean enabled, @NonNull BiConsumer<Integer, Boolean> toggleListener, @NonNull TaskListSpan span, String content) {
        this.enabled = enabled;
        this.toggleListener = toggleListener;
        this.span = span;
        this.content = content;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if(enabled.get()) {
            span.setDone(!span.isDone());
            widget.invalidate();
            Log.v(TAG, "task-list click, isDone: " + span.isDone() + ", content: '" + content + "'");

            // it must be a TextView
            final TextView textView = (TextView) widget;
            // it must be spanned
            // TODO what if textView is not a spanned?
            final Spanned spanned = (Spanned) textView.getText();

            final ClickableSpan[] toggles = spanned.getSpans(0, spanned.length(), getClass());
            Arrays.sort(toggles, (o1, o2) -> spanned.getSpanStart(o1) - spanned.getSpanStart(o2));

            int currentTogglePosition = -1;
            for (int i = 0; i < toggles.length; i++) {
                if (spanned.getSpanStart(toggles[i]) == spanned.getSpanStart(this) && spanned.getSpanEnd(toggles[i]) == spanned.getSpanEnd(this)) {
                    currentTogglePosition = i;
                    break;
                }
            }

            toggleListener.accept(currentTogglePosition, span.isDone());
        } else {
            Log.w(TAG, "Prevented toggling checkbox because the view is disabled");
        }
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        // NoOp to remove underline text decoration
    }
}