package it.niedermann.owncloud.notes.activities.repository

import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.sync.VersionsAPI

class VersionsRepository (
    private val ssoAccount: SingleSignOnAccount
) {
    private val api by lazy { ApiProvider.getInstance().getVersionsAPI(ssoAccount) }

    fun fetchVersions(noteId: Long): VersionsResult {
        return api.fetchVersions(noteId)
    }

}