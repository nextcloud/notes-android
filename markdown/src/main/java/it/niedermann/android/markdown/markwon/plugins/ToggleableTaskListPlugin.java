package it.niedermann.android.markdown.markwon.plugins;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Range;

import androidx.annotation.NonNull;

import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.SpannableBuilder;
import io.noties.markwon.ext.tasklist.TaskListItem;
import io.noties.markwon.ext.tasklist.TaskListProps;
import io.noties.markwon.ext.tasklist.TaskListSpan;
import it.niedermann.android.markdown.markwon.span.ToggleTaskListSpan;

/**
 * @see <a href="https://github.com/noties/Markwon/issues/196#issuecomment-751680138">Support from upstream</a>
 * @see <a href="https://github.com/noties/Markwon/blob/910bf311dac1bade400616a00ab0c9b7b7ade8cb/app-sample/src/main/java/io/noties/markwon/app/samples/tasklist/TaskListMutateNestedSample.kt">Original kotlin implementation</a>
 */
public class ToggleableTaskListPlugin extends AbstractMarkwonPlugin {

    private static final String TAG = ToggleableTaskListPlugin.class.getSimpleName();

    @NonNull
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    @NonNull
    private final BiConsumer<Integer, Boolean> toggleListener;

    public ToggleableTaskListPlugin(@NonNull BiConsumer<Integer, Boolean> toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(TaskListItem.class, (visitor, node) -> {
            final int length = visitor.length();
            visitor.visitChildren(node);
            TaskListProps.DONE.set(visitor.renderProps(), node.isDone());
            final SpanFactory spanFactory = visitor.configuration()
                    .spansFactory()
                    .get(TaskListItem.class);
            final Object spans = spanFactory == null ? null :
                    spanFactory.getSpans(visitor.configuration(), visitor.renderProps());
            SpannableBuilder.setSpans(
                    visitor.builder(),
                    spans,
                    length,
                    visitor.length()
            );

            if (visitor.hasNext(node)) {
                visitor.ensureNewLine();
            }
        });
    }

    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        super.afterRender(node, visitor);
        final Spannable spanned = visitor.builder().spannableStringBuilder();
        final List<SpannableBuilder.Span> spans = visitor.builder().getSpans(0, visitor.builder().length());
        final List<TaskListSpan> taskListSpans = spans.stream()
                .filter(span -> span.what instanceof TaskListSpan)
                .map(span -> ((TaskListSpan) span.what))
                .sorted((o1, o2) -> spanned.getSpanStart(o1) - spanned.getSpanStart(o2))
                .collect(Collectors.toList());

        for (int position = 0; position < taskListSpans.size(); position++) {
            final TaskListSpan taskListSpan = taskListSpans.get(position);
            final int start = spanned.getSpanStart(taskListSpan);
//            final int contentLength = TaskListContextVisitor.contentLength(node);
//            final int end = start + contentLength;
            final int end = spanned.getSpanEnd(taskListSpan);
            final List<Range<Integer>> freeRanges = findFreeRanges(spanned, start, end);
            for (Range<Integer> freeRange : freeRanges) {
                visitor.builder().setSpan(
                        new ToggleTaskListSpan(enabled, toggleListener, taskListSpan, position),
                        freeRange.getLower(), freeRange.getUpper());
            }
        }
    }

    /**
     * @return a list of ranges in the given {@param spanned} from {@param start} to {@param end} which is <strong>not</strong> taken for a {@link ClickableSpan}.
     */
    @NonNull
    private static List<Range<Integer>> findFreeRanges(@NonNull Spannable spanned, int start, int end) {
        final List<Range<Integer>> freeRanges;
        final List<ClickableSpan> clickableSpans = getClickableSpans(spanned, start, end);
        if (clickableSpans.size() > 0) {
            freeRanges = new ArrayList<>(clickableSpans.size());
            int from = start;
            for (ClickableSpan clickableSpan : clickableSpans) {
                final int clickableStart = spanned.getSpanStart(clickableSpan);
                final int clickableEnd = spanned.getSpanEnd(clickableSpan);
                if (from != clickableStart) {
                    freeRanges.add(new Range<>(from, clickableStart));
                }
                from = clickableEnd;
            }
            if (clickableSpans.size() > 0) {
                final int lastUpperBlocker = spanned.getSpanEnd(clickableSpans.get(clickableSpans.size() - 1));
                if (lastUpperBlocker < end) {
                    freeRanges.add(new Range<>(lastUpperBlocker, end));
                }
            }
        } else {
            freeRanges = Collections.singletonList(new Range<>(start, end));
        }
        return freeRanges;
    }

    @NonNull
    private static List<ClickableSpan> getClickableSpans(@NonNull Spannable spanned, int start, int end) {
        return Arrays.stream(spanned.getSpans(start, end, ClickableSpan.class))
                .sorted((o1, o2) -> spanned.getSpanStart(o1) - spanned.getSpanStart(o2))
                .collect(Collectors.toList());
    }

//    static class TaskListContextVisitor extends AbstractVisitor {
//        private int contentLength = 0;
//
//        static int contentLength(Node node) {
//            final TaskListContextVisitor visitor = new TaskListContextVisitor();
//            visitor.visitChildren(node);
//            return visitor.contentLength;
//        }
//
//        @Override
//        public void visit(Text text) {
//            super.visit(text);
//            contentLength += text.getLiteral().length();
//        }
//
//        // NB! if count both soft and hard breaks as having length of 1
//        @Override
//        public void visit(SoftLineBreak softLineBreak) {
//            super.visit(softLineBreak);
//            contentLength += 1;
//        }
//
//        // NB! if count both soft and hard breaks as having length of 1
//        @Override
//        public void visit(HardLineBreak hardLineBreak) {
//            super.visit(hardLineBreak);
//            contentLength += 1;
//        }
//
//        @Override
//        protected void visitChildren(Node parent) {
//            Node node = parent.getFirstChild();
//            while (node != null) {
//                // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
//                // node after visiting it. So get the next node before visiting.
//                Node next = node.getNext();
//                if (node instanceof Block && !(node instanceof Paragraph)) {
//                    break;
//                }
//                node.accept(this);
//                node = next;
//            }
//        }
//    }
}
