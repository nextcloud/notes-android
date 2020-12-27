package it.niedermann.android.markdown.markwon.plugins;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import java.util.Arrays;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.ext.tasklist.TaskListItem;
import io.noties.markwon.ext.tasklist.TaskListSpan;

public class ToggleableTaskListPlugin extends AbstractMarkwonPlugin {

    @NonNull
    private final Consumer<Integer> toggleListener;

    public ToggleableTaskListPlugin(@NonNull Consumer<Integer> toggleListener) {
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

                    toggleListener.accept(currentTogglePosition);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    //NoOp
                }
            };
            return new Object[]{span, c};
        });
    }
}
