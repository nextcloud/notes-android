package it.niedermann.owncloud.notes.persistence.sync;

import android.graphics.Color;
import android.util.Log;

import com.bumptech.glide.load.HttpException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import java.lang.reflect.Type;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class CapabilitiesDeserializer implements JsonDeserializer<Capabilities> {

    private static final String TAG = CapabilitiesDeserializer.class.getSimpleName();

    private static final String JSON_OCS = "ocs";
    private static final String JSON_OCS_META = "meta";
    private static final String JSON_OCS_META_STATUSCODE = "statuscode";
    private static final String JSON_OCS_DATA = "data";
    private static final String JSON_OCS_DATA_CAPABILITIES = "capabilities";
    private static final String JSON_OCS_DATA_CAPABILITIES_NOTES = "notes";
    private static final String JSON_OCS_DATA_CAPABILITIES_NOTES_API_VERSION = "api_version";
    private static final String JSON_OCS_DATA_CAPABILITIES_THEMING = "theming";
    private static final String JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR = "color";
    private static final String JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR_TEXT = "color-text";

    @Override
    public Capabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final Capabilities response = new Capabilities();
        final JsonObject ocs = json.getAsJsonObject().getAsJsonObject(JSON_OCS);
        if (ocs.has(JSON_OCS_META)) {
            final JsonObject meta = ocs.getAsJsonObject(JSON_OCS_META);
            if (meta.has(JSON_OCS_META_STATUSCODE)) {
                if (meta.get(JSON_OCS_META_STATUSCODE).getAsInt() == HTTP_UNAVAILABLE) {
                    Log.i(TAG, "Capabilities Endpoint: This instance is currently in maintenance mode.");
                    throw new JsonParseException(new NextcloudHttpRequestFailedException(HTTP_UNAVAILABLE, new HttpException(HTTP_UNAVAILABLE)));
                }
            }
        }
        if (ocs.has(JSON_OCS_DATA)) {
            final JsonObject data = ocs.getAsJsonObject(JSON_OCS_DATA);
            if (data.has(JSON_OCS_DATA_CAPABILITIES)) {
                final JsonObject capabilities = data.getAsJsonObject(JSON_OCS_DATA_CAPABILITIES);
                if (capabilities.has(JSON_OCS_DATA_CAPABILITIES_NOTES)) {
                    final JsonObject notes = capabilities.getAsJsonObject(JSON_OCS_DATA_CAPABILITIES_NOTES);
                    if (notes.has(JSON_OCS_DATA_CAPABILITIES_NOTES_API_VERSION)) {
                        final JsonElement apiVersion = notes.get(JSON_OCS_DATA_CAPABILITIES_NOTES_API_VERSION);
                        response.setApiVersion(apiVersion.isJsonArray() ? apiVersion.toString() : null);
                    }
                }
                if (capabilities.has(JSON_OCS_DATA_CAPABILITIES_THEMING)) {
                    final JsonObject theming = capabilities.getAsJsonObject(JSON_OCS_DATA_CAPABILITIES_THEMING);
                    if (theming.has(JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR)) {
                        try {
                            response.setColor(Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(theming.get(JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR).getAsString())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (theming.has(JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR_TEXT)) {
                        try {
                            response.setTextColor(Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(theming.get(JSON_OCS_DATA_CAPABILITIES_THEMING_COLOR_TEXT).getAsString())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return response;
    }
}
