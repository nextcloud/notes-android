package it.niedermann.owncloud.notes.share.model

data class ShareesData(
    val exact: ExactMatches,
    val users: List<ShareeItem>,
    val groups: List<ShareeItem>,
    val remotes: List<ShareeItem>,
    val remote_groups: List<ShareeItem>,
    val emails: List<ShareeItem>,
    val circles: List<ShareeItem>,
    val rooms: List<ShareeItem>,
    val lookup: List<ShareeItem>,
    val lookupEnabled: Boolean
)

data class ExactMatches(
    val users: List<ShareeItem>,
    val groups: List<ShareeItem>,
    val remotes: List<ShareeItem>,
    val remote_groups: List<ShareeItem>,
    val emails: List<ShareeItem>,
    val circles: List<ShareeItem>,
    val rooms: List<ShareeItem>
)

data class ShareeItem(
    val label: String,
    val value: ShareeValue
)

data class ShareeValue(
    val shareType: Double,
    val shareWith: String
)
