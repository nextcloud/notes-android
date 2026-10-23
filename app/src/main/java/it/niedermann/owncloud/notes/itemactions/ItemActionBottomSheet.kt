package it.niedermann.owncloud.notes.itemactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.Branded
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActionsBottomSheetItemBinding
import it.niedermann.owncloud.notes.databinding.ItemActionBottomSheetBinding
import it.niedermann.owncloud.notes.itemdetails.ItemDetailsActivity
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note


class ItemActionBottomSheet(
    val note: Note,
    val account: Account
) : BrandedBottomSheetDialogFragment(R.layout.item_action_bottom_sheet) {

    lateinit var binding: ItemActionBottomSheetBinding
    private var actionViews : List<ActionView>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemActionBottomSheetBinding.inflate(layoutInflater, container, false)

        setTitle()
        addActions(
            listOf(
                NoteAction.SEE_DETAILS,
                NoteAction.SEND_SHARE
            )
        )

        return binding.root
    }

    fun setTitle() {
        binding.title.text = note.title
    }

    fun addActions(actions: List<NoteAction>) {
        actionViews = actions.map { action -> inflateActionView(action) }
        actionViews?.forEach { binding.fileActionsList.addView(it.binding.root) }
    }

    private fun inflateActionView(action: NoteAction): ActionView {
        val itemBinding = ActionsBottomSheetItemBinding.inflate(layoutInflater, binding.fileActionsList, false)
            .apply {
                text.setText(action.title)
                icon.setImageResource(action.icon)
                root.setOnClickListener { onActionSelected(action) }
            }
        return ActionView(itemBinding)
    }

    fun onActionSelected(action: NoteAction) {
        when (action) {
            NoteAction.SEE_DETAILS -> openInfo(ItemDetailsActivity.PAGE.ACTIVITIES)
            NoteAction.SEND_SHARE -> openInfo(ItemDetailsActivity.PAGE.SHARE)
        }
        dismiss()
    }

    override fun applyBrand(color: Int) {
        actionViews?.forEach { it.applyBrand(color) }
    }

    private fun openInfo(page: ItemDetailsActivity.PAGE) {
        val context = context ?: return
        val intent = ItemDetailsActivity.getActivityStartIntent(context, note, account, page)
        startActivity(intent);
    }

    private data class ActionView(
        val binding: ActionsBottomSheetItemBinding
    ) : Branded {

        override fun applyBrand(color: Int) {
            binding.apply {
                val brandingUtil = BrandingUtil.of(color, root.context)
                brandingUtil.platform.colorImageView(icon, ColorRole.PRIMARY)
            }
        }

    }


    companion object {
        @JvmField
        val TAG: String = ItemActionBottomSheet::class.simpleName ?: "ItemActionBottomSheet"
    }

}