package it.niedermann.owncloud.notes.accountswitcher


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.resources.users.Status
import com.owncloud.android.lib.resources.users.StatusType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.accountswitcher.repository.UserStatusRepository
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.SetOnlineStatusBottomSheetBinding
import it.niedermann.owncloud.notes.shared.util.DisplayUtils
import it.niedermann.owncloud.notes.shared.util.FilesSpecificViewThemeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetOnlineStatusBottomSheet :
    BrandedBottomSheetDialogFragment(R.layout.set_online_status_bottom_sheet) {

    companion object {
        private val TAG = SetOnlineStatusBottomSheet::class.simpleName
    }

    private lateinit var binding: SetOnlineStatusBottomSheetBinding
    private var cardViews: Triple<MaterialCardView, TextView, ImageView>? = null
    private var repository: UserStatusRepository? = null

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRepository()
        setupStatusClickListeners()
    }

    private fun setupStatusClickListeners() {
        val statusMap = mapOf(
            binding.onlineStatus to StatusType.ONLINE,
            binding.awayStatus to StatusType.AWAY,
            binding.busyStatus to StatusType.BUSY,
            binding.dndStatus to StatusType.DND,
            binding.invisibleStatus to StatusType.INVISIBLE
        )

        statusMap.forEach { (view, statusType) ->
            view.setOnClickListener { setStatus(statusType) }
        }
    }

    private fun initRepository() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ssoAccount =
                SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext()) ?: return@launch
            repository = UserStatusRepository(requireContext(), ssoAccount)
            val currentStatus =
                repository?.fetchUserStatus() ?: Status(StatusType.OFFLINE, "", "", -1)

            val capabilities = repository?.getCapabilities()

            if (capabilities?.isUserStatusSupportsBusy == true) {
                binding.busyStatus.visibility = View.VISIBLE
            } else {
                binding.busyStatus.visibility = View.GONE
            }

            withContext(Dispatchers.Main) {
                updateCurrentStatusViews(currentStatus)
            }
        }
    }

    private fun updateCurrentStatusViews(it: Status) {
        visualizeStatus(it.status)
    }

    private fun setStatus(statusType: StatusType) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository?.setStatusType(statusType)
            withContext(Dispatchers.Main) {
                if (result == true) {
                    dismiss()
                } else {
                    showErrorSnackbar()
                }
            }
        }
    }

    private fun showErrorSnackbar() {
        DisplayUtils.showSnackMessage(view, R.string.status_set_fail_message)
        clearTopStatus()
    }

    private fun visualizeStatus(statusType: StatusType) {
        clearTopStatus()
        cardViews = when (statusType) {
            StatusType.ONLINE -> Triple(
                binding.onlineStatus,
                binding.onlineHeadline,
                binding.onlineIcon
            )

            StatusType.AWAY -> Triple(binding.awayStatus, binding.awayHeadline, binding.awayIcon)
            StatusType.BUSY -> Triple(binding.busyStatus, binding.busyHeadline, binding.busyIcon)
            StatusType.DND -> Triple(binding.dndStatus, binding.dndHeadline, binding.dndIcon)
            StatusType.INVISIBLE -> Triple(
                binding.invisibleStatus,
                binding.invisibleHeadline,
                binding.invisibleIcon
            )

            else -> {
                Log.d(TAG, "unknown status")
                return
            }
        }
        cardViews?.first?.isChecked = true
    }

    private fun clearTopStatus() {
        context?.let { ctx ->
            binding.run {
                val headlines = listOf(
                    onlineHeadline,
                    awayHeadline,
                    busyHeadline,
                    dndHeadline,
                    invisibleHeadline
                )
                val color = ContextCompat.getColor(ctx, com.nextcloud.android.common.ui.R.color.high_emphasis_text)
                headlines.forEach { it.setTextColor(color) }
                listOf(awayIcon, dndIcon, invisibleIcon).forEach { it.imageTintList = null }
                listOf(onlineStatus, awayStatus, busyStatus, dndStatus, invisibleStatus).forEach { it.isChecked = false }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetOnlineStatusBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun applyBrand(color: Int) {
        BrandingUtil.of(color, requireContext()).run {
            platform.themeDialog(binding.root)

            cardViews?.let {
                platform.colorTextView(it.second, ColorRole.ON_SECONDARY_CONTAINER)
            }
        }

        FilesSpecificViewThemeUtils.run {
            themeStatusCardView(binding.onlineStatus, color)
            themeStatusCardView(binding.awayStatus, color)
            themeStatusCardView(binding.busyStatus, color)
            themeStatusCardView(binding.dndStatus, color)
            themeStatusCardView(binding.invisibleStatus, color)
        }
    }
}
