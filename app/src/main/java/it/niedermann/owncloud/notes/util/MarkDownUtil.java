package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;

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
        return new RxMDConfiguration.Builder(context)
                .setHeader2RelativeSize(1.35f)
                .setHeader3RelativeSize(1.25f)
                .setHeader4RelativeSize(1.15f)
                .setHeader5RelativeSize(1.1f)
                .setHeader6RelativeSize(1.05f)
                .setHorizontalRulesHeight(2)
                .setLinkFontColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null));
    }
}
