package it.niedermann.android.markdown.markwon.plugins;

import androidx.annotation.NonNull;

import org.commonmark.node.Link;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.CoreProps;
import it.niedermann.android.markdown.markwon.span.InterceptedURLSpan;

public class LinkClickInterceptorPlugin extends AbstractMarkwonPlugin {

    @NonNull
    private final Collection<Function<String, Boolean>> onLinkClickCallbacks = new LinkedList<>();

    public static MarkwonPlugin create() {
        return new LinkClickInterceptorPlugin();
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        super.configureSpansFactory(builder);
        builder.setFactory(Link.class, (configuration, props) -> new InterceptedURLSpan(onLinkClickCallbacks, CoreProps.LINK_DESTINATION.get(props)));
    }

    public void registerOnLinkClickCallback(@NonNull Function<String, Boolean> callback) {
        this.onLinkClickCallbacks.add(callback);
    }
}
