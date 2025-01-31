package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CreateShareResponse(
    @Expose
    val id: String,

    @Expose
    @SerializedName("share_type")
    val shareType: Long,

    @Expose
    @SerializedName("uid_owner")
    val uidOwner: String,

    @Expose
    @SerializedName("displayname_owner")
    val displaynameOwner: String,

    @Expose
    val permissions: Long,

    @Expose
    @SerializedName("can_edit")
    val canEdit: Boolean,

    @Expose
    @SerializedName("can_delete")
    val canDelete: Boolean,

    @Expose
    val stime: Long,

    @Expose
    @SerializedName("uid_file_owner")
    val uidFileOwner: String,

    @Expose
    val note: String,

    @Expose
    val label: String,

    @Expose
    @SerializedName("displayname_file_owner")
    val displaynameFileOwner: String,

    @Expose
    val path: String,

    @Expose
    @SerializedName("item_type")
    val itemType: String,

    @Expose
    @SerializedName("item_permissions")
    val itemPermissions: Long,

    @Expose
    @SerializedName("is-mount-root")
    val isMountRoot: Boolean,

    @Expose
    @SerializedName("mount-type")
    val mountType: String,

    @Expose
    val mimetype: String,

    @Expose
    @SerializedName("has_preview")
    val hasPreview: Boolean,

    @Expose
    @SerializedName("storage_id")
    val storageId: String,

    @Expose
    val storage: Long,

    @Expose
    @SerializedName("item_source")
    val itemSource: Long,

    @Expose
    @SerializedName("file_source")
    val fileSource: Long,

    @Expose
    @SerializedName("file_parent")
    val fileParent: Long,

    @Expose
    @SerializedName("file_target")
    val fileTarget: String,

    @Expose
    @SerializedName("item_size")
    val itemSize: Long,

    @Expose
    @SerializedName("item_mtime")
    val itemMtime: Long,

    @Expose
    @SerializedName("share_with")
    val shareWith: String,

    @Expose
    @SerializedName("share_with_displayname")
    val shareWithDisplayname: String,

    @Expose
    @SerializedName("mail_send")
    val mailSend: Long,

    @Expose
    @SerializedName("hide_download")
    val hideDownload: Long,
)
