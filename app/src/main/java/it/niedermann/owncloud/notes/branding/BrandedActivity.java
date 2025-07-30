/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import static it.niedermann.owncloud.notes.branding.BrandingUtil.readBrandMainColorLiveData;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.R;
import com.nextcloud.android.common.ui.util.extensions.AppCompatActivityExtensionsKt;


public abstract class BrandedActivity extends AppCompatActivity implements Branded {

    @ColorInt
    protected int colorAccent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatActivityExtensionsKt.applyEdgeToEdgeWithSystemBarPadding(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final var typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;

        readBrandMainColorLiveData(this).observe(this, this::applyBrand);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final var utils = BrandingUtil.of(colorAccent, this);

        for (int i = 0; i < menu.size(); i++) {
            utils.platform.colorToolbarMenuIcon(this, menu.getItem(i));
        }

        return super.onCreateOptionsMenu(menu);
    }
}
