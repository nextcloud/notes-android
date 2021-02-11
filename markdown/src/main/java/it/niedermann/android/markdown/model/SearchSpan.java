package it.niedermann.android.markdown.model;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import it.niedermann.android.util.ColorUtil;

public class SearchSpan extends MetricAffectingSpan {

    private final boolean current;
    @ColorInt
    private final int mainColor;
    @ColorInt
    private final int highlightColor;
    private final boolean darkTheme;

    public SearchSpan(@ColorInt int mainColor, @ColorInt int highlightColor, boolean current, boolean darkTheme) {
        this.mainColor = mainColor;
        this.current = current;
        this.highlightColor = highlightColor;
        this.darkTheme = darkTheme;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (current) {
            if (darkTheme) {
                if (ColorUtil.INSTANCE.isColorDark(mainColor)) {
                    tp.bgColor = Color.WHITE;
                    tp.setColor(mainColor);
                } else {
                    tp.bgColor = mainColor;
                    tp.setColor(Color.BLACK);
                }
            } else {
                if (ColorUtil.INSTANCE.isColorDark(mainColor)) {
                    tp.bgColor = mainColor;
                    tp.setColor(Color.WHITE);
                } else {
                    if (ColorUtil.INSTANCE.getContrastRatio(mainColor, highlightColor) > 3d) {
                        tp.bgColor = highlightColor;
                    } else {
                        tp.bgColor = Color.BLACK;
                    }
                    tp.setColor(mainColor);
                }
            }
        } else {
            tp.bgColor = highlightColor;
            if (ColorUtil.INSTANCE.getContrastRatio(mainColor, highlightColor) > 3d) {
                tp.setColor(mainColor);
            } else {
                if (darkTheme) {
                    tp.setColor(Color.WHITE);
                } else {
                    tp.setColor(Color.BLACK);
                }
            }
        }
        tp.setFakeBoldText(true);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint tp) {
        tp.setFakeBoldText(true);
    }
}