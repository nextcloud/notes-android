package it.niedermann.android.markdown.markwon.plugins;

import android.util.Log;

import androidx.annotation.NonNull;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Block;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

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

                Log.i(TAG, visitor.builder().subSequence(length, visitor.builder().length()).toString());
                int content = TaskListContextVisitor.contentLength(node);
                Log.i(TAG, "content: " + content + ", '" + visitor.builder().subSequence(length, length + content) + "'");

                if (content > 0 && taskListSpan != null) {
                    // maybe additionally identify this task list (for persistence)
                    visitor.builder().setSpan(
                            new ToggleTaskListSpan(enabled, toggleListener, taskListSpan, visitor.builder().subSequence(length, length + content).toString()),
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

    static class TaskListContextVisitor extends AbstractVisitor {
        private int contentLength = 0;

        static int contentLength(Node node) {
            final TaskListContextVisitor visitor = new TaskListContextVisitor();
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
            Node node = parent.getFirstChild();
            while (node != null) {
                // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
                // node after visiting it. So get the next node before visiting.
                Node next = node.getNext();
                if (node instanceof Block && !(node instanceof Paragraph)) {
                    break;
                }
                node.accept(this);
                node = next;
            }
        }
    }
}
