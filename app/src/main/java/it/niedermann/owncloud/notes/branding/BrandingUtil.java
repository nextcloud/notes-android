package it.niedermann.owncloud.notes.branding;

import static it.niedermann.owncloud.notes.NotesApplication.isDarkThemeActive;
import static it.niedermann.owncloud.notes.shared.util.NotesColorUtil.contrastRatioIsSufficient;
import static it.niedermann.owncloud.notes.shared.util.NotesColorUtil.contrastRatioIsSufficientBigAreas;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;
import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;
import com.nextcloud.android.common.ui.theme.utils.AndroidViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.AndroidXViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.DialogViewThemeUtils;
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils;
import com.nextcloud.android.common.ui.util.PlatformThemeUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import it.niedermann.android.sharedpreferences.SharedPreferenceIntLiveData;
import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;
import scheme.Scheme;

public class BrandingUtil extends ViewThemeUtilsBase {

    private static final String TAG = BrandingUtil.class.getSimpleName();
    private static final ConcurrentMap<Integer, BrandingUtil> CACHE = new ConcurrentHashMap<>();
    private static final String pref_key_branding_main = "branding_main";
    private static final String pref_key_branding_text = "branding_text";

    public final AndroidViewThemeUtils platform;
    public final MaterialViewThemeUtils material;
    public final AndroidXViewThemeUtils androidx;
    public final DialogViewThemeUtils dialog;
    public final NotesViewThemeUtils notes;

    private BrandingUtil(
            final MaterialSchemes schemes,
            final com.nextcloud.android.common.ui.color.ColorUtil colorUtil
    ) {
        super(schemes);

        this.platform = new AndroidViewThemeUtils(schemes, colorUtil);
        this.material = new MaterialViewThemeUtils(schemes, colorUtil);
        this.androidx = new AndroidXViewThemeUtils(schemes, this.platform);
        this.dialog = new DialogViewThemeUtils(schemes);
        this.notes = new NotesViewThemeUtils(schemes);
    }

    public static BrandingUtil of(@ColorInt int color, @NonNull Context context) {
        return CACHE.computeIfAbsent(color, c -> new BrandingUtil(
                MaterialSchemes.Companion.fromColor(c),
                new com.nextcloud.android.common.ui.color.ColorUtil(context)
        ));
    }

    public static LiveData<Pair<Integer, Integer>> readBrandColors(@NonNull Context context) {
        return new BrandingLiveData(context);
    }

    private static class BrandingLiveData extends MediatorLiveData<Pair<Integer, Integer>> {
        @ColorInt
        Integer lastMainColor = null;
        @ColorInt
        Integer lastTextColor = null;

        public BrandingLiveData(@NonNull Context context) {
            addSource(readBrandMainColorLiveData(context), (nextMainColor) -> {
                lastMainColor = nextMainColor;
                if (lastTextColor != null) {
                    postValue(new Pair<>(lastMainColor, lastTextColor));
                }
            });
            addSource(readBrandTextColorLiveData(context), (nextTextColor) -> {
                lastTextColor = nextTextColor;
                if (lastMainColor != null) {
                    postValue(new Pair<>(lastMainColor, lastTextColor));
                }
            });
        }
    }

    public static LiveData<Integer> readBrandMainColorLiveData(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_main");
        return new SharedPreferenceIntLiveData(sharedPreferences, pref_key_branding_main, context.getApplicationContext().getResources().getColor(R.color.defaultBrand));
    }

    public static LiveData<Integer> readBrandTextColorLiveData(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_text");
        return new SharedPreferenceIntLiveData(sharedPreferences, pref_key_branding_text, Color.WHITE);
    }

    @ColorInt
    public static int readBrandMainColor(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_main");
        return sharedPreferences.getInt(pref_key_branding_main, context.getApplicationContext().getResources().getColor(R.color.defaultBrand));
    }

    @ColorInt
    public static int readBrandTextColor(@NonNull Context context) {
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Log.v(TAG, "--- Read: shared_preference_theme_text");
        return sharedPreferences.getInt(pref_key_branding_text, Color.WHITE);
    }

    public static void saveBrandColors(@NonNull Context context, @ColorInt int mainColor, @ColorInt int textColor) {
        final int previousMainColor = readBrandMainColor(context);
        final int previousTextColor = readBrandTextColor(context);
        final var editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Log.v(TAG, "--- Write: shared_preference_theme_main" + " | " + mainColor);
        Log.v(TAG, "--- Write: shared_preference_theme_text" + " | " + textColor);
        editor.putInt(pref_key_branding_main, mainColor);
        editor.putInt(pref_key_branding_text, textColor);
        editor.apply();
        if (context instanceof BrandedActivity) {
            if (mainColor != previousMainColor || textColor != previousTextColor) {
                final var activity = (BrandedActivity) context;
                activity.runOnUiThread(() -> ActivityCompat.recreate(activity));
            }
        }
    }
}
