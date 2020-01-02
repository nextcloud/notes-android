package it.niedermann.owncloud.notes.util;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDConfiguration.Builder;

import it.niedermann.owncloud.notes.R;

/**
 * Created by stefan on 07.12.16.
 */

public class MarkDownUtil {

    /**
     * Ensures every instance of RxMD uses the same configuration
     *
     * @param context Context
     * @return RxMDConfiguration
     */
    public static Builder getMarkDownConfiguration(Context context) {
        return getMarkDownConfiguration(context, Notes.getAppTheme(context));
    }

    public static Builder getMarkDownConfiguration(Context context, Boolean darkTheme) {
        return new RxMDConfiguration.Builder(context)
                .setUnOrderListColor(ResourcesCompat.getColor(context.getResources(),
                        darkTheme ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setHeader2RelativeSize(1.35f)
                .setHeader3RelativeSize(1.25f)
                .setHeader4RelativeSize(1.15f)
                .setHeader5RelativeSize(1.1f)
                .setHeader6RelativeSize(1.05f)
                .setHorizontalRulesHeight(2)
                .setTodoColor(ResourcesCompat.getColor(context.getResources(),
                        Notes.getAppTheme(context) ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setTodoDoneColor(ResourcesCompat.getColor(context.getResources(),
                        Notes.getAppTheme(context) ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setLinkFontColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
    }
}
