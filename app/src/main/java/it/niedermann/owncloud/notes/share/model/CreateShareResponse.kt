package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.SerializedName

data class CreateShareResponse(
    val id: String,
    @SerializedName("share_type")
    val shareType: Long,
    @SerializedName("uid_owner")
    val uidOwner: String,
    @SerializedName("displayname_owner")
    val displaynameOwner: String,
    val permissions: Long,
    @SerializedName("can_edit")
    val canEdit: Boolean,
    @SerializedName("can_delete")
    val canDelete: Boolean,
    val stime: Long,
    @SerializedName("uid_file_owner")
    val uidFileOwner: String,
    val note: String,
    val label: String,
    @SerializedName("displayname_file_owner")
    val displaynameFileOwner: String,
    val path: String,
    @SerializedName("item_type")
    val itemType: String,
    @SerializedName("item_permissions")
    val itemPermissions: Long,
    @SerializedName("is-mount-root")
    val isMountRoot: Boolean,
    @SerializedName("mount-type")
    val mountType: String,
    val mimetype: String,
    @SerializedName("has_preview")
    val hasPreview: Boolean,
    @SerializedName("storage_id")
    val storageId: String,
    val storage: Long,
    @SerializedName("item_source")
    val itemSource: Long,
    @SerializedName("file_source")
    val fileSource: Long,
    @SerializedName("file_parent")
    val fileParent: Long,
    @SerializedName("file_target")
    val fileTarget: String,
    @SerializedName("item_size")
    val itemSize: Long,
    @SerializedName("item_mtime")
    val itemMtime: Long,
    @SerializedName("share_with")
    val shareWith: String,
    @SerializedName("share_with_displayname")
    val shareWithDisplayname: String,
    @SerializedName("mail_send")
    val mailSend: Long,
    @SerializedName("hide_download")
    val hideDownload: Long,
)
