package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

/**
 * Editor for direct editing data model
 */
data class DirectEditingEditor(
    @Expose
    val id: String,
    @Expose
    val name: String,
    @Expose
    val mimetypes: ArrayList<String>,
    @Expose
    val optionalMimetypes: ArrayList<String>,
    @Expose
    val secure: Boolean,
)
