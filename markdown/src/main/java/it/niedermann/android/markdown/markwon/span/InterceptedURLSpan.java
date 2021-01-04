package it.niedermann.android.markdown.markwon.span;

import android.text.style.URLSpan;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InterceptedURLSpan extends URLSpan {
    @NonNull
    private final List<Function<String, Boolean>> onLinkClickCallbacks = new ArrayList<>();

    public InterceptedURLSpan(@NonNull List<Function<String, Boolean>> onLinkClickCallbacks, String url) {
        super(url);
        this.onLinkClickCallbacks.addAll(onLinkClickCallbacks);
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