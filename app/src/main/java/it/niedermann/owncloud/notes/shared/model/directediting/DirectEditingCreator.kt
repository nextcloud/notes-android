package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

data class DirectEditingCreator(
    @Expose
    val id: String,
    @Expose
    val editor: String,
    @Expose
    val name: String,
    @Expose
    val extension: String,
    @Expose
    val mimetype: String,
    @Expose
    val templates: Boolean,
)
