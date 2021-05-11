package it.niedermann.owncloud.notes.shared.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a>
 *
 * @param <T> defines the payload of this {@link OcsResponse}.
 */
public class OcsResponse<T> implements Serializable {

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