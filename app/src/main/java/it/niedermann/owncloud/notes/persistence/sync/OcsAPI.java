/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync;

import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;
import it.niedermann.owncloud.notes.shared.model.OcsUser;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * @link <a href="https://deck.readthedocs.io/en/latest/API/">Deck REST API</a>
 */
public interface OcsAPI {

    @GET("capabilities?format=json")
    Observable<ParsedResponse<OcsResponse<Capabilities>>> getCapabilities(@Header("If-None-Match") String eTag);

    @GET("users/{userId}?format=json")
    Call<OcsResponse<OcsUser>> getUser(@Path("userId") String userId);

    @DELETE("apps/user_status/api/v1/user_status/message?format=json")
    Call<OcsResponse<Void>> clearStatusMessage();

    @PUT("apps/user_status/api/v1/user_status/message/predefined?format=json")
    @Headers("Content-Type: application/json")
    Call<OcsResponse<Void>> setPredefinedStatusMessage(@Body Map<String, String> body);

    @PUT("apps/user_status/api/v1/user_status/message/custom?format=json")
    @Headers("Content-Type: application/json")
    Call<OcsResponse<Void>> setUserDefinedStatusMessage(@Body Map<String, String> body);
}
