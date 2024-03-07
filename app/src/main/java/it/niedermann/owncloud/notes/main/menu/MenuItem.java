/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.menu;

import android.content.Intent;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class MenuItem {

    @NonNull
    private Intent intent;
    @StringRes
    private final int labelResource;
    @DrawableRes
    private final int drawableResource;

    @Nullable
    private Integer resultCode;

    public MenuItem(@NonNull Intent intent, int labelResource, int drawableResource) {
        this.intent = intent;
        this.labelResource = labelResource;
        this.drawableResource = drawableResource;
    }

    public MenuItem(@NonNull Intent intent, int resultCode, int labelResource, int drawableResource) {
        this.intent = intent;
        this.resultCode = resultCode;
        this.labelResource = labelResource;
        this.drawableResource = drawableResource;
    }

    @NonNull
    public Intent getIntent() {
        return intent;
    }

    public void setIntent(@NonNull Intent intent) {
        this.intent = intent;
    }

    public int getLabelResource() {
        return labelResource;
    }

    public int getDrawableResource() {
        return drawableResource;
    }

    @Nullable
    public Integer getResultCode() {
        return resultCode;
    }
}
