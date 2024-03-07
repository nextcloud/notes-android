/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import androidx.annotation.ColorInt;
import androidx.annotation.UiThread;

public interface Branded {
    @UiThread
    void applyBrand(@ColorInt int color);
}