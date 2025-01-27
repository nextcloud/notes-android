package it.niedermann.owncloud.notes.share.model

object UsersAndGroupsSearchConfig {
    var searchOnlyUsers: Boolean = false

    fun reset() {
        searchOnlyUsers = false
    }
}
