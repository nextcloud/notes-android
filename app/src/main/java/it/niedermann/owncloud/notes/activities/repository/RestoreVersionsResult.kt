package it.niedermann.owncloud.notes.activities.repository

import com.owncloud.android.lib.resources.files.model.FileVersion

enum class RestoreVersionsResult{
    Success, Failed, FailedUnsupported
}