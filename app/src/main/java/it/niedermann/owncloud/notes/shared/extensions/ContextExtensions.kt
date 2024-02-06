package it.niedermann.owncloud.notes.shared.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner

fun Context.lifecycleOwner(): LifecycleOwner? {
    var curContext: Context? = this
    while (curContext != null && curContext !is LifecycleOwner) {
        curContext = (curContext as? ContextWrapper)?.baseContext
    }
    return curContext as? LifecycleOwner
}
