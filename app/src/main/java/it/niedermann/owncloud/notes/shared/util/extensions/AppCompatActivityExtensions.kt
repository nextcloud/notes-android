package it.niedermann.owncloud.notes.shared.util.extensions

import android.graphics.Color
import android.os.Build
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.adjustUIForAPILevel35() {
    val isApiLevel35OrHigher = (Build.VERSION.SDK_INT >= 35)
    if (!isApiLevel35OrHigher) {
        return
    }

    val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(style, style)

    window.addSystemBarPaddings()
}
