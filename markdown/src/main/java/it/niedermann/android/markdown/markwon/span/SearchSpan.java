package it.niedermann.android.markdown.markwon.span;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import it.niedermann.android.markdown.R;
import it.niedermann.android.util.ColorUtil;

public class SearchSpan extends MetricAffectingSpan {

    private final boolean current;
    @NonNull
    Context context;
    @ColorInt
    private final int mainColor;
    @ColorInt
    private final int highlightColor;

    public SearchSpan(@NonNull Context context, @ColorInt int mainColor, boolean current) {
        this.context = context;
        this.mainColor = mainColor;
        this.current = current;
        this.highlightColor = context.getResources().getColor(R.color.bg_highlighted);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (current) {
            if (isDarkThemeActive(context)) {
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
                if (isDarkThemeActive(context)) {
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

    private static boolean isDarkThemeActive(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}