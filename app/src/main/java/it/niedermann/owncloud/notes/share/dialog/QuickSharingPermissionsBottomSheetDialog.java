package it.niedermann.owncloud.notes.share.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.owncloud.android.lib.resources.shares.OCShare;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

import static com.owncloud.android.lib.resources.shares.OCShare.CREATE_PERMISSION_FLAG;
import static com.owncloud.android.lib.resources.shares.OCShare.MAXIMUM_PERMISSIONS_FOR_FILE;
import static com.owncloud.android.lib.resources.shares.OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER;
import static com.owncloud.android.lib.resources.shares.OCShare.READ_PERMISSION_FLAG;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.QuickSharingPermissionsBottomSheetFragmentBinding;
import it.niedermann.owncloud.notes.share.adapter.QuickSharingPermissionsAdapter;
import it.niedermann.owncloud.notes.share.helper.SharingMenuHelper;
import it.niedermann.owncloud.notes.share.model.QuickPermissionModel;

/**
 * File Details Quick Sharing permissions options {@link android.app.Dialog} styled as a bottom sheet for main actions.
 */
public class QuickSharingPermissionsBottomSheetDialog extends BottomSheetDialog {
    private QuickSharingPermissionsBottomSheetFragmentBinding binding;
    private final QuickPermissionSharingBottomSheetActions actions;
    private final Activity activity;
    private final OCShare ocShare;

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

        // viewThemeUtils.platform.themeDialog(binding.getRoot());

        setUpRecyclerView();
        setOnShowListener(d ->
                BottomSheetBehavior.from((View) binding.getRoot().getParent())
                        .setPeekHeight(binding.getRoot().getMeasuredHeight())
        );
    }

    private void setUpRecyclerView() {
        List<QuickPermissionModel> quickPermissionModelList = getQuickPermissionList();
        QuickSharingPermissionsAdapter adapter = new QuickSharingPermissionsAdapter(
                quickPermissionModelList,
                new QuickSharingPermissionsAdapter.QuickSharingPermissionViewHolder.OnPermissionChangeListener() {
                    @Override
                    public void onPermissionChanged(int position) {
                        handlePermissionChanged(quickPermissionModelList, position);
                    }

                    @Override
                    public void onDismissSheet() {
                        dismiss();
                    }
                }
        );
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        binding.rvQuickSharePermissions.setLayoutManager(linearLayoutManager);
        binding.rvQuickSharePermissions.setAdapter(adapter);
    }

    private void handlePermissionChanged(List<QuickPermissionModel> quickPermissionModelList, int position) {
        if (quickPermissionModelList.get(position).getPermissionName().equalsIgnoreCase(activity.getResources().getString(R.string.link_share_allow_upload_and_editing))
                || quickPermissionModelList.get(position).getPermissionName().equalsIgnoreCase(activity.getResources().getString(R.string.link_share_editing))) {
            if (ocShare.isFolder()) {
                actions.onQuickPermissionChanged(ocShare,
                        MAXIMUM_PERMISSIONS_FOR_FOLDER);
            } else {
                actions.onQuickPermissionChanged(ocShare,
                        MAXIMUM_PERMISSIONS_FOR_FILE);
            }
        } else if (quickPermissionModelList.get(position).getPermissionName().equalsIgnoreCase(activity.getResources().getString(R.string
                .link_share_view_only))) {
            actions.onQuickPermissionChanged(ocShare,
                    READ_PERMISSION_FLAG);

        } else if (quickPermissionModelList.get(position).getPermissionName().equalsIgnoreCase(activity.getResources().getString(R.string
                .link_share_file_drop))) {
            actions.onQuickPermissionChanged(ocShare,
                    CREATE_PERMISSION_FLAG);
        }
        dismiss();
    }

    /**
     * prepare the list of permissions needs to be displayed on recyclerview
     * @return
     */
    private List<QuickPermissionModel> getQuickPermissionList() {

        String[] permissionArray;
        if (ocShare.isFolder()) {
            permissionArray =
                    activity.getResources().getStringArray(R.array.quick_sharing_permission_bottom_sheet_dialog_folder_share_values);
        } else {
            permissionArray =
                    activity.getResources().getStringArray(R.array.quick_sharing_permission_bottom_sheet_dialog_note_share_values);
        }
        //get the checked item position
        int checkedItem = SharingMenuHelper.getPermissionCheckedItem(activity, ocShare, permissionArray);


        final List<QuickPermissionModel> quickPermissionModelList = new ArrayList<>(permissionArray.length);
        for (int i = 0; i < permissionArray.length; i++) {
            QuickPermissionModel quickPermissionModel = new QuickPermissionModel(permissionArray[i], checkedItem == i);
            quickPermissionModelList.add(quickPermissionModel);
        }
        return quickPermissionModelList;
    }


    @Override
    protected void onStop() {
        super.onStop();
        binding = null;
    }

    public interface QuickPermissionSharingBottomSheetActions {
        void onQuickPermissionChanged(OCShare share, int permission);
    }
}
