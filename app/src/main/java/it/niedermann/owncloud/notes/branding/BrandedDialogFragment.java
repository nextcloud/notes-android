package it.niedermann.owncloud.notes.branding;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public abstract class BrandedDialogFragment extends DialogFragment implements Branded {

    @Override
    public void onStart() {
        super.onStart();

        @Nullable final var context = requireContext();
        @ColorInt final int color = BrandingUtil.readBrandMainColor(context);
        applyBrand(color);
    }
}
