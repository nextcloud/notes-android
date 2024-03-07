/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

public interface IResponseCallback<T> {
    void onSuccess(T result);

    void onError(@NonNull Throwable t);
}
