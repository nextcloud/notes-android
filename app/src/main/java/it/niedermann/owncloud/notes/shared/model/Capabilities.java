/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class Capabilities {

    /**
     * 30(Major) .0(Minor). 5(Micro)
     */
    private String nextcloudMajorVersion = null;
    private String nextcloudMinorVersion = null;
    private String nextcloudMicroVersion = null;

    private boolean federationShare = false;

    private String apiVersion = null;
    @ColorInt
    private int color = -16743735; // #0082C9
    @ColorInt
    private int textColor = Color.WHITE;
    @Nullable
    private String eTag;

    private boolean directEditingAvailable;

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    @Nullable
    public String getETag() {
        return eTag;
    }

    @Nullable
    public String getNextcloudMajorVersion() {
        return nextcloudMajorVersion;
    }

    @Nullable
    public String getNextcloudMinorVersion() {
        return nextcloudMinorVersion;
    }

    @Nullable
    public String getNextcloudMicroVersion() {
        return nextcloudMicroVersion;
    }

    public void setNextcloudMajorVersion(@Nullable String nextcloudMajorVersion) {
        this.nextcloudMajorVersion = nextcloudMajorVersion;
    }

    public void setNextcloudMinorVersion(@Nullable String nextcloudMinorVersion) {
        this.nextcloudMinorVersion = nextcloudMinorVersion;
    }

    public void setNextcloudMicroVersion(@Nullable String nextcloudMicroVersion) {
        this.nextcloudMicroVersion = nextcloudMicroVersion;
    }

    public boolean getFederationShare() {
        return federationShare;
    }

    public void setFederationShare(boolean value) {
        this.federationShare = value;
    }

    public void setETag(@Nullable String eTag) {
        this.eTag = eTag;
    }

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }


    public boolean isDirectEditingAvailable() {
        return directEditingAvailable;
    }

    public void setDirectEditingAvailable(boolean directEditingAvailable) {
        this.directEditingAvailable = directEditingAvailable;
    }

    @Override
    public String toString() {
        return "Capabilities{" +
                "apiVersion='" + apiVersion + '\'' +
                ", nextcloudMajorVersion='" + nextcloudMajorVersion + '\'' +
                ", nextcloudMinorVersion='" + nextcloudMinorVersion + '\'' +
                ", nextcloudMicroVersion='" + nextcloudMicroVersion + '\'' +
                ", federationShare=" + federationShare +
                ", color=" + color +
                ", textColor=" + textColor +
                ", eTag='" + eTag + '\'' +
                ", hasDirectEditing=" + directEditingAvailable +
                '}';
    }
}
