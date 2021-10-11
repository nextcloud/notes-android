package it.niedermann.android.markdown.markwon.plugins;

import static java.util.Comparator.comparingInt;

import android.text.style.ClickableSpan;
import android.util.Range;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Block;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.SpannableBuilder;
import io.noties.markwon.SpannableBuilder.Span;
import io.noties.markwon.ext.tasklist.TaskListItem;
import io.noties.markwon.ext.tasklist.TaskListProps;
import io.noties.markwon.ext.tasklist.TaskListSpan;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.markwon.span.ToggleTaskListSpan;

/**
 * @see <a href="https://github.com/noties/Markwon/issues/196#issuecomment-751680138">Support from upstream</a>
 * @see <a href="https://github.com/noties/Markwon/blob/910bf311dac1bade400616a00ab0c9b7b7ade8cb/app-sample/src/main/java/io/noties/markwon/app/samples/tasklist/TaskListMutateNestedSample.kt">Original kotlin implementation</a>
 */
public class ToggleableTaskListPlugin extends AbstractMarkwonPlugin {

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

    /**
     * Prepares {@link TaskListSpan}s and marks each one with a {@link ToggleMarkerSpan} in the first step.
     * The {@link ToggleMarkerSpan} are different from {@link TaskListSpan}s as they will stop on nested tasks instead of spanning the whole tasks including its subtasks.
     */
    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(TaskListItem.class, (visitor, node) -> {
            final int length = visitor.length();
            visitor.visitChildren(node);
            TaskListProps.DONE.set(visitor.renderProps(), node.isDone());
            final var spanFactory = visitor.configuration()
                    .spansFactory()
                    .get(TaskListItem.class);
            final var spans = spanFactory == null ? null :
                    spanFactory.getSpans(visitor.configuration(), visitor.renderProps());
            if (spans != null) {
                final TaskListSpan taskListSpan;
                if (spans instanceof TaskListSpan[]) {
                    if (((TaskListSpan[]) spans).length > 0) {
                        taskListSpan = ((TaskListSpan[]) spans)[0];
                    } else {
                        taskListSpan = null;
                    }
                } else if (spans instanceof TaskListSpan) {
                    taskListSpan = (TaskListSpan) spans;
                } else {
                    taskListSpan = null;
                }

                final int content = TaskListContextVisitor.contentLength(node);
                if (content > 0 && taskListSpan != null) {
                    // maybe additionally identify this task list (for persistence)
                    visitor.builder().setSpan(
                            new ToggleMarkerSpan(taskListSpan),
                            length,
                            length + content
                    );
                }
            }
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


    /**
     * Adds for each symbolic {@link ToggleMarkerSpan} an actual {@link ToggleTaskListSpan}s respecting existing {@link ClickableSpan}s.
     */
    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        super.afterRender(node, visitor);

        final var markerSpans = getSortedSpans(visitor.builder(), ToggleMarkerSpan.class, 0, visitor.builder().length());

        for (int position = 0; position < markerSpans.size(); position++) {
            final var markerSpan = markerSpans.get(position);
            final int start = markerSpan.start;
            final int end = markerSpan.end;
            final var freeRanges = findFreeRanges(visitor.builder(), start, end);
            for (Range<Integer> freeRange : freeRanges) {
                visitor.builder().setSpan(
                        new ToggleTaskListSpan(enabled, toggleListener, ((ToggleMarkerSpan) markerSpan.what).getTaskListSpan(), position),
                        freeRange.getLower(), freeRange.getUpper());
            }
        }
    }

    /**
     * Removes {@link ToggleMarkerSpan}s from {@param textView}.
     */
    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        final var spannable = MarkdownUtil.getContentAsSpannable(textView);
        for (final var span : spannable.getSpans(0, spannable.length(), ToggleMarkerSpan.class)) {
            spannable.removeSpan(span);
        }
        textView.setText(spannable);
    }

    /**
     * @return a {@link List} of {@link Range}s in the given {@param spanned} from {@param start} to {@param end} which is <strong>not</strong> taken for a {@link ClickableSpan}.
     */
    @NonNull
    private static Collection<Range<Integer>> findFreeRanges(@NonNull SpannableBuilder builder, int start, int end) {
        final List<Range<Integer>> freeRanges;
        final var clickableSpans = getSortedSpans(builder, ClickableSpan.class, start, end);
        if (clickableSpans.size() > 0) {
            freeRanges = new LinkedList<>();
            int from = start;
            for (final var clickableSpan : clickableSpans) {
                final int clickableStart = clickableSpan.start;
                final int clickableEnd = clickableSpan.end;
                if (from < clickableStart) {
                    freeRanges.add(new Range<>(from, clickableStart));
                }
                from = clickableEnd;
            }
            if (clickableSpans.size() > 0) {
                final int lastUpperBlocker = clickableSpans.get(clickableSpans.size() - 1).end;
                if (lastUpperBlocker < end) {
                    freeRanges.add(new Range<>(lastUpperBlocker, end));
                }
            }
        } else if (start == end) {
            freeRanges = Collections.emptyList();
        } else {
            freeRanges = Collections.singletonList(new Range<>(start, end));
        }
        return freeRanges;
    }

    /**
     * @return a {@link List} of {@link Span}s holding {@param type}s, sorted ascending by the span start.
     */
    private static <T> List<Span> getSortedSpans(@NonNull SpannableBuilder builder, @NonNull Class<T> type, int start, int end) {
        return builder.getSpans(start, end)
                .stream()
                .filter(span -> type.isInstance(span.what))
                .sorted(comparingInt(o -> o.start))
                .collect(Collectors.toList());
    }

    private static final class TaskListContextVisitor extends AbstractVisitor {
        private int contentLength = 0;

        static int contentLength(Node node) {
            final var visitor = new TaskListContextVisitor();
            visitor.visitChildren(node);
            return visitor.contentLength;
        }

        @Override
        public void visit(Text text) {
            super.visit(text);
            contentLength += text.getLiteral().length();
        }

        // NB! if count both soft and hard breaks as having length of 1
        @Override
        public void visit(SoftLineBreak softLineBreak) {
            super.visit(softLineBreak);
            contentLength += 1;
        }

        // NB! if count both soft and hard breaks as having length of 1
        @Override
        public void visit(HardLineBreak hardLineBreak) {
            super.visit(hardLineBreak);
            contentLength += 1;
        }

        @Override
        protected void visitChildren(Node parent) {
            var node = parent.getFirstChild();
            while (node != null) {
                // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
                // node after visiting it. So get the next node before visiting.
                final var next = node.getNext();
                if (node instanceof Block && !(node instanceof Paragraph)) {
                    break;
                }
                node.accept(this);
                node = next;
            }
        }
    }

    /**
     * Helper class which holds an {@link TaskListSpan} but does not include the range of child {@link TaskListSpan}s.
     */
    @VisibleForTesting
    static final class ToggleMarkerSpan {

        @NonNull
        private final TaskListSpan taskListSpan;

        private ToggleMarkerSpan(@NonNull TaskListSpan taskListSpan) {
            this.taskListSpan = taskListSpan;
        }

        @NonNull
        private TaskListSpan getTaskListSpan() {
            return taskListSpan;
        }
    }
}
