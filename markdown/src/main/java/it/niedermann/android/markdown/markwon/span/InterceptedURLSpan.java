package it.niedermann.android.markdown.markwon.span;

import android.text.style.URLSpan;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.function.Function;

public class InterceptedURLSpan extends URLSpan {
    @NonNull
    private final Collection<Function<String, Boolean>> onLinkClickCallbacks;

    public InterceptedURLSpan(@NonNull Collection<Function<String, Boolean>> onLinkClickCallbacks, String url) {
        super(url);
        this.onLinkClickCallbacks = onLinkClickCallbacks;
    }

    @Override
    public void onClick(View widget) {
        for (Function<String, Boolean> callback : onLinkClickCallbacks) {
            if (callback.apply(getURL())) {
                return;
            }
        }
        super.onClick(widget);
    }
}