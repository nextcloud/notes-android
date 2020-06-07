package it.niedermann.owncloud.notes.branding;

import androidx.annotation.ColorInt;
import androidx.annotation.UiThread;

public interface Branded {
    @UiThread
    void applyBrand(@ColorInt int mainColor, @ColorInt int textColor);
}