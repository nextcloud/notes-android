/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.niedermann.owncloud.notes.NotesApplication;

public class DeleteAlertDialogBuilder extends MaterialAlertDialogBuilder {

    protected AlertDialog dialog;

    public DeleteAlertDialogBuilder(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public AlertDialog create() {
        this.dialog = super.create();
        applyBrand();
        dialog.setOnShowListener(dialog -> applyBrand());
        return dialog;
    }

    public void applyBrand() {
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) instanceof MaterialButton positiveButton) {
            NotesApplication.brandingUtil().material.colorMaterialButtonPrimaryTonal(positiveButton);
        }

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) instanceof MaterialButton negativeButton) {
            NotesApplication.brandingUtil().material.colorMaterialButtonPrimaryBorderless(negativeButton);
        }
    }
}
