package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.R;

import static it.niedermann.owncloud.notes.branding.BrandingUtil.tintMenuIcon;

public abstract class BrandedFragment extends Fragment implements Branded {

    @ColorInt
    protected int colorAccent;
    @ColorInt
    protected int colorPrimary;

    @Override
    public void onStart() {
        super.onStart();

        final TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;
        requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        colorPrimary = typedValue.data;

        @Nullable Context context = getContext();
        if (context != null && BrandingUtil.isBrandingEnabled(context)) {
            @ColorInt final int mainColor = BrandingUtil.readBrandMainColor(context);
            @ColorInt final int textColor = BrandingUtil.readBrandTextColor(context);
            applyBrand(mainColor, textColor);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        for (int i = 0; i < menu.size(); i++) {
            tintMenuIcon(menu.getItem(i), colorAccent);
        }
    }
}
