package it.niedermann.owncloud.notes.shared.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

object ExtendedFabUtil {
    @JvmStatic
    public fun setExtendedFabVisibility(
        extendedFab: ExtendedFloatingActionButton,
        visibility: Boolean,
    ) {
        if (visibility) {
            extendedFab.show()
        } else {
            if (extendedFab.isExtended) {
                extendedFab.hide()
            } else {
                if (extendedFab.animation == null) {
                    val animation = AnimationUtils.loadAnimation(
                        extendedFab.context,
                        R.anim.abc_shrink_fade_out_from_bottom,
                    )
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            extendedFab.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    extendedFab.startAnimation(animation)
                }
            }
        }
    }
}
