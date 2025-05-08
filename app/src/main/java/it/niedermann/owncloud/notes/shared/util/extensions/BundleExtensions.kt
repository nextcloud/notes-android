/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2023 Nextcloud GmbH
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.shared.util.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

@Suppress("TopLevelPropertyNaming")

fun <T : Serializable?> Bundle?.getSerializableArgument(key: String, type: Class<T>): T? {
    if (this == null) {
        return null
    }

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializable(key, type)
        } else {
            @Suppress("UNCHECKED_CAST", "DEPRECATION")
            if (type.isInstance(this.getSerializable(key))) {
                this.getSerializable(key) as T
            } else {
                null
            }
        }
    } catch (e: ClassCastException) {
        null
    }
}

fun <T : Parcelable?> Bundle?.getParcelableArgument(key: String, type: Class<T>): T? {
    if (this == null) {
        return null
    }

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getParcelable(key, type)
        } else {
            @Suppress("DEPRECATION")
            this.getParcelable(key)
        }
    } catch (e: ClassCastException) {
        e.printStackTrace()
        null
    }
}
