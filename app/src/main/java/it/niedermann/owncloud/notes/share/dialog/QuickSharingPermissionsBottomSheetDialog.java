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

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.owncloud.android.lib.resources.shares.OCShare;

import java.util.List;

import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialog;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.QuickSharingPermissionsBottomSheetFragmentBinding;
import it.niedermann.owncloud.notes.share.adapter.QuickSharingPermissionsAdapter;
import it.niedermann.owncloud.notes.share.helper.SharePermissionManager;
import it.niedermann.owncloud.notes.share.model.QuickPermission;
import it.niedermann.owncloud.notes.util.OCShareExtensionsKt;

/**
 * File Details Quick Sharing permissions options {@link android.app.Dialog} styled as a bottom sheet for main actions.
 */
public class QuickSharingPermissionsBottomSheetDialog extends BrandedBottomSheetDialog {
    private QuickSharingPermissionsBottomSheetFragmentBinding binding;
    private final QuickPermissionSharingBottomSheetActions actions;
    private final Activity activity;
    private final OCShare ocShare;

    private QuickSharingPermissionsAdapter adapter;

    private int color = 0;

    public QuickSharingPermissionsBottomSheetDialog(Activity activity,
                                                    QuickPermissionSharingBottomSheetActions actions,
                                                    OCShare ocShare) {
        super(activity);
        this.actions = actions;
        this.ocShare = ocShare;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = QuickSharingPermissionsBottomSheetFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        setUpRecyclerView();
        setOnShowListener(d ->
                BottomSheetBehavior.from((View) binding.getRoot().getParent())
                        .setPeekHeight(binding.getRoot().getMeasuredHeight())
        );
    }

    private void setUpRecyclerView() {
        List<QuickPermission> quickPermissionList = getQuickPermissionList();
        QuickSharingPermissionsAdapter adapter = new QuickSharingPermissionsAdapter(
            quickPermissionList,
            new QuickSharingPermissionsAdapter.QuickSharingPermissionViewHolder.OnPermissionChangeListener() {
                @Override
                public void onPermissionChanged(int position) {
                    handlePermissionChanged(quickPermissionList, position);
                }

                @Override
                public void onDismissSheet() {
                    dismiss();
                }
            },
            color
        );
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        binding.rvQuickSharePermissions.setLayoutManager(linearLayoutManager);
        binding.rvQuickSharePermissions.setAdapter(adapter);
        adapter.applyBrand(color);
    }

    /**
     * Handle permission changed on click of selected permission
     */
    private void handlePermissionChanged(List<QuickPermission> quickPermissionList, int position) {
        final var type = quickPermissionList.get(position).getType();
        int permissionFlag = type.getPermissionFlag(ocShare.isFolder());
        actions.onQuickPermissionChanged(ocShare, permissionFlag);
        dismiss();
    }

    /**
     * Prepare the list of permissions needs to be displayed on recyclerview
     */
    private List<QuickPermission> getQuickPermissionList() {
        final var selectedType = SharePermissionManager.INSTANCE.getSelectedType(ocShare, false);
        final var hasFileRequestPermission = OCShareExtensionsKt.hasFileRequestPermission(ocShare);
        return selectedType.getAvailablePermissions(hasFileRequestPermission);
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding = null;
    }

    @Override
    public void applyBrand(int color) {
        this.color = color;
        final var util = BrandingUtil.of(color, getContext());
        if (adapter != null) {
            adapter.applyBrand(color);
        }
        util.platform.themeDialog(binding.getRoot());
    }

    public interface QuickPermissionSharingBottomSheetActions {
        void onQuickPermissionChanged(OCShare share, int permission);
    }
}
