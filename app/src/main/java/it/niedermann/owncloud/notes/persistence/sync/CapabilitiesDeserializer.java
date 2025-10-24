/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
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
    private static final String CAPABILITIES_FILES = "files";
    private static final String CAPABILITIES_FILES_DIRECT_EDITING = "directEditing";
    private static final String CAPABILITIES_FILES_DIRECT_EDITING_SUPPORTS_FILE_ID = "supportsFileId";
    private static final String CAPABILITIES_FILES_SHARING = "files_sharing";
    private static final String VERSION = "version";

    @Override
    public Capabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final var response = new Capabilities();
        final var data = json.getAsJsonObject();
        if (data.has(VERSION)) {
            final var version = data.getAsJsonObject(VERSION);
            final var nextcloudMajorVersion = version.get("major");
            response.setNextcloudMajorVersion(String.valueOf(nextcloudMajorVersion));

            final var nextcloudMinorVersion = version.get("minor");
            response.setNextcloudMinorVersion(String.valueOf(nextcloudMinorVersion));


            final var nextcloudMicroVersion = version.get("micro");
            response.setNextcloudMicroVersion(String.valueOf(nextcloudMicroVersion));
        }

        if (data.has(CAPABILITIES)) {
            final var capabilities = data.getAsJsonObject(CAPABILITIES);

            if (capabilities.has(CAPABILITIES_FILES_SHARING)) {
                final var filesSharing = capabilities.getAsJsonObject(CAPABILITIES_FILES_SHARING);
                final var federation = filesSharing.getAsJsonObject("federation");
                final var outgoing = federation.get("outgoing");

                response.setFederationShare(outgoing.getAsBoolean());

                final var publicObject = filesSharing.getAsJsonObject("public");
                if (publicObject.has("password")) {
                    final var password = publicObject.getAsJsonObject("password");
                    final var enforced = password.getAsJsonPrimitive("enforced");
                    final var askForOptionalPassword = password.getAsJsonPrimitive("askForOptionalPassword");

                    response.setPublicPasswordEnforced(enforced.getAsBoolean());
                    response.setAskForOptionalPassword(askForOptionalPassword.getAsBoolean());
                }

                final var isReSharingAllowed = filesSharing.getAsJsonPrimitive("resharing");
                final var defaultPermission = filesSharing.getAsJsonPrimitive("default_permissions");
                response.setDefaultPermission(defaultPermission.getAsInt());
                response.setReSharingAllowed(isReSharingAllowed.getAsBoolean());
            }

            if (capabilities.has(CAPABILITIES_NOTES)) {
                final var notes = capabilities.getAsJsonObject(CAPABILITIES_NOTES);
                if (notes.has(CAPABILITIES_NOTES_API_VERSION)) {
                    response.setApiVersion(notes.get(CAPABILITIES_NOTES_API_VERSION).toString());
                }
            }
            if (capabilities.has(CAPABILITIES_THEMING)) {
                final var theming = capabilities.getAsJsonObject(CAPABILITIES_THEMING);
                if (theming.has(CAPABILITIES_THEMING_COLOR)) {
                    try {
                        response.setColor(Color.parseColor(ColorUtil.formatColorToParsableHexString(theming.get(CAPABILITIES_THEMING_COLOR).getAsString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (theming.has(CAPABILITIES_THEMING_COLOR_TEXT)) {
                    try {
                        response.setTextColor(Color.parseColor(ColorUtil.formatColorToParsableHexString(theming.get(CAPABILITIES_THEMING_COLOR_TEXT).getAsString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            response.setDirectEditingAvailable(hasDirectEditingCapability(capabilities));
        }
        return response;
    }

    private boolean hasDirectEditingCapability(final JsonObject capabilities) {
        if (capabilities.has(CAPABILITIES_FILES)) {
            final var files = capabilities.getAsJsonObject(CAPABILITIES_FILES);
            if (files.has(CAPABILITIES_FILES_DIRECT_EDITING)) {
                final var directEditing = files.getAsJsonObject(CAPABILITIES_FILES_DIRECT_EDITING);
                if (directEditing.has(CAPABILITIES_FILES_DIRECT_EDITING_SUPPORTS_FILE_ID)) {
                    return directEditing.get(CAPABILITIES_FILES_DIRECT_EDITING_SUPPORTS_FILE_ID).getAsBoolean();
                }
            }
        }
        return false;
    }
}
