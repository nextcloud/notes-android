package it.niedermann.owncloud.notes.share.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import it.niedermann.owncloud.notes.databinding.FileDetailsSharingMenuBottomSheetFragmentBinding;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.listener.FileDetailsSharingMenuBottomSheetActions;

/**
 * File Details Sharing option menus {@link android.app.Dialog} styled as a bottom sheet for main actions.
 */
public class FileDetailSharingMenuBottomSheetDialog extends BottomSheetDialog {
    private FileDetailsSharingMenuBottomSheetFragmentBinding binding;
    private final FileDetailsSharingMenuBottomSheetActions actions;
    private final OCShare ocShare;
    public FileDetailSharingMenuBottomSheetDialog(Activity activity,
                                                  FileDetailsSharingMenuBottomSheetActions actions,
                                                  OCShare ocShare) {
        super(activity);
        this.actions = actions;
        this.ocShare = ocShare;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FileDetailsSharingMenuBottomSheetFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        /*
         viewThemeUtils.platform.themeDialog(binding.getRoot());

        viewThemeUtils.platform.colorImageView(binding.menuIconAddAnotherLink);
        viewThemeUtils.platform.colorImageView(binding.menuIconAdvancedPermissions);
        viewThemeUtils.platform.colorImageView(binding.menuIconSendLink);
        viewThemeUtils.platform.colorImageView(binding.menuIconUnshare);
        viewThemeUtils.platform.colorImageView(binding.menuIconSendNewEmail);
         */

        updateUI();

        setupClickListener();

        setOnShowListener(d ->
                BottomSheetBehavior.from((View) binding.getRoot().getParent())
                        .setPeekHeight(binding.getRoot().getMeasuredHeight())
        );
    }

    private void updateUI() {
        if (ocShare.getShareType() == ShareType.PUBLIC_LINK) {
            binding.menuShareAddAnotherLink.setVisibility(View.VISIBLE);
            binding.menuShareSendLink.setVisibility(View.VISIBLE);
        } else {
            binding.menuShareAddAnotherLink.setVisibility(View.GONE);
            binding.menuShareSendLink.setVisibility(View.GONE);
        }

        if (SharingMenuHelper.isSecureFileDrop(ocShare)) {
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
}
