/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2017-2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2015 María Asensio Valverde <masensio@solidgear.es>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: GPL-2.0-only AND (AGPL-3.0-or-later OR GPL-2.0-only)
 */
package it.niedermann.owncloud.notes.share.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.owncloud.android.lib.resources.shares.OCShare
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.PasswordDialogBinding
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.util.KeyboardUtils
import it.niedermann.owncloud.notes.shared.util.extensions.getParcelableArgument
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument

/**
 * Dialog to input the password for sharing a file/folder.
 *
 *
 * Triggers the share when the password is introduced.
 */
class SharePasswordDialogFragment : BrandedDialogFragment() {

    private var keyboardUtils: KeyboardUtils? = null

    private var binding: PasswordDialogBinding? = null
    private var note: Note? = null
    private var share: OCShare? = null
    private var createShare = false
    private var askForPassword = false
    private var builder: MaterialAlertDialogBuilder? = null
    private var listener: SharePasswordDialogListener? = null

    interface SharePasswordDialogListener {
        fun shareFileViaPublicShare(note: Note?, password: String?)
        fun setPasswordToShare(share: OCShare, password: String?)
    }

    override fun onStart() {
        super.onStart()

        val alertDialog = dialog as AlertDialog?
        if (alertDialog == null) {
            return
        }

        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE) as MaterialButton?
        positiveButton?.setOnClickListener {
            val sharePassword = binding?.sharePassword?.text

            if (sharePassword != null) {
                val password = sharePassword.toString()
                if (!askForPassword && TextUtils.isEmpty(password)) {
                    BrandedSnackbar.make(
                        binding!!.root,
                        getString(R.string.note_share_detail_activity_share_link_empty_password),
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    return@setOnClickListener
                }
                if (share == null) {
                    listener?.shareFileViaPublicShare(note, password)
                } else {
                    listener?.setPasswordToShare(share!!, password)
                }
            }

            alertDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        keyboardUtils?.showKeyboardForEditText(binding!!.sharePassword)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        note = requireArguments().getSerializableArgument(ARG_FILE, Note::class.java)
        share = requireArguments().getParcelableArgument(ARG_SHARE, OCShare::class.java)

        createShare = requireArguments().getBoolean(ARG_CREATE_SHARE, false)
        askForPassword = requireArguments().getBoolean(ARG_ASK_FOR_PASSWORD, false)

        // Inflate the layout for the dialog
        val inflater = requireActivity().layoutInflater
        binding = PasswordDialogBinding.inflate(inflater, null, false)

        // Setup layout
        binding?.sharePassword?.setText(R.string.empty)

        val neutralButtonTextId: Int
        val title: Int
        if (askForPassword) {
            title = R.string.share_password_dialog_fragment_share_link_optional_password_title
            neutralButtonTextId = R.string.share_password_dialog_fragment_skip
        } else {
            title = R.string.share_link_password_title
            neutralButtonTextId = R.string.note_share_detail_activity_cancel
        }

        // Build the dialog
        builder = MaterialAlertDialogBuilder(requireContext())
        builder!!.setView(binding!!.root)
            .setPositiveButton(R.string.common_ok, null)
            .setNegativeButton(R.string.common_delete) { _: DialogInterface?, _: Int -> callSetPassword() }
            .setNeutralButton(neutralButtonTextId) { _: DialogInterface?, _: Int ->
                if (askForPassword) {
                    callSetPassword()
                }
            }
            .setTitle(title)

        return builder!!.create()
    }

    private fun callSetPassword() {
        val password = binding?.sharePassword?.text.toString().trim()
        if (share == null) {
            listener?.shareFileViaPublicShare(note, password)
        } else {
            listener?.setPasswordToShare(share!!, password)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, requireContext())
        builder?.let {
            util?.dialog?.colorMaterialAlertDialogBackground(requireContext(), it)
        }

        val alertDialog = dialog as AlertDialog?
        val positiveButton = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE) as? MaterialButton?
        if (positiveButton != null) {
            util?.material?.colorMaterialButtonPrimaryTonal(positiveButton)
        }

        val negativeButton = alertDialog?.getButton(AlertDialog.BUTTON_NEGATIVE) as? MaterialButton?
        if (negativeButton != null) {
            util?.material?.colorMaterialButtonPrimaryBorderless(negativeButton)
        }

        val neutralButton = alertDialog?.getButton(AlertDialog.BUTTON_NEUTRAL) as? MaterialButton?
        if (neutralButton != null) {
            val warningColorId =
                ContextCompat.getColor(requireContext(), R.color.highlight_textColor_Warning)
            util?.platform?.colorTextButtons(warningColorId, neutralButton)
        }

        binding?.sharePasswordContainer?.let {
            util?.material?.colorTextInputLayout(it)
        }
    }

    companion object {
        private const val ARG_FILE = "FILE"
        private const val ARG_SHARE = "SHARE"
        private const val ARG_CREATE_SHARE = "CREATE_SHARE"
        private const val ARG_ASK_FOR_PASSWORD = "ASK_FOR_PASSWORD"
        const val PASSWORD_FRAGMENT = "PASSWORD_FRAGMENT"

        /**
         * Public factory method to create new SharePasswordDialogFragment instances.
         *
         * @param note        Note bound to the public share that which
         * password will be set or updated
         * @param createShare When 'true', the request for password will be
         * followed by the creation of a new public link
         * when 'false', a public share is assumed to exist, and the password is bound to it.
         * @return Dialog ready to show.
         */
        @JvmStatic
        fun newInstance(
            note: Note?,
            createShare: Boolean,
            askForPassword: Boolean,
            dialogListener: SharePasswordDialogListener
        ): SharePasswordDialogFragment {
            val bundle = Bundle().apply {
                putSerializable(ARG_FILE, note)
                putBoolean(ARG_CREATE_SHARE, createShare)
                putBoolean(ARG_ASK_FOR_PASSWORD, askForPassword)
            }

            return SharePasswordDialogFragment().apply {
                listener = dialogListener
                arguments = bundle
            }
        }

        /**
         * Public factory method to create new SharePasswordDialogFragment instances.
         *
         * @param share OCFile bound to the public share that which password will be set or updated
         * @return Dialog ready to show.
         */
        @JvmStatic
        fun newInstance(
            share: OCShare?,
            askForPassword: Boolean,
            dialogListener: SharePasswordDialogListener
        ): SharePasswordDialogFragment {
            val bundle = Bundle().apply {
                putParcelable(ARG_SHARE, share)
                putBoolean(ARG_ASK_FOR_PASSWORD, askForPassword)
            }

            return SharePasswordDialogFragment().apply {
                listener = dialogListener
                arguments = bundle
            }
        }

        /**
         * Public factory method to create new SharePasswordDialogFragment instances.
         *
         * @param share OCFile bound to the public share that which password will be set or updated
         * @return Dialog ready to show.
         */
        fun newInstance(
            share: OCShare?,
            dialogListener: SharePasswordDialogListener
        ): SharePasswordDialogFragment {
            val bundle = Bundle().apply {
                putParcelable(ARG_SHARE, share)
            }

            return SharePasswordDialogFragment().apply {
                listener = dialogListener
                arguments = bundle
            }
        }
    }
}
