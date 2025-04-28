package it.niedermann.owncloud.notes.share;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedActivity;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityNoteShareBinding;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.ApiResult;
import it.niedermann.owncloud.notes.persistence.ApiResultKt;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.share.adapter.ShareeListAdapter;
import it.niedermann.owncloud.notes.share.adapter.SuggestionAdapter;
import it.niedermann.owncloud.notes.share.dialog.NoteShareActivityShareItemActionBottomSheetDialog;
import it.niedermann.owncloud.notes.share.dialog.QuickSharingPermissionsBottomSheetDialog;
import it.niedermann.owncloud.notes.share.dialog.ShareLinkToDialog;
import it.niedermann.owncloud.notes.share.dialog.SharePasswordDialogFragment;
import it.niedermann.owncloud.notes.share.helper.AvatarLoader;
import it.niedermann.owncloud.notes.share.helper.UsersAndGroupsSearchProvider;
import it.niedermann.owncloud.notes.share.listener.NoteShareItemAction;
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener;
import it.niedermann.owncloud.notes.share.model.CreateShareResponse;
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig;
import it.niedermann.owncloud.notes.share.repository.ShareRepository;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.OcsResponse;
import it.niedermann.owncloud.notes.shared.util.DisplayUtils;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;
import it.niedermann.owncloud.notes.shared.util.clipboard.ClipboardUtil;
import it.niedermann.owncloud.notes.shared.util.extensions.BundleExtensionsKt;

public class NoteShareActivity extends BrandedActivity implements ShareeListAdapterListener, NoteShareItemAction, QuickSharingPermissionsBottomSheetDialog.QuickPermissionSharingBottomSheetActions, SharePasswordDialogFragment.SharePasswordDialogListener {

    private static final String TAG = "NoteShareActivity";
    public static final String ARG_NOTE = "NOTE";
    public static final String ARG_ACCOUNT = "ACCOUNT";
    public static final String FTAG_CHOOSER_DIALOG = "CHOOSER_DIALOG";

    private ScheduledExecutorService executorService;
    private Future<?> future;
    private static final long SEARCH_DELAY_MS = 500;

    private ActivityNoteShareBinding binding;
    private Note note;
    private Account account;
    private ShareRepository repository;
    private Capabilities capabilities;
    private final List<OCShare> shares = new ArrayList<>();

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executorService = Executors.newSingleThreadScheduledExecutor();
        binding = ActivityNoteShareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeArguments();
    }

    private void initializeArguments() {
        Bundle bundler = getIntent().getExtras();
        note = BundleExtensionsKt.getSerializableArgument(bundler, ARG_NOTE, Note.class);
        account = BundleExtensionsKt.getSerializableArgument(bundler, ARG_ACCOUNT, Account.class);
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        executorService.submit(() -> {
            try {
                final var ssoAcc = SingleAccountHelper.getCurrentSingleSignOnAccount(NoteShareActivity.this);
                repository = new ShareRepository(NoteShareActivity.this, ssoAcc);
                capabilities = repository.getCapabilities();
                repository.getSharesForNotesAndSaveShareEntities();

                runOnUiThread(() -> {
                    binding.searchContainer.setVisibility(View.VISIBLE);
                    binding.sharesList.setVisibility(View.VISIBLE);
                    binding.sharesList.setAdapter(new ShareeListAdapter(this, new ArrayList<>(), this, account));
                    binding.sharesList.setLayoutManager(new LinearLayoutManager(this));
                    binding.pickContactEmailBtn.setOnClickListener(v -> checkContactPermission());
                    binding.btnShareButton.setOnClickListener(v -> ShareUtil.openShareDialog(this, note.getTitle(), note.getContent()));

                    if (note.getReadonly()) {
                        setupReadOnlySearchView();
                    } else {
                        setupSearchView((SearchManager) getSystemService(Context.SEARCH_SERVICE), getComponentName());
                    }

                    refreshSharesFromDB();
                    binding.loadingLayout.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        UsersAndGroupsSearchConfig.INSTANCE.setSearchOnlyUsers(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        UsersAndGroupsSearchConfig.INSTANCE.reset();
    }

    private void disableSearchView(View view) {
        view.setEnabled(false);

        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                disableSearchView(viewGroup.getChildAt(i));
            }
        }
    }

    private void setupReadOnlySearchView() {
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.setQueryHint(getResources().getString(R.string.note_share_activity_resharing_not_allowed));
        binding.searchView.setInputType(InputType.TYPE_NULL);
        binding.pickContactEmailBtn.setVisibility(View.GONE);
        disableSearchView(binding.searchView);
    }

    private void setupSearchView(@Nullable SearchManager searchManager, ComponentName componentName) {
        if (searchManager == null) {
            binding.searchView.setVisibility(View.GONE);
            return;
        }

        SuggestionAdapter suggestionAdapter = new SuggestionAdapter(this, null, account);
        UsersAndGroupsSearchProvider provider = new UsersAndGroupsSearchProvider(this, repository);

        binding.searchView.setSuggestionsAdapter(suggestionAdapter);
        binding.searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        binding.searchView.setIconifiedByDefault(false);
        binding.searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        binding.searchView.setQueryHint(getResources().getString(R.string.note_share_activity_search_text));
        binding.searchView.setInputType(InputType.TYPE_NULL);

        View closeButton = binding.searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.searchView.setQuery("", false);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // return true to prevent the query from being processed;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                binding.progressBar.setVisibility(View.VISIBLE);

                // Cancel the previous task if it's still running
                if (future != null && !future.isDone()) {
                    future.cancel(true);
                }

                // Schedule a new task with a delay
                future = executorService.schedule(() -> {
                    if (capabilities == null) {
                        Log_OC.d(TAG, "Capabilities cannot be null");
                        return;
                    }

                    final var isFederationShareAllowed = capabilities.getFederationShare();
                    try {
                        var cursor = provider.searchForUsersOrGroups(newText, isFederationShareAllowed);

                        if (cursor == null || cursor.getCount() == 0) {
                            return;
                        }

                        runOnUiThread(() -> {
                            if (binding.searchView.getVisibility() == View.VISIBLE) {
                                suggestionAdapter.swapCursor(cursor);
                            }

                            binding.progressBar.setVisibility(View.GONE);
                        });

                    } catch (Exception e) {
                        Log_OC.d(TAG, "Exception setupSearchView.onQueryTextChange: " + e);
                        runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
                    }

                }, SEARCH_DELAY_MS, TimeUnit.MILLISECONDS);

                return false;
            }
        });

        binding.searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = suggestionAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String suggestion = cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1));
                    binding.searchView.setQuery(suggestion, false);

                    String shareWith = cursor.getString(cursor.getColumnIndexOrThrow(UsersAndGroupsSearchProvider.SHARE_WITH));
                    int shareType = cursor.getInt(cursor.getColumnIndexOrThrow(UsersAndGroupsSearchProvider.SHARE_TYPE));
                    navigateNoteShareDetail(shareWith, shareType);
                }
                return true;
            }
        });
    }

    private void navigateNoteShareDetail(String shareWith, int shareType) {
        Bundle bundle = new Bundle();

        bundle.putSerializable(NoteShareDetailActivity.ARG_NOTE, note);
        bundle.putString(NoteShareDetailActivity.ARG_SHAREE_NAME, shareWith);
        bundle.putInt(NoteShareDetailActivity.ARG_SHARE_TYPE, shareType);

        Intent intent = new Intent(this, NoteShareDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private boolean accountOwnsFile() {
        if (shares.isEmpty()) {
            return true;
        }

        final var share = shares.get(0);
        String ownerDisplayName = share.getOwnerDisplayName();
        return TextUtils.isEmpty(ownerDisplayName) || account.getAccountName().split("@")[0].equalsIgnoreCase(ownerDisplayName);
    }

    private void setShareWithYou() {
        if (accountOwnsFile()) {
            binding.sharedWithYouContainer.setVisibility(View.GONE);
        } else {
            if (shares.isEmpty()) {
                return;
            }

            final var share = shares.get(0);

            binding.sharedWithYouUsername.setText(
                    String.format(getString(R.string.note_share_activity_shared_with_you), share.getOwnerDisplayName()));
            AvatarLoader.INSTANCE.load(this, binding.sharedWithYouAvatar, account);
            binding.sharedWithYouAvatar.setVisibility(View.VISIBLE);

            String description = share.getNote();

            if (!TextUtils.isEmpty(description)) {
                binding.sharedWithYouNote.setText(description);
                binding.sharedWithYouNoteContainer.setVisibility(View.VISIBLE);
            } else {
                binding.sharedWithYouNoteContainer.setVisibility(View.GONE);
            }
        }
    }

    public void copyInternalLink() {
        if (account == null) {
            DisplayUtils.showSnackMessage(this, getString(R.string.note_share_activity_could_not_retrieve_url));
            return;
        }

        final var link = createInternalLink();
        showShareLinkDialog(link);
    }

    @Override
    public void createPublicShareLink() {
        if (capabilities == null) {
            Log_OC.d(TAG, "Capabilities cannot be null");
            return;
        }

        if (capabilities.getPublicPasswordEnforced() || capabilities.getAskForOptionalPassword()) {
            // password enforced by server, request to the user before trying to create
            requestPasswordForShareViaLink(true, capabilities.getAskForOptionalPassword());
        } else {
            executorService.submit(() -> {
                final var result = repository.addShare(note, ShareType.PUBLIC_LINK, "", "false", "", 0, "");
                runOnUiThread(() -> {
                    if (result instanceof ApiResult.Success<OcsResponse<CreateShareResponse>> successResponse) {
                        DisplayUtils.showSnackMessage(NoteShareActivity.this, successResponse.getMessage());

                        note.setIsShared(true);
                        repository.updateNote(note);
                        runOnUiThread(NoteShareActivity.this::recreate);
                    } else if (result instanceof ApiResult.Error errorResponse) {
                        DisplayUtils.showSnackMessage(NoteShareActivity.this, errorResponse.getMessage());
                    }
                });
            });
        }
    }

    public void requestPasswordForShareViaLink(boolean createShare, boolean askForPassword) {
        SharePasswordDialogFragment dialog = SharePasswordDialogFragment.newInstance(note, createShare, askForPassword, this);
        dialog.show(getSupportFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT);
    }

    // TODO:
    @Override
    public void createSecureFileDrop() {

    }

    private void showShareLinkDialog(String link) {
        Intent intentToShareLink = new Intent(Intent.ACTION_SEND);

        intentToShareLink.putExtra(Intent.EXTRA_TEXT, link);
        intentToShareLink.setType("text/plain");
        intentToShareLink.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.note_share_activity_subject_shared_with_you, note.getTitle()));

        String[] packagesToExclude = new String[]{this.getPackageName()};
        DialogFragment chooserDialog = ShareLinkToDialog.newInstance(intentToShareLink, packagesToExclude);
        chooserDialog.show(getSupportFragmentManager(), FTAG_CHOOSER_DIALOG);
    }

    private String createInternalLink() {
        Uri baseUri = Uri.parse(account.getUrl());
        return baseUri + "/index.php/f/" + note.getRemoteId();
    }

    @Override
    public void copyLink(OCShare share) {
        if (!note.isShared()) {
            return;
        }

        if (TextUtils.isEmpty(share.getShareLink())) {
            copyAndShareFileLink(share.getShareLink());
        } else {
            ClipboardUtil.copyToClipboard(this, share.getShareLink());
        }
    }

    private void copyAndShareFileLink(String link) {
        ClipboardUtil.copyToClipboard(this, link, false);
        Snackbar snackbar = Snackbar
                .make(this.findViewById(android.R.id.content), R.string.clipboard_text_copied, Snackbar.LENGTH_LONG)
                .setAction(R.string.share, v -> showShareLinkDialog(link));
        snackbar.show();
    }

    @Override
    public void showSharingMenuActionSheet(OCShare share) {
        if (!this.isFinishing()) {
            new NoteShareActivityShareItemActionBottomSheetDialog(this, this, share).show();
        }
    }

    @Override
    public void showPermissionsDialog(OCShare share) {
        new QuickSharingPermissionsBottomSheetDialog(this, this, share).show();
    }

    @Override
    public void requestPasswordForShare(OCShare share, boolean askForPassword) {
        SharePasswordDialogFragment dialog = SharePasswordDialogFragment.newInstance(share, askForPassword, this);
        dialog.show(getSupportFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT);
    }

    // TODO:
    @Override
    public void showProfileBottomSheet(Account account, String shareWith) {
    }

    public void refreshSharesFromDB() {
        executorService.submit(() -> {
            try {
                ShareeListAdapter adapter = (ShareeListAdapter) binding.sharesList.getAdapter();

                if (adapter == null) {
                    runOnUiThread(() -> DisplayUtils.showSnackMessage(NoteShareActivity.this, getString(R.string.could_not_retrieve_shares)));
                    return;
                }

                // clear adapter
                adapter.removeAll();
                shares.clear();

                // to show share with users/groups info
                if (note != null) {
                    // get shares from local DB
                    final var shareEntities = repository.getShareEntitiesForSpecificNote(note);
                    shareEntities.forEach(entity -> {
                        if (entity.getId() != null) {
                            addShares(entity.getId());
                        }
                    });

                    // get shares from remote
                    final var shares = repository.getShareFromNote(note);
                    if (shares != null) {
                        shares.forEach(entity -> addShares(entity.getId()));
                    }
                }

                runOnUiThread(() -> {
                    adapter.addShares(shares);
                    addPublicShares(adapter);
                    setShareWithYou();
                });
            } catch (Exception e) {
                Log_OC.d(TAG, "Exception while refreshSharesFromDB: " + e);
            }
        });
    }

    private void addShares(long id) {
        final var result = repository.getShares(id);
        if (result != null) {
            result.forEach(ocShare -> {
                if (!shares.contains(ocShare)) {
                    shares.add(ocShare);
                }
            });
        }
    }

    private void addPublicShares(ShareeListAdapter adapter) {
        List<OCShare> publicShares = new ArrayList<>();

        if (containsNoNewPublicShare(adapter.getShares())) {
            final OCShare ocShare = new OCShare();
            ocShare.setShareType(ShareType.NEW_PUBLIC_LINK);
            publicShares.add(ocShare);
        } else {
            adapter.removeNewPublicShare();
        }

        adapter.addShares(publicShares);
    }

    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            pickContactEmail();
        } else {
            requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void pickContactEmail() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);

        // FIXME:
        if (intent.resolveActivity(getPackageManager()) != null) {
            onContactSelectionResultLauncher.launch(intent);
        } else {
            DisplayUtils.showSnackMessage(this, getString(R.string.file_detail_sharing_fragment_no_contact_app_message));
        }
    }

    private void handleContactResult(@NonNull Uri contactUri) {
        // Define the projection to get all email addresses.
        String[] projection = {ContactsContract.CommonDataKinds.Email.ADDRESS};

        Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
        if (cursor == null) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed));
            Log_OC.e(TAG, "Failed to pick email address as Cursor is null.");
            return;
        }

        if (!cursor.moveToFirst()) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed));
            Log_OC.e(TAG, "Failed to pick email address as no Email found.");
            return;
        }

        // The contact has only one email address, use it.
        int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
        if (columnIndex == -1) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed));
            Log_OC.e(TAG, "Failed to pick email address.");
            return;
        }

        // Use the email address as needed.
        // email variable contains the selected contact's email address.
        String email = cursor.getString(columnIndex);
        binding.searchView.post(() -> {
            binding.searchView.setQuery(email, false);
            binding.searchView.requestFocus();
        });

        cursor.close();
    }

    private boolean containsNoNewPublicShare(List<OCShare> shares) {
        for (OCShare share : shares) {
            if (share.getShareType() != null && share.getShareType() == ShareType.NEW_PUBLIC_LINK) {
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
    }

    private boolean isReshareForbidden(OCShare share) {
        return (share.getShareType() != null && ShareType.FEDERATED == share.getShareType()) ||
                capabilities != null && !capabilities.isReSharingAllowed();
    }

    @VisibleForTesting
    public void search(String query) {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQuery(query, true);
    }

    @Override
    public void advancedPermissions(OCShare share) {
        modifyExistingShare(share, NoteShareDetailActivity.SCREEN_TYPE_PERMISSION);
    }


    @Override
    public void sendNewEmail(OCShare share) {
        modifyExistingShare(share, NoteShareDetailActivity.SCREEN_TYPE_NOTE);
    }

    @Override
    public void unShare(OCShare share) {
        executorService.submit(() -> {
            final var result = repository.removeShare(share, note);

            runOnUiThread(() -> {
                if (result) {
                    ShareeListAdapter adapter = (ShareeListAdapter) binding.sharesList.getAdapter();
                    if (adapter == null) {
                        DisplayUtils.showSnackMessage(NoteShareActivity.this, getString(R.string.email_pick_failed));
                        return;
                    }
                    adapter.remove(share);
                } else {
                    DisplayUtils.showSnackMessage(NoteShareActivity.this, getString(R.string.failed_the_remove_share));
                }
            });
        });
    }

    // TODO:
    @Override
    public void sendLink(OCShare share) {
    }

    @Override
    public void addAnotherLink(OCShare share) {
        createPublicShareLink();
    }

    private void modifyExistingShare(OCShare share, int screenTypePermission) {
        Bundle bundle = new Bundle();

        bundle.putSerializable(NoteShareDetailActivity.ARG_OCSHARE, share);
        bundle.putInt(NoteShareDetailActivity.ARG_SCREEN_TYPE, screenTypePermission);
        bundle.putBoolean(NoteShareDetailActivity.ARG_RESHARE_SHOWN, !isReshareForbidden(share));
        bundle.putBoolean(NoteShareDetailActivity.ARG_EXP_DATE_SHOWN, getExpDateShown());

        Intent intent = new Intent(this, NoteShareDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private boolean getExpDateShown() {
        try {
            if (capabilities == null) {
                Log_OC.d(TAG, "Capabilities cannot be null");
                return false;
            }

            final var majorVersionAsString = capabilities.getNextcloudMajorVersion();
            if (majorVersionAsString != null) {
                final var majorVersion = Integer.parseInt(majorVersionAsString);
                return majorVersion >= 18;
            }

            return false;
        } catch (NumberFormatException e) {
            Log_OC.d(TAG, "Exception while getting expDateShown");
            return false;
        }
    }

    @Override
    public void onQuickPermissionChanged(OCShare share, int permission) {
        executorService.submit(() -> {
            final var result = repository.updateSharePermission(share.getId(), permission);
            runOnUiThread(() -> {
                if (ApiResultKt.isSuccess(result)) {
                    NoteShareActivity.this.recreate();
                } else if (result instanceof ApiResult.Error error) {
                    DisplayUtils.showSnackMessage(NoteShareActivity.this, error.getMessage());
                }
            });
        });
    }

    private final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickContactEmail();
                } else {
                    BrandedSnackbar.make(binding.getRoot(), getString(R.string.note_share_activity_contact_no_permission), Snackbar.LENGTH_LONG)
                            .show();
                }
            });

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

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.androidx.themeToolbarSearchView(binding.searchView);
        util.platform.themeHorizontalProgressBar(binding.progressBar);
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }

    @Override
    public void shareFileViaPublicShare(@Nullable Note note, @Nullable String password) {
        if (note == null || password == null) {
            Log_OC.d(TAG, "note or password is null, cannot create a public share");
            return;
        }

        executorService.submit(() -> {
            final var result = repository.addShare(
                    note,
                    ShareType.PUBLIC_LINK,
                    "",
                    "false",
                    password,
                    repository.getCapabilities().getDefaultPermission(),
                    ""
            );

            runOnUiThread(() -> {
                if (ApiResultKt.isSuccess(result)) {
                    NoteShareActivity.this.recreate();
                } else if (ApiResultKt.isError(result)) {
                    ApiResult.Error error = (ApiResult.Error) result;
                    DisplayUtils.showSnackMessage(NoteShareActivity.this, error.getMessage());
                }
            });
        });
    }

    @Override
    public void setPasswordToShare(@NotNull OCShare share, @Nullable String password) {
        if (password == null) {
            Log_OC.d(TAG, "password is null, cannot update a public share");
            return;
        }

        executorService.submit(() -> {
            {
                final var requestBody = repository.getUpdateShareRequest(
                        false,
                        share,
                        "",
                        password,
                        false,
                        -1,
                        share.getPermissions()
                );
                final var result = repository.updateShare(share.getId(), requestBody);

                runOnUiThread(() -> {
                    if (ApiResultKt.isSuccess(result)) {
                        final var intent = new Intent(NoteShareActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        NoteShareActivity.this.startActivity(intent);
                    } else if (ApiResultKt.isError(result)) {
                        ApiResult.Error error = (ApiResult.Error) result;
                        DisplayUtils.showSnackMessage(NoteShareActivity.this, error.getMessage());
                    }
                });
            }
        });
    }
}
