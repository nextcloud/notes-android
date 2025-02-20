package it.niedermann.owncloud.notes.branding

import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

abstract class BrandedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Branded {

    fun bindBranding() {
        @ColorInt val color = BrandingUtil.readBrandMainColor(itemView.context)
        applyBrand(color)
    }
}
