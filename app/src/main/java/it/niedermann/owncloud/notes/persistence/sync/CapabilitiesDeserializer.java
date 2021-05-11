package it.niedermann.owncloud.notes.persistence.sync;

import android.graphics.Color;

import androidx.annotation.ColorInt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

/**
 * Deserialization of <code><a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html?highlight=ocs#theming-capabilities">OcsCapabilities</a></code> to {@link Capabilities} is more complex than just mapping the JSON values to the Pojo properties.
 *
 * <ul>
 * <li>The supported API versions of the Notes app are checked and <code>null</code>ed in case they are not present to maintain backward compatibility</li>
 * <li>The color hex codes of the theming app are sanitized and mapped to {@link ColorInt}s</li>
 * </ul>
 */
public class CapabilitiesDeserializer implements JsonDeserializer<Capabilities> {

    private static final String CAPABILITIES = "capabilities";
    private static final String CAPABILITIES_NOTES = "notes";
    private static final String CAPABILITIES_NOTES_API_VERSION = "api_version";
    private static final String CAPABILITIES_THEMING = "theming";
    private static final String CAPABILITIES_THEMING_COLOR = "color";
    private static final String CAPABILITIES_THEMING_COLOR_TEXT = "color-text";

    @Override
    public Capabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final Capabilities response = new Capabilities();
        final JsonObject data = json.getAsJsonObject();
        if (data.has(CAPABILITIES)) {
            final JsonObject capabilities = data.getAsJsonObject(CAPABILITIES);
            if (capabilities.has(CAPABILITIES_NOTES)) {
                final JsonObject notes = capabilities.getAsJsonObject(CAPABILITIES_NOTES);
                if (notes.has(CAPABILITIES_NOTES_API_VERSION)) {
                    final JsonElement apiVersion = notes.get(CAPABILITIES_NOTES_API_VERSION);
                    response.setApiVersion(apiVersion.isJsonArray() ? apiVersion.toString() : null);
                }
            }
            if (capabilities.has(CAPABILITIES_THEMING)) {
                final JsonObject theming = capabilities.getAsJsonObject(CAPABILITIES_THEMING);
                if (theming.has(CAPABILITIES_THEMING_COLOR)) {
                    try {
                        response.setColor(Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(theming.get(CAPABILITIES_THEMING_COLOR).getAsString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (theming.has(CAPABILITIES_THEMING_COLOR_TEXT)) {
                    try {
                        response.setTextColor(Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(theming.get(CAPABILITIES_THEMING_COLOR_TEXT).getAsString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return response;
    }
}
