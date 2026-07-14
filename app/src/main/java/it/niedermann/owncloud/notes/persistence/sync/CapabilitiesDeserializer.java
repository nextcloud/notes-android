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
    private static final String CAPABILITIES_USER_STATUS = "user_status";
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
    private static final String CAPABILITIES_SUPPORTS_BUSY = "supports_busy";

    @Override
    public Capabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final var response = new Capabilities();
        final var data = json.getAsJsonObject();

        deserializeVersion(data, response);

        if (!data.has(CAPABILITIES)) {
            return response;
        }

        final var capabilities = data.getAsJsonObject(CAPABILITIES);

        deserializeFilesSharing(capabilities, response);
        deserializeNotes(capabilities, response);
        deserializeTheming(capabilities, response);
        deserializeDirectEditing(capabilities, response);
        deserializeUserStatus(capabilities, response);

        return response;
    }

    private void deserializeVersion(final JsonObject data, final Capabilities response) {
        if (!data.has(VERSION)) {
            return;
        }

        final var version = data.getAsJsonObject(VERSION);

        if (version.has("major")) {
            response.setNextcloudMajorVersion(String.valueOf(version.get("major")));
        }
        if (version.has("minor")) {
            response.setNextcloudMinorVersion(String.valueOf(version.get("minor")));
        }
        if (version.has("micro")) {
            response.setNextcloudMicroVersion(String.valueOf(version.get("micro")));
        }
    }

    private void deserializeFilesSharing(final JsonObject capabilities, final Capabilities response) {
        if (!capabilities.has(CAPABILITIES_FILES_SHARING)) {
            return;
        }

        final var filesSharing = capabilities.getAsJsonObject(CAPABILITIES_FILES_SHARING);

        if (filesSharing.has("federation")) {
            final var federation = filesSharing.getAsJsonObject("federation");
            if (federation.has("outgoing")) {
                response.setFederationShare(federation.get("outgoing").getAsBoolean());
            }
        }

        if (filesSharing.has("api_enabled")) {
            final var shareApiEnabled = filesSharing.get("api_enabled");
            if (!shareApiEnabled.getAsBoolean()) {
                return;
            }
        }

        if (filesSharing.has("public")) {
            final var publicObject = filesSharing.getAsJsonObject("public");
            if (publicObject.has("password")) {
                final var password = publicObject.getAsJsonObject("password");
                if (password.has("enforced")) {
                    response.setPublicPasswordEnforced(password.getAsJsonPrimitive("enforced").getAsBoolean());
                }
                if (password.has("askForOptionalPassword")) {
                    response.setAskForOptionalPassword(password.getAsJsonPrimitive("askForOptionalPassword").getAsBoolean());
                }
            }
        }

        if (filesSharing.has("resharing")) {
            response.setReSharingAllowed(filesSharing.getAsJsonPrimitive("resharing").getAsBoolean());
        }

        if (filesSharing.has("default_permissions")) {
            response.setDefaultPermission(filesSharing.getAsJsonPrimitive("default_permissions").getAsInt());
        }
    }

    private void deserializeNotes(final JsonObject capabilities, final Capabilities response) {
        if (!capabilities.has(CAPABILITIES_NOTES)) {
            return;
        }

        final var notes = capabilities.getAsJsonObject(CAPABILITIES_NOTES);
        if (notes.has(CAPABILITIES_NOTES_API_VERSION)) {
            response.setApiVersion(notes.get(CAPABILITIES_NOTES_API_VERSION).toString());
        }
    }

    private void deserializeTheming(final JsonObject capabilities, final Capabilities response) {
        if (!capabilities.has(CAPABILITIES_THEMING)) {
            return;
        }

        final var theming = capabilities.getAsJsonObject(CAPABILITIES_THEMING);
        if (theming.has(CAPABILITIES_THEMING_COLOR)) {
            try {
                response.setColor(Color.parseColor(ColorUtil.formatColorToParsableHexString(
                        theming.get(CAPABILITIES_THEMING_COLOR).getAsString()
                )));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (theming.has(CAPABILITIES_THEMING_COLOR_TEXT)) {
            try {
                response.setTextColor(Color.parseColor(ColorUtil.formatColorToParsableHexString(
                        theming.get(CAPABILITIES_THEMING_COLOR_TEXT).getAsString()
                )));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deserializeUserStatus(final JsonObject capabilities, final Capabilities response) {
        if (!capabilities.has(CAPABILITIES_USER_STATUS)) {
            return;
        }

        final var userStatus = capabilities.getAsJsonObject(CAPABILITIES_USER_STATUS);
        if (userStatus.has(CAPABILITIES_SUPPORTS_BUSY)) {
            final var userStatusSupportsBusy = userStatus.getAsJsonPrimitive(CAPABILITIES_SUPPORTS_BUSY);
            if (userStatusSupportsBusy != null) {
                response.setUserStatusSupportsBusy(userStatusSupportsBusy.getAsBoolean());
            }
        }
    }

    private void deserializeDirectEditing(final JsonObject capabilities, final Capabilities response) {
        if (!capabilities.has(CAPABILITIES_FILES)) {
            response.setDirectEditingAvailable(false);
            return;
        }

        final var files = capabilities.getAsJsonObject(CAPABILITIES_FILES);
        if (files.has(CAPABILITIES_FILES_DIRECT_EDITING)) {
            final var directEditing = files.getAsJsonObject(CAPABILITIES_FILES_DIRECT_EDITING);
            if (directEditing.has(CAPABILITIES_FILES_DIRECT_EDITING_SUPPORTS_FILE_ID)) {
                response.setDirectEditingAvailable(
                        directEditing.get(CAPABILITIES_FILES_DIRECT_EDITING_SUPPORTS_FILE_ID).getAsBoolean()
                );
            }
        }
    }
}
