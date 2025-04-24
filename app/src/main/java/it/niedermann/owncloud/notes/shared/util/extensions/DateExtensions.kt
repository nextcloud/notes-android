package it.niedermann.owncloud.notes.shared.util.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toExpirationDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)
}
