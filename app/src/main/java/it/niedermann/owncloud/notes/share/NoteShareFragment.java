package it.niedermann.owncloud.notes.share;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.databinding.FragmentNoteShareBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.share.adapter.ShareeListAdapter;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;
import it.niedermann.owncloud.notes.shared.user.User;
import it.niedermann.owncloud.notes.shared.util.ClipboardUtil;
import it.niedermann.owncloud.notes.shared.util.extensions.BundleExtensionsKt;

public class NoteShareFragment extends Fragment implements ShareeListAdapterListener {

    private static final String TAG = "NoteShareFragment";
    private static final String ARG_NOTE = "NOTE";
    private static final String ARG_ACCOUNT = "ACCOUNT";
    private static final String ARG_USER = "USER";

    private FragmentNoteShareBinding binding;
    private Note note;
    private User user;
    private Account account;

    private OnEditShareListener onEditShareListener;

    public static NoteShareFragment newInstance(Note note, User user, Account account) {
        NoteShareFragment fragment = new NoteShareFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE, note);
        args.putSerializable(ARG_ACCOUNT, account);
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            note = BundleExtensionsKt.getSerializableArgument(savedInstanceState, ARG_NOTE, Note.class);
            account = BundleExtensionsKt.getSerializableArgument(savedInstanceState, ARG_ACCOUNT, Account.class);
            user = BundleExtensionsKt.getParcelableArgument(savedInstanceState, ARG_USER, User.class);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                note = BundleExtensionsKt.getSerializableArgument(arguments, ARG_NOTE, Note.class);
                account = BundleExtensionsKt.getSerializableArgument(arguments, ARG_ACCOUNT, Account.class);
                user = BundleExtensionsKt.getParcelableArgument(arguments, ARG_USER, User.class);
            }
        }

        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        if (user == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshCapabilitiesFromDB();
        refreshSharesFromDB();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNoteShareBinding.inflate(inflater, container, false);

        binding.sharesList.setAdapter(new ShareeListAdapter(requireActivity(),
                new ArrayList<>(),
                this,
                user,
                account));

        binding.sharesList.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.pickContactEmailBtn.setOnClickListener(v -> checkContactPermission());

        setupView();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onEditShareListener = (OnEditShareListener) context;
        } catch (Exception e) {
            throw new IllegalArgumentException("Calling activity must implement the interface", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        searchConfig.setSearchOnlyUsers(file.isEncrypted());
    }

    @Override
    public void onStop() {
        super.onStop();
        searchConfig.reset();
    }

    private void setupView() {
        setShareWithYou();

        OCFile parentFile = fileDataStorageManager.getFileById(file.getParentId());

        FileDetailSharingFragmentHelper.setupSearchView(
                (SearchManager) fileActivity.getSystemService(Context.SEARCH_SERVICE),
                binding.searchView,
                fileActivity.getComponentName());
        viewThemeUtils.androidx.themeToolbarSearchView(binding.searchView);

        if (file.canReshare()) {
            binding.searchView.setQueryHint(getResources().getString(R.string.note_share_fragment_search_text));
        } else {
            binding.searchView.setQueryHint(getResources().getString(R.string.note_share_fragment_resharing_not_allowed));
            binding.searchView.setInputType(InputType.TYPE_NULL);
            binding.pickContactEmailBtn.setVisibility(View.GONE);
            disableSearchView(binding.searchView);
        }
    }

    private void disableSearchView(View view) {
        view.setEnabled(false);

        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                disableSearchView(viewGroup.getChildAt(i));
            }
        }
    }

    // TODO: Check if note.getAccountId() return note's owner's id
    private boolean accountOwnsFile() {
        String noteId = String.valueOf(note.getAccountId());
        return TextUtils.isEmpty(noteId) || account.getAccountName().split("@")[0].equalsIgnoreCase(noteId);
    }

    private void setShareWithYou() {
        if (accountOwnsFile()) {
            binding.sharedWithYouContainer.setVisibility(View.GONE);
        } else {

            /*
            // TODO: How to get owner display name from note?

             binding.sharedWithYouUsername.setText(
                    String.format(getString(R.string.note_share_fragment_shared_with_you), file.getOwnerDisplayName()));
             */


            Glide.with(this)
                    .load(new SingleSignOnUrl(account.getAccountName(), account.getUrl() + "/index.php/avatar/" + Uri.encode(account.getUserName()) + "/64"))
                    .placeholder(R.drawable.ic_account_circle_grey_24dp)
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.sharedWithYouAvatar);

            binding.sharedWithYouAvatar.setVisibility(View.VISIBLE);

            /*
            // TODO: Note's note?
            String note = file.getNote();

            if (!TextUtils.isEmpty(note)) {
                binding.sharedWithYouNote.setText(file.getNote());
                binding.sharedWithYouNoteContainer.setVisibility(View.VISIBLE);
            } else {
                binding.sharedWithYouNoteContainer.setVisibility(View.GONE);
            }
             */
        }
    }

    public void copyInternalLink() {
        if (account == null) {
            BrandedSnackbar.make(requireView(), getString(R.string.note_share_fragment_could_not_retrieve_url), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.sharesList)
                    .show();
            return;
        }

        showShareLinkDialog();
    }

    private void showShareLinkDialog() {
        String link = createInternalLink();

        Intent intentToShareLink = new Intent(Intent.ACTION_SEND);

        intentToShareLink.putExtra(Intent.EXTRA_TEXT, link);
        intentToShareLink.setType("text/plain");
        intentToShareLink.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.note_share_fragment_subject_shared_with_you, note.getTitle()));

        String[] packagesToExclude = new String[] { requireContext().getPackageName() };
        DialogFragment chooserDialog = ShareLinkToDialog.newInstance(intentToShareLink, packagesToExclude);
        chooserDialog.show(getParentFragmentManager(), FileDisplayActivity.FTAG_CHOOSER_DIALOG);
    }

    // TODO: Check account.getUrl returning base url?
    private String createInternalLink() {
        Uri baseUri = Uri.parse(account.getUrl());
        return baseUri + "/index.php/f/" +  note.getId();
    }

    // TODO: Capabilities in notes app doesn't have following functions...
    public void createPublicShareLink() {
        if (capabilities != null && (capabilities.getFilesSharingPublicPasswordEnforced().isTrue() ||
                capabilities.getFilesSharingPublicAskForOptionalPassword().isTrue())) {
            // password enforced by server, request to the user before trying to create
            requestPasswordForShareViaLink(true,
                    capabilities.getFilesSharingPublicAskForOptionalPassword().isTrue());

        } else {
            // TODO: Share files logic not suitable with notes app. How can I get remote path from remote id of the note
            // Is CreateShareViaLink operation compatible?

            Intent service = new Intent(fileActivity, OperationsService.class);
            service.setAction(OperationsService.ACTION_CREATE_SHARE_VIA_LINK);
            service.putExtra(OperationsService.EXTRA_ACCOUNT, fileActivity.getAccount());
            if (!TextUtils.isEmpty(password)) {
                service.putExtra(OperationsService.EXTRA_SHARE_PASSWORD, password);
            }
            service.putExtra(OperationsService.EXTRA_REMOTE_PATH, file.getRemotePath());
            mWaitingForOpId = fileActivity.getOperationsServiceBinder().queueNewOperation(service);
        }
    }

    private void createSecureFileDrop() {
        fileOperationsHelper.shareFolderViaSecureFileDrop(file);
    }

    /*
    // TODO: Cant call getFileWithLink

     public void getFileWithLink(@NonNull OCFile file, final ViewThemeUtils viewThemeUtils) {
        List<OCShare> shares = fileActivity.getStorageManager().getSharesByPathAndType(file.getRemotePath(),
                                                                                       ShareType.PUBLIC_LINK,
                                                                                       "");

        if (shares.size() == SINGLE_LINK_SIZE) {
            FileActivity.copyAndShareFileLink(fileActivity, file, shares.get(0).getShareLink(), viewThemeUtils);
        } else {
            if (fileActivity instanceof FileDisplayActivity) {
                ((FileDisplayActivity) fileActivity).showDetails(file, 1);
            } else {
                showShareFile(file);
            }
        }

        fileActivity.refreshList();
    }
     */
    private void showSendLinkTo(OCShare publicShare) {
        if (file.isSharedViaLink()) {
            if (TextUtils.isEmpty(publicShare.getShareLink())) {
                fileOperationsHelper.getFileWithLink(file, viewThemeUtils);
            } else {
                // TODO: get link from public share and pass to the function
                showShareLinkDialog();
            }
        }
    }

    public void copyLink(OCShare share) {
        if (file.isSharedViaLink()) {
            if (TextUtils.isEmpty(share.getShareLink())) {
                fileOperationsHelper.getFileWithLink(file, viewThemeUtils);
            } else {
                ClipboardUtil.copyToClipboard(requireActivity(), share.getShareLink());
            }
        }
    }

    /**
     * show share action bottom sheet
     *
     * @param share
     */
    @Override
    @VisibleForTesting
    public void showSharingMenuActionSheet(OCShare share) {
        if (fileActivity != null && !fileActivity.isFinishing()) {
            new FileDetailSharingMenuBottomSheetDialog(fileActivity, this, share, viewThemeUtils).show();
        }
    }

    /**
     * show quick sharing permission dialog
     *
     * @param share
     */
    @Override
    public void showPermissionsDialog(OCShare share) {
        new QuickSharingPermissionsBottomSheetDialog(fileActivity, this, share, viewThemeUtils).show();
    }

    /**
     * Updates the UI after the result of an update operation on the edited {@link OCFile}.
     *
     * @param result {@link RemoteOperationResult} of an update on the edited {@link OCFile} sharing information.
     * @param file   the edited {@link OCFile}
     * @see #onUpdateShareInformation(RemoteOperationResult)
     */
    public void onUpdateShareInformation(RemoteOperationResult result, OCFile file) {
        this.file = file;

        onUpdateShareInformation(result);
    }

    /**
     * Updates the UI after the result of an update operation on the edited {@link OCFile}. Keeps the current {@link
     * OCFile held by this fragment}.
     *
     * @param result {@link RemoteOperationResult} of an update on the edited {@link OCFile} sharing information.
     * @see #onUpdateShareInformation(RemoteOperationResult, OCFile)
     */
    public void onUpdateShareInformation(RemoteOperationResult result) {
        if (result.isSuccess()) {
            refreshUiFromDB();
        } else {
            setupView();
        }
    }

    /**
     * Get {@link OCShare} instance from DB and updates the UI.
     */
    private void refreshUiFromDB() {
        refreshSharesFromDB();
        // Updates UI with new state
        setupView();
    }

    private void unshareWith(OCShare share) {
        fileOperationsHelper.unshareShare(file, share);
    }

    /**
     * Starts a dialog that requests a password to the user to protect a share link.
     *
     * @param createShare    When 'true', the request for password will be followed by the creation of a new public
     *                       link; when 'false', a public share is assumed to exist, and the password is bound to it.
     * @param askForPassword if true, password is optional
     */
    public void requestPasswordForShareViaLink(boolean createShare, boolean askForPassword) {
        SharePasswordDialogFragment dialog = SharePasswordDialogFragment.newInstance(file,
                createShare,
                askForPassword);
        dialog.show(getChildFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT);
    }

    @Override
    public void requestPasswordForShare(OCShare share, boolean askForPassword) {
        SharePasswordDialogFragment dialog = SharePasswordDialogFragment.newInstance(share, askForPassword);
        dialog.show(getChildFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT);
    }

    @Override
    public void showProfileBottomSheet(User user, String shareWith) {
        if (user.getServer().getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_23)) {
            new RetrieveHoverCardAsyncTask(user,
                    shareWith,
                    fileActivity,
                    clientFactory,
                    viewThemeUtils).execute();
        }
    }

    /**
     * Get known server capabilities from DB
     */
    public void refreshCapabilitiesFromDB() {
        capabilities = fileDataStorageManager.getCapability(user.getAccountName());
    }

    /**
     * Get public link from the DB to fill in the "Share link" section in the UI. Takes into account server capabilities
     * before reading database.
     */
    public void refreshSharesFromDB() {
        OCFile newFile = fileDataStorageManager.getFileById(file.getFileId());
        if (newFile != null) {
            file = newFile;
        }

        ShareeListAdapter adapter = (ShareeListAdapter) binding.sharesList.getAdapter();

        if (adapter == null) {
            BrandedSnackbar.make(requireView(), getString(R.string.could_not_retrieve_shares), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        adapter.getShares().clear();

        // to show share with users/groups info
        List<OCShare> shares = fileDataStorageManager.getSharesWithForAFile(file.getRemotePath(),
                user.getAccountName());

        adapter.addShares(shares);

        if (FileDetailSharingFragmentHelper.isPublicShareDisabled(capabilities) || !file.canReshare()) {
            return;
        }

        // Get public share
        List<OCShare> publicShares = fileDataStorageManager.getSharesByPathAndType(file.getRemotePath(),
                ShareType.PUBLIC_LINK,
                "");

        if (publicShares.isEmpty() && containsNoNewPublicShare(adapter.getShares()) &&
                (!file.isEncrypted() || capabilities.getEndToEndEncryption().isTrue())) {
            final OCShare ocShare = new OCShare();
            ocShare.setShareType(ShareType.NEW_PUBLIC_LINK);
            publicShares.add(ocShare);
        } else {
            adapter.removeNewPublicShare();
        }

        adapter.addShares(publicShares);
    }

    private void checkContactPermission() {
        if (PermissionUtil.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS)) {
            pickContactEmail();
        } else {
            requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void pickContactEmail() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            onContactSelectionResultLauncher.launch(intent);
        } else {
            BrandedSnackbar.make(requireView(), getString(R.string.file_detail_sharing_fragment_no_contact_app_message), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void handleContactResult(@NonNull Uri contactUri) {
        // Define the projection to get all email addresses.
        String[] projection = {ContactsContract.CommonDataKinds.Email.ADDRESS};

        Cursor cursor = fileActivity.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // The contact has only one email address, use it.
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                if (columnIndex != -1) {
                    // Use the email address as needed.
                    // email variable contains the selected contact's email address.
                    String email = cursor.getString(columnIndex);
                    binding.searchView.post(() -> {
                        binding.searchView.setQuery(email, false);
                        binding.searchView.requestFocus();
                    });
                } else {
                    BrandedSnackbar.make(requireView(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                            .show();
                    Log_OC.e(NoteShareFragment.class.getSimpleName(), "Failed to pick email address.");
                }
            } else {
                BrandedSnackbar.make(requireView(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                        .show();
                Log_OC.e(NoteShareFragment.class.getSimpleName(), "Failed to pick email address as no Email found.");
            }
            cursor.close();
        } else {
            BrandedSnackbar.make(requireView(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                    .show();
            Log_OC.e(NoteShareFragment.class.getSimpleName(), "Failed to pick email address as Cursor is null.");
        }
    }

    private boolean containsNoNewPublicShare(List<OCShare> shares) {
        for (OCShare share : shares) {
            if (share.getShareType() == ShareType.NEW_PUBLIC_LINK) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_NOTE, note);
        outState.putSerializable(ARG_ACCOUNT, account);
        outState.putParcelable(ARG_USER, user);
    }

    @Override
    public void avatarGenerated(Drawable avatarDrawable, Object callContext) {
        binding.sharedWithYouAvatar.setImageDrawable(avatarDrawable);
    }

    @Override
    public boolean shouldCallGeneratedCallback(String tag, Object callContext) {
        return false;
    }

    private boolean isReshareForbidden(OCShare share) {
        return ShareType.FEDERATED == share.getShareType() ||
                capabilities != null && capabilities.getFilesSharingResharing().isFalse();
    }

    @VisibleForTesting
    public void search(String query) {
        SearchView searchView = requireView().findViewById(R.id.searchView);
        searchView.setQuery(query, true);
    }

    @Override
    public void advancedPermissions(OCShare share) {
        modifyExistingShare(share, FileDetailsSharingProcessFragment.SCREEN_TYPE_PERMISSION);
    }


    @Override
    public void sendNewEmail(OCShare share) {
        modifyExistingShare(share, FileDetailsSharingProcessFragment.SCREEN_TYPE_NOTE);
    }

    @Override
    public void unShare(OCShare share) {
        unshareWith(share);
        ShareeListAdapter adapter = (ShareeListAdapter) binding.sharesList.getAdapter();
        if (adapter == null) {
            BrandedSnackbar.make(requireView(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        adapter.remove(share);
    }

    @Override
    public void sendLink(OCShare share) {
        if (file.isSharedViaLink() && !TextUtils.isEmpty(share.getShareLink())) {
            FileDisplayActivity.showShareLinkDialog(fileActivity, file, share.getShareLink());
        } else {
            showSendLinkTo(share);
        }
    }

    @Override
    public void addAnotherLink(OCShare share) {
        createPublicShareLink();
    }

    private void modifyExistingShare(OCShare share, int screenTypePermission) {
        onEditShareListener.editExistingShare(share, screenTypePermission, !isReshareForbidden(share),
                capabilities.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_18));
    }

    @Override
    public void onQuickPermissionChanged(OCShare share, int permission) {
        fileOperationsHelper.setPermissionsToShare(share, permission);
    }

    //launcher for contact permission
    private final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickContactEmail();
                } else {
                    BrandedSnackbar.make(binding.getRoot(), getString(R.string.contact_no_permission), Snackbar.LENGTH_LONG)
                            .show();
                }
            });

    //launcher to handle contact selection
    private final ActivityResultLauncher<Intent> onContactSelectionResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent == null) {
                                BrandedSnackbar.make(binding.getRoot(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            Uri contactUri = intent.getData();
                            if (contactUri == null) {
                                BrandedSnackbar.make(binding.getRoot(), getString(R.string.email_pick_failed), Snackbar.LENGTH_LONG)
                                        .show();
                                return;
                            }

                            handleContactResult(contactUri);

                        }
                    });

    public interface OnEditShareListener {
        void editExistingShare(OCShare share, int screenTypePermission, boolean isReshareShown,
                               boolean isExpiryDateShown);

        void onShareProcessClosed();
    }
}
