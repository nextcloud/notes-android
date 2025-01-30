package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CreateShareResponse(
    @Expose
    @JvmField
    val id: String,

    @Expose
    @JvmField
    @SerializedName("share_type")
    val shareType: Long,

    @Expose
    @JvmField
    @SerializedName("uid_owner")
    val uidOwner: String,

    @Expose
    @JvmField
    @SerializedName("displayname_owner")
    val displaynameOwner: String,

    @Expose
    @JvmField
    val permissions: Long,

    @Expose
    @JvmField
    @SerializedName("can_edit")
    val canEdit: Boolean,

    @Expose
    @JvmField
    @SerializedName("can_delete")
    val canDelete: Boolean,

    @Expose
    @JvmField
    val stime: Long,

    @Expose
    @JvmField
    @SerializedName("uid_file_owner")
    val uidFileOwner: String,

    @Expose
    @JvmField
    val note: String,

    @Expose
    @JvmField
    val label: String,

    @Expose
    @JvmField
    @SerializedName("displayname_file_owner")
    val displaynameFileOwner: String,

    @Expose
    @JvmField
    val path: String,

    @Expose
    @JvmField
    @SerializedName("item_type")
    val itemType: String,

    @Expose
    @JvmField
    @SerializedName("item_permissions")
    val itemPermissions: Long,

    @Expose
    @JvmField
    @SerializedName("is-mount-root")
    val isMountRoot: Boolean,

    @Expose
    @JvmField
    @SerializedName("mount-type")
    val mountType: String,

    @Expose
    @JvmField
    val mimetype: String,

    @Expose
    @JvmField
    @SerializedName("has_preview")
    val hasPreview: Boolean,

    @Expose
    @JvmField
    @SerializedName("storage_id")
    val storageId: String,

    @Expose
    @JvmField
    val storage: Long,

    @Expose
    @JvmField
    @SerializedName("item_source")
    val itemSource: Long,

    @Expose
    @JvmField
    @SerializedName("file_source")
    val fileSource: Long,

    @Expose
    @JvmField
    @SerializedName("file_parent")
    val fileParent: Long,

    @Expose
    @JvmField
    @SerializedName("file_target")
    val fileTarget: String,

    @Expose
    @JvmField
    @SerializedName("item_size")
    val itemSize: Long,

    @Expose
    @JvmField
    @SerializedName("item_mtime")
    val itemMtime: Long,

    @Expose
    @JvmField
    @SerializedName("share_with")
    val shareWith: String,

    @Expose
    @JvmField
    @SerializedName("share_with_displayname")
    val shareWithDisplayname: String,

    @Expose
    @JvmField
    @SerializedName("mail_send")
    val mailSend: Long,

    @Expose
    @JvmField
    @SerializedName("hide_download")
    val hideDownload: Long,
)
