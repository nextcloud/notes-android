package it.niedermann.owncloud.notes.shared.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Equivalent of an <code><a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html?highlight=ocs#user-metadata">OcsUser</a></code>
 */
public class OcsUser {
    @Expose
    @SerializedName("id")
    public String userId;
    @Expose
    @SerializedName("displayname")
    public String displayName;
}