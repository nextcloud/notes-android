package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;

public class DisplayUtils {

    private DisplayUtils() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    public static List<NavigationItem.CategoryNavigationItem> convertToCategoryNavigationItem(@NonNull Context context, @NonNull Collection<CategoryWithNotesCount> counter) {
        return counter.stream()
                .map(ctr -> convertToCategoryNavigationItem(context, ctr))
                .collect(Collectors.toList());
    }

    public static NavigationItem.CategoryNavigationItem convertToCategoryNavigationItem(@NonNull Context context, @NonNull CategoryWithNotesCount counter) {
        Resources res = context.getResources();
        String category = counter.getCategory().toLowerCase();
        int icon = NavigationAdapter.ICON_FOLDER;
        if (category.equals(res.getString(R.string.category_music).toLowerCase())) {
            icon = R.drawable.ic_library_music_grey600_24dp;
        } else if (category.equals(res.getString(R.string.category_movies).toLowerCase()) || category.equals(res.getString(R.string.category_movie).toLowerCase())) {
            icon = R.drawable.ic_local_movies_grey600_24dp;
        } else if (category.equals(res.getString(R.string.category_work).toLowerCase())) {
            icon = R.drawable.ic_work_grey600_24dp;
        }
        return new NavigationItem.CategoryNavigationItem("category:" + counter.getCategory(), counter.getCategory(), counter.getTotalNotes(), icon, counter.getAccountId(), counter.getCategory());
    }

    /**
     * Detect if the soft keyboard is open.
     * On API prior to 30 we fall back to workaround which might be less reliable
     *
     * @param parentView View
     * @return keyboardVisibility Boolean
     */
    public static boolean isSoftKeyboardVisible(@NonNull View parentView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(parentView);
            if(insets != null){
                return insets.isVisible(WindowInsets.Type.ime());
            }
        }
        //Fall Back to workaround

        //Arbitrary keyboard height
        final int defaultKeyboardHeightDP = 100;
        final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
        final Rect rect = new Rect();

        int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
        parentView.getWindowVisibleDisplayFrame(rect);
        int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
        return heightDiff >= estimatedKeyboardHeight;
    }
}
