package it.niedermann.owncloud.notes.share

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.SharePermissionsBuilder
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedActivity
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityNoteShareDetailBinding
import it.niedermann.owncloud.notes.main.MainActivity
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.share.dialog.ExpirationDatePickerDialogFragment
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper
import it.niedermann.owncloud.notes.share.model.ShareAttributesV1
import it.niedermann.owncloud.notes.share.model.ShareAttributesV2
import it.niedermann.owncloud.notes.share.model.SharePasswordRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.share.repository.ShareRepository
import it.niedermann.owncloud.notes.shared.util.DisplayUtils
import it.niedermann.owncloud.notes.shared.util.clipboard.ClipboardUtil
import it.niedermann.owncloud.notes.shared.util.extensions.getParcelableArgument
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity class to show share permission options, set expiration date, change label, set password, send note
 *
 * This activity handles following:
 * 1. This will be shown while creating new internal and external share. So that user can set every share
 * configuration at one time.
 * 2. This will handle both Advanced Permissions and Send New Email functionality for existing shares to modify them.
 */
@Suppress("TooManyFunctions")
class NoteShareDetailActivity : BrandedActivity(),
    ExpirationDatePickerDialogFragment.OnExpiryDateListener {

    companion object {
        const val TAG = "NoteShareDetailActivity"
        const val ARG_NOTE = "arg_sharing_note"
        const val ARG_SHAREE_NAME = "arg_sharee_name"
        const val ARG_SHARE_TYPE = "arg_share_type"
        const val ARG_OCSHARE = "arg_ocshare"
        const val ARG_SCREEN_TYPE = "arg_screen_type"
        const val ARG_RESHARE_SHOWN = "arg_reshare_shown"
        const val ARG_EXP_DATE_SHOWN = "arg_exp_date_shown"
        private const val ARG_SECURE_SHARE = "secure_share"

        // types of screens to be displayed
        const val SCREEN_TYPE_PERMISSION = 1 // permissions screen
        const val SCREEN_TYPE_NOTE = 2 // note screen
    }

    private lateinit var binding: ActivityNoteShareDetailBinding
    private var note: Note? = null // note to be share
    private var shareeName: String? = null
    private lateinit var shareType: ShareType
    private var shareProcessStep = SCREEN_TYPE_PERMISSION // default screen type
    private var permission = OCShare.NO_PERMISSION // no permission
    private var chosenExpDateInMills: Long = -1 // for no expiry date

    private var share: OCShare? = null
    private var isReShareShown: Boolean = true // show or hide reShare option
    private var isExpDateShown: Boolean = true // show or hide expiry date option
    private var isSecureShare: Boolean = false

    private var expirationDatePickerFragment: ExpirationDatePickerDialogFragment? = null
    private lateinit var repository: ShareRepository
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoteShareDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val arguments = intent.extras

        arguments?.let {
            note = it.getSerializableArgument(ARG_NOTE, Note::class.java)
            shareeName = it.getString(ARG_SHAREE_NAME)
            share = it.getParcelableArgument(ARG_OCSHARE, OCShare::class.java)

            if (it.containsKey(ARG_SHARE_TYPE)) {
                shareType = ShareType.fromValue(it.getInt(ARG_SHARE_TYPE))
            } else if (share != null) {
                shareType = share!!.shareType!!
            }

            shareProcessStep = it.getInt(ARG_SCREEN_TYPE, SCREEN_TYPE_PERMISSION)
            isReShareShown = it.getBoolean(ARG_RESHARE_SHOWN, true)
            isExpDateShown = it.getBoolean(ARG_EXP_DATE_SHOWN, true)
            isSecureShare = it.getBoolean(ARG_SECURE_SHARE, false)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val ssoAcc =
                SingleAccountHelper.getCurrentSingleSignOnAccount(this@NoteShareDetailActivity)
            repository = ShareRepository(this@NoteShareDetailActivity, ssoAcc)

            withContext(Dispatchers.Main) {
                if (shareProcessStep == SCREEN_TYPE_PERMISSION) {
                    showShareProcessFirst()
                } else {
                    showShareProcessSecond()
                }
                implementClickEvents()
            }
        }
    }


    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, this)

        binding.run {
            util.platform.run {
                themeRadioButton(shareProcessPermissionReadOnly)
                themeRadioButton(shareProcessPermissionUploadEditing)
                themeRadioButton(shareProcessPermissionFileDrop)

                colorTextView(shareProcessEditShareLink)
                colorTextView(shareProcessAdvancePermissionTitle)

                themeCheckbox(shareProcessAllowResharingCheckbox)
            }

            util.androidx.run {
                colorSwitchCompat(shareProcessSetPasswordSwitch)
                colorSwitchCompat(shareProcessSetExpDateSwitch)
                colorSwitchCompat(shareProcessHideDownloadCheckbox)
                colorSwitchCompat(shareProcessChangeNameSwitch)
            }

            util.material.run {
                colorTextInputLayout(shareProcessEnterPasswordContainer)
                colorTextInputLayout(shareProcessChangeNameContainer)
                colorTextInputLayout(noteContainer)

                colorMaterialButtonPrimaryFilled(shareProcessBtnNext)
                colorMaterialButtonPrimaryOutlined(shareProcessBtnCancel)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Force recreation of dialog activity when screen rotates
        // This is needed because the calendar layout should be different in portrait and landscape,
        // but as FDA persists through config changes, the dialog is not recreated automatically
        val datePicker = expirationDatePickerFragment
        if (datePicker?.dialog?.isShowing == true) {
            val currentSelectionMillis = datePicker.currentSelectionMillis
            datePicker.dismiss()
            showExpirationDateDialog(currentSelectionMillis)
        }
    }

    private fun showShareProcessFirst() {
        binding.shareProcessGroupOne.visibility = View.VISIBLE
        binding.shareProcessEditShareLink.visibility = View.VISIBLE
        binding.shareProcessGroupTwo.visibility = View.GONE

        if (share != null) {
            setupModificationUI()
        } else {
            setupUpdateUI()
        }

        if (isSecureShare) {
            binding.shareProcessAdvancePermissionTitle.visibility = View.GONE
        }

        // show or hide expiry date
        if (isExpDateShown && !isSecureShare) {
            binding.shareProcessSetExpDateSwitch.visibility = View.VISIBLE
        } else {
            binding.shareProcessSetExpDateSwitch.visibility = View.GONE
        }
        shareProcessStep = SCREEN_TYPE_PERMISSION
    }

    private fun setupModificationUI() {
        if (share?.isFolder == true) updateViewForFolder() else updateViewForFile()

        // read only / allow upload and editing / file drop
        if (SharingMenuHelper.isUploadAndEditingAllowed(share)) {
            binding.shareProcessPermissionUploadEditing.isChecked = true
        } else if (SharingMenuHelper.isFileDrop(share) && share?.isFolder == true) {
            binding.shareProcessPermissionFileDrop.isChecked = true
        } else if (SharingMenuHelper.isReadOnly(share)) {
            binding.shareProcessPermissionReadOnly.isChecked = true
        }

        shareType = share?.shareType ?: ShareType.NO_SHARED

        // show different text for link share and other shares
        // because we have link to share in Public Link
        binding.shareProcessBtnNext.text = getString(
            if (shareType == ShareType.PUBLIC_LINK) {
                R.string.note_share_detail_activity_share_copy_link
            } else {
                R.string.note_share_detail_activity_common_confirm
            }
        )

        updateViewForShareType()
        binding.shareProcessSetPasswordSwitch.isChecked = share?.isPasswordProtected == true
        showPasswordInput(binding.shareProcessSetPasswordSwitch.isChecked)
        updateExpirationDateView()
        showExpirationDateInput(binding.shareProcessSetExpDateSwitch.isChecked)
    }

    private fun setupUpdateUI() {
        binding.shareProcessBtnNext.text =
            getString(R.string.note_share_detail_activity_common_next)
        note.let {
            updateViewForFile()
            updateViewForShareType()
        }
        showPasswordInput(binding.shareProcessSetPasswordSwitch.isChecked)
        showExpirationDateInput(binding.shareProcessSetExpDateSwitch.isChecked)
    }

    private fun updateViewForShareType() {
        when (shareType) {
            ShareType.EMAIL -> {
                updateViewForExternalShare()
            }

            ShareType.PUBLIC_LINK -> {
                updateViewForLinkShare()
            }

            else -> {
                updateViewForInternalShare()
            }
        }
    }

    private fun updateViewForExternalShare() {
        binding.shareProcessChangeNameSwitch.visibility = View.GONE
        binding.shareProcessChangeNameContainer.visibility = View.GONE
        updateViewForExternalAndLinkShare()
    }

    private fun updateViewForLinkShare() {
        updateViewForExternalAndLinkShare()
        binding.shareProcessChangeNameSwitch.visibility = View.VISIBLE
        if (share != null) {
            binding.shareProcessChangeName.setText(share?.label)
            binding.shareProcessChangeNameSwitch.isChecked = !TextUtils.isEmpty(share?.label)
        }
        showChangeNameInput(binding.shareProcessChangeNameSwitch.isChecked)
    }

    private fun updateViewForInternalShare() {
        binding.run {
            shareProcessChangeNameSwitch.visibility = View.GONE
            shareProcessChangeNameContainer.visibility = View.GONE
            shareProcessHideDownloadCheckbox.visibility = View.GONE
            if (isSecureShare) {
                shareProcessAllowResharingCheckbox.visibility = View.GONE
            } else {
                shareProcessAllowResharingCheckbox.visibility = View.VISIBLE
            }
            shareProcessSetPasswordSwitch.visibility = View.GONE

            if (share != null) {
                if (!isReShareShown) {
                    shareProcessAllowResharingCheckbox.visibility = View.GONE
                }
                shareProcessAllowResharingCheckbox.isChecked =
                    SharingMenuHelper.canReshare(share)
            }
        }
    }

    /**
     * update views where share type external or link share
     */
    private fun updateViewForExternalAndLinkShare() {
        binding.run {
            shareProcessHideDownloadCheckbox.visibility = View.VISIBLE
            shareProcessAllowResharingCheckbox.visibility = View.GONE
            shareProcessSetPasswordSwitch.visibility = View.VISIBLE

            if (share != null) {
                if (SharingMenuHelper.isFileDrop(share)) {
                    shareProcessHideDownloadCheckbox.visibility = View.GONE
                } else {
                    shareProcessHideDownloadCheckbox.visibility = View.VISIBLE
                    shareProcessHideDownloadCheckbox.isChecked =
                        share?.isHideFileDownload == true
                }
            }
        }
    }

    /**
     * update expiration date view while modifying the share
     */
    private fun updateExpirationDateView() {
        share?.let { share ->
            if (share.expirationDate > 0) {
                chosenExpDateInMills = share.expirationDate
                binding.shareProcessSetExpDateSwitch.isChecked = true
                binding.shareProcessSelectExpDate.text = getString(
                    R.string.share_expiration_date_format,
                    SimpleDateFormat.getDateInstance().format(Date(share.expirationDate))
                )
            }
        }
    }

    private fun updateViewForFile() {
        binding.shareProcessPermissionUploadEditing.text = getString(R.string.link_share_editing)
        binding.shareProcessPermissionFileDrop.visibility = View.GONE
    }

    private fun updateViewForFolder() {
        binding.run {
            shareProcessPermissionUploadEditing.text =
                getString(R.string.link_share_allow_upload_and_editing)
            shareProcessPermissionFileDrop.visibility = View.VISIBLE
            if (isSecureShare) {
                shareProcessPermissionFileDrop.visibility = View.GONE
                shareProcessAllowResharingCheckbox.visibility = View.GONE
                shareProcessSetExpDateSwitch.visibility = View.GONE
            }
        }
    }

    /**
     * update views for screen type Note
     */
    private fun showShareProcessSecond() {
        binding.run {
            shareProcessGroupOne.visibility = View.GONE
            shareProcessEditShareLink.visibility = View.GONE
            shareProcessGroupTwo.visibility = View.VISIBLE
            if (share != null) {
                shareProcessBtnNext.text =
                    getString(R.string.note_share_detail_activity_set_note)
                noteText.setText(share?.note)
            } else {
                shareProcessBtnNext.text =
                    getString(R.string.note_share_detail_activity_send_share)
                noteText.setText(R.string.empty)
            }
            shareProcessStep = SCREEN_TYPE_NOTE
            shareProcessBtnNext.performClick()
        }
    }

    private fun implementClickEvents() {
        binding.run {
            shareProcessBtnCancel.setOnClickListener {
                onCancelClick()
            }
            shareProcessBtnNext.setOnClickListener {
                if (shareProcessStep == SCREEN_TYPE_PERMISSION) {
                    validateShareProcessFirst()
                } else {
                    createOrUpdateShare()
                }
            }
            shareProcessSetPasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
                showPasswordInput(isChecked)
            }
            shareProcessSetExpDateSwitch.setOnCheckedChangeListener { _, isChecked ->
                showExpirationDateInput(isChecked)
            }
            shareProcessChangeNameSwitch.setOnCheckedChangeListener { _, isChecked ->
                showChangeNameInput(isChecked)
            }
            shareProcessSelectExpDate.setOnClickListener {
                showExpirationDateDialog()
            }
        }
    }

    private fun showExpirationDateDialog(chosenDateInMillis: Long = chosenExpDateInMills) {
        val dialog = ExpirationDatePickerDialogFragment.newInstance(chosenDateInMillis)
        dialog.setOnExpiryDateListener(this)
        expirationDatePickerFragment = dialog
        dialog.show(supportFragmentManager, ExpirationDatePickerDialogFragment.DATE_PICKER_DIALOG)
    }

    private fun showChangeNameInput(isChecked: Boolean) {
        binding.shareProcessChangeNameContainer.visibility =
            if (isChecked) View.VISIBLE else View.GONE
        if (!isChecked) {
            binding.shareProcessChangeName.setText(R.string.empty)
        }
    }

    private fun onCancelClick() {
        // if modifying the existing share then on back press remove the current activity
        if (share != null) {
            finish()
        }

        // else we have to check if user is in step 2(note screen) then show step 1 (permission screen)
        // and if user is in step 1 (permission screen) then remove the activity
        else {
            if (shareProcessStep == SCREEN_TYPE_NOTE) {
                showShareProcessFirst()
            } else {
                finish()
            }
        }
    }

    private fun showExpirationDateInput(isChecked: Boolean) {
        binding.shareProcessSelectExpDate.visibility = if (isChecked) View.VISIBLE else View.GONE
        binding.shareProcessExpDateDivider.visibility = if (isChecked) View.VISIBLE else View.GONE

        // reset the expiration date if switch is unchecked
        if (!isChecked) {
            chosenExpDateInMills = -1
            binding.shareProcessSelectExpDate.text = getString(R.string.empty)
        }
    }

    private fun showPasswordInput(isChecked: Boolean) {
        binding.shareProcessEnterPasswordContainer.visibility =
            if (isChecked) View.VISIBLE else View.GONE

        // reset the password if switch is unchecked
        if (!isChecked) {
            binding.shareProcessEnterPassword.setText(R.string.empty)
        }
    }


    private fun getReSharePermission(): Int {
        return SharePermissionsBuilder().apply {
            setSharePermission(true)
        }.build()
    }

    /**
     * method to validate the step 1 screen information
     */
    @Suppress("ReturnCount")
    private fun validateShareProcessFirst() {
        permission = getSelectedPermission()
        if (permission == OCShare.NO_PERMISSION) {
            DisplayUtils.showSnackMessage(
                binding.root,
                R.string.note_share_detail_activity_no_share_permission_selected
            )
            return
        }

        if (binding.shareProcessSetPasswordSwitch.isChecked &&
            binding.shareProcessEnterPassword.text?.trim().isNullOrEmpty()
        ) {
            DisplayUtils.showSnackMessage(
                binding.root,
                R.string.note_share_detail_activity_share_link_empty_password
            )
            return
        }

        if (binding.shareProcessSetExpDateSwitch.isChecked &&
            binding.shareProcessSelectExpDate.text?.trim().isNullOrEmpty()
        ) {
            showExpirationDateDialog()
            return
        }

        if (binding.shareProcessChangeNameSwitch.isChecked &&
            binding.shareProcessChangeName.text?.trim().isNullOrEmpty()
        ) {
            DisplayUtils.showSnackMessage(
                binding.root,
                R.string.note_share_detail_activity_label_empty
            )
            return
        }

        // if modifying existing share information then execute the process
        if (share != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val noteText = binding.noteText.text.toString().trim()
                val password = binding.shareProcessEnterPassword.text.toString().trim()

                updateShare(noteText, password, false)
            }
        } else {
            // else show step 2 (note screen)
            showShareProcessSecond()
        }
    }

    /**
     *  get the permissions on the basis of selection
     */
    private fun getSelectedPermission() = when {
        binding.shareProcessAllowResharingCheckbox.isChecked -> getReSharePermission()
        binding.shareProcessPermissionReadOnly.isChecked -> OCShare.READ_PERMISSION_FLAG
        binding.shareProcessPermissionUploadEditing.isChecked -> OCShare.MAXIMUM_PERMISSIONS_FOR_FILE
        binding.shareProcessPermissionFileDrop.isChecked -> OCShare.CREATE_PERMISSION_FLAG
        else -> permission
    }

    private fun getExpirationDate(): String? {
        if (chosenExpDateInMills == -1L) {
            return null
        }

        val date = Date(chosenExpDateInMills)

        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    /**
     * method to validate step 2 (note screen) information
     */
    private fun createOrUpdateShare() {
        val noteText = binding.noteText.text.toString().trim()
        val password = binding.shareProcessEnterPassword.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            if (share != null && share?.note != noteText) {
                updateShare(noteText, password, true)
            } else {
                createShare(noteText, password)
            }
        }
    }

    private suspend fun updateShare(noteText: String, password: String, sendEmail: Boolean) {
        val capabilities = repository.capabilities()
        val shouldUseShareAttributesV2 = (capabilities.nextcloudMajorVersion?.toInt() ?: 0) >= 30
        val downloadPermission = !binding.shareProcessHideDownloadCheckbox.isChecked

        val shareAttributes = arrayOf(
            if (shouldUseShareAttributesV2) {
                ShareAttributesV2(
                    scope = "permissions",
                    key = "download",
                    value = downloadPermission
                )
            } else {
                ShareAttributesV1(
                    scope = "permissions",
                    key = "download",
                    enabled = downloadPermission
                )
            }
        )

        val attributes = gson.toJson(shareAttributes)

        val requestBody = UpdateShareRequest(
            share_id = share!!.id.toInt(),
            permissions = if (permission == -1) null else permission,
            password = password,
            publicUpload = "false",
            expireDate = getExpirationDate(),
            note = noteText,
            attributes = attributes,
            sendMail = sendEmail.toString()
        )

        val updateShareResult = repository.updateShare(share!!.id, requestBody)

        if (updateShareResult && sendEmail) {
            val sendEmailResult = repository.sendEmail(share!!.id, SharePasswordRequest(password))
            handleResult(sendEmailResult)
        } else {
            handleResult(updateShareResult)
        }

        if (!sendEmail) {
            withContext(Dispatchers.Main) {
                if (!TextUtils.isEmpty(share?.shareLink)) {
                    ClipboardUtil.copyToClipboard(this@NoteShareDetailActivity, share?.shareLink)
                }
            }
        }
    }

    private suspend fun createShare(noteText: String, password: String) {
        if (note == null || shareeName == null) {
            Log_OC.d(TAG, "validateShareProcessSecond cancelled")
            return
        }

        val result = repository.addShare(
            note!!,
            shareType,
            shareeName!!,
            "false", // TODO: Check how to determine it
            password,
            permission,
            noteText
        )

        if (result != null) {
            repository.getSharesForNotesAndSaveShareEntities()
        }

        handleResult(result != null)
    }

    private suspend fun handleResult(success: Boolean) {
        withContext(Dispatchers.Main) {
            if (success) {
                Intent(this@NoteShareDetailActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }.also {
                    startActivity(it)
                }
            } else {
                DisplayUtils.showSnackMessage(
                    this@NoteShareDetailActivity,
                    getString(R.string.note_share_detail_activity_create_share_error)
                )
            }
        }
    }

    override fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int, chosenDateInMillis: Long) {
        binding.shareProcessSelectExpDate.text = getString(
            R.string.share_expiration_date_format,
            SimpleDateFormat.getDateInstance().format(Date(chosenDateInMillis))
        )
        this.chosenExpDateInMills = chosenDateInMillis
    }

    override fun onDateUnSet() {
        binding.shareProcessSetExpDateSwitch.isChecked = false
    }
}
