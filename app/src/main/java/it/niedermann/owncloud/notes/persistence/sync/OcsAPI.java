/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync;

import com.nextcloud.android.sso.api.ParsedResponse;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;
import it.niedermann.owncloud.notes.shared.model.OcsUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * @link <a href="https://deck.readthedocs.io/en/latest/API/">Deck REST API</a>
 */
public interface OcsAPI {

    @GET("capabilities?format=json")
    Observable<ParsedResponse<OcsResponse<Capabilities>>> getCapabilities(@Header("If-None-Match") String eTag);

    @GET("users/{userId}?format=json")
    Call<OcsResponse<OcsUser>> getUser(@Path("userId") String userId);
}
