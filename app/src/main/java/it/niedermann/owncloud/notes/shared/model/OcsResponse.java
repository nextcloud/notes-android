/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import com.google.gson.annotations.Expose;

/**
 * <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a>
 *
 * @param <T> defines the payload of this {@link OcsResponse}.
 */
public class OcsResponse<T> {

    @Expose
    public OcsWrapper<T> ocs;

    public static class OcsWrapper<T> {
        @Expose
        public OcsMeta meta;
        @Expose
        public T data;
    }

    public static class OcsMeta {
        @Expose
        public String status;
        @Expose
        public int statuscode;
        @Expose
        public String message;
    }
}