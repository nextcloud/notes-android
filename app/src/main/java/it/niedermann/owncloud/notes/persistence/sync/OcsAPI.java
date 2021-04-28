package it.niedermann.owncloud.notes.persistence.sync;


import com.nextcloud.android.sso.api.ParsedResponse;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @link <a href="https://deck.readthedocs.io/en/latest/API/">Deck REST API</a>
 */
public interface OcsAPI {

    @GET("capabilities?format=json")
    Observable<ParsedResponse<Capabilities>> getCapabilities(@Query("If-None-Match") String eTag);
}
