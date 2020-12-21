package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

abstract public class InterceptorTextWatcher implements TextWatcher {

    @NonNull
    protected final TextWatcher originalWatcher;

    public InterceptorTextWatcher(@NonNull TextWatcher originalWatcher) {
        this.originalWatcher = originalWatcher;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        this.originalWatcher.beforeTextChanged(s, start, count, after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.originalWatcher.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        this.originalWatcher.afterTextChanged(s);
    }
}
