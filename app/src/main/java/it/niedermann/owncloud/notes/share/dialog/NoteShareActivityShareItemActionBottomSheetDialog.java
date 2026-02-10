/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.nextcloud.android.common.ui.theme.utils.ColorRole;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialog;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemNoteShareActionBinding;
import it.niedermann.owncloud.notes.share.helper.SharePermissionManager;
import it.niedermann.owncloud.notes.share.listener.NoteShareItemAction;

public class NoteShareActivityShareItemActionBottomSheetDialog extends BrandedBottomSheetDialog {
    private ItemNoteShareActionBinding binding;
    private final NoteShareItemAction actions;
    private final OCShare ocShare;
    public NoteShareActivityShareItemActionBottomSheetDialog(Activity activity,
                                                             NoteShareItemAction actions,
                                                             OCShare ocShare) {
        super(activity);
        this.actions = actions;
        this.ocShare = ocShare;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ItemNoteShareActionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        updateUI();

        setupClickListener();

        setOnShowListener(d ->
                BottomSheetBehavior.from((View) binding.getRoot().getParent())
                        .setPeekHeight(binding.getRoot().getMeasuredHeight())
        );
    }

    private void updateUI() {
        if (ocShare.getShareType() != null && ocShare.getShareType() == ShareType.PUBLIC_LINK) {
            binding.menuShareAddAnotherLink.setVisibility(View.VISIBLE);
            binding.menuShareSendLink.setVisibility(View.VISIBLE);
        } else {
            binding.menuShareAddAnotherLink.setVisibility(View.GONE);
            binding.menuShareSendLink.setVisibility(View.GONE);
        }

        if (SharePermissionManager.INSTANCE.isSecureFileDrop(ocShare)) {
            binding.menuShareAdvancedPermissions.setVisibility(View.GONE);
            binding.menuShareAddAnotherLink.setVisibility(View.GONE);
        }
    }

    private void setupClickListener() {
        binding.menuShareAdvancedPermissions.setOnClickListener(v -> {
            actions.advancedPermissions(ocShare);
            dismiss();
        });

        binding.menuShareSendNewEmail.setOnClickListener(v -> {
            actions.sendNewEmail(ocShare);
            dismiss();
        });

        binding.menuShareUnshare.setOnClickListener(v -> {
            actions.unShare(ocShare);
            dismiss();
        });

        binding.menuShareSendLink.setOnClickListener(v -> {
            actions.sendLink(ocShare);
            dismiss();
        });

        binding.menuShareAddAnotherLink.setOnClickListener(v -> {
            actions.addAnotherLink(ocShare);
            dismiss();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding = null;
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, getContext());
        util.platform.themeDialog(binding.getRoot());

        util.platform.colorImageView(binding.menuIconAddAnotherLink, ColorRole.PRIMARY);
        util.platform.colorImageView(binding.menuIconAdvancedPermissions, ColorRole.PRIMARY);
        util.platform.colorImageView(binding.menuIconSendLink, ColorRole.PRIMARY);
        util.platform.colorImageView(binding.menuIconUnshare, ColorRole.PRIMARY);
        util.platform.colorImageView(binding.menuIconSendNewEmail, ColorRole.PRIMARY);
    }
}
