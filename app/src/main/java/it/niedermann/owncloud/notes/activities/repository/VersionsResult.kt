package it.niedermann.owncloud.notes.activities.repository

import com.owncloud.android.lib.resources.files.model.FileVersion

sealed class VersionsResult {

    data class Success(val versions: List<FileVersion>) : VersionsResult()
    data object Failed : VersionsResult()

}