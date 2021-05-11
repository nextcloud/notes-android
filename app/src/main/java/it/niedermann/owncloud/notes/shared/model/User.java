package it.niedermann.owncloud.notes.shared.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @Expose
    @SerializedName("id")
    public String userId;
    @Expose
    @SerializedName("displayname")
    public String displayName;
}