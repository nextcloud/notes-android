package it.niedermann.owncloud.notes.itemactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment
import it.niedermann.owncloud.notes.databinding.ItemActionBottomSheetBinding
import it.niedermann.owncloud.notes.itemdetails.ItemDetailsActivity
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note

class ItemActionBottomSheet(
    val note: Note,
    val account: Account
) : BrandedBottomSheetDialogFragment(R.layout.item_action_bottom_sheet) {

    lateinit var binding: ItemActionBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemActionBottomSheetBinding.inflate(layoutInflater, container, false)

        setTitle()
        addActions()

        return binding.root
    }

    fun setTitle() {
        binding.title.text = note.title
    }

    fun addActions() {

    }

    override fun applyBrand(color: Int) {
        // TODO
    }

    private fun openInfo(page: ItemDetailsActivity.PAGE) {
        val context = context ?: return
        val intent = ItemDetailsActivity.getActivityStartIntent(context, note, account, page)
        startActivity(intent);
    }

    companion object {
        @JvmField
        val TAG: String = ItemActionBottomSheet::class.simpleName ?: "ItemActionBottomSheet"
    }

}