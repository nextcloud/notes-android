package it.niedermann.owncloud.notes.main.items

import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment
import it.niedermann.owncloud.notes.itemdetails.ItemDetailsActivity
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note

class ItemActionBottomSheet(
    val note: Note,
    val account: Account
) : BrandedBottomSheetDialogFragment(R.layout.item_action_bottom_sheet) {

    override fun applyBrand(color: Int) {
        // TODO
    }

    private fun openInfo(page: ItemDetailsActivity.PAGE) {
        val context = context ?: return
        val intent = ItemDetailsActivity.getActivityStartIntent(context, note, account, page);
        startActivity(intent);
    }

    companion object {
        @JvmField
        val TAG: String = ItemActionBottomSheet::class.simpleName ?: "ItemActionBottomSheet"
    }

}