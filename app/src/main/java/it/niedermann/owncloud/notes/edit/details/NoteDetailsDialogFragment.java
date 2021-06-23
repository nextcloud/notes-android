package it.niedermann.owncloud.notes.edit.details;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogNoteDetailsBinding;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static it.niedermann.owncloud.notes.edit.EditNoteActivity.ACTION_SHORTCUT;
import static java.lang.Boolean.TRUE;

public class NoteDetailsDialogFragment extends DialogFragment {

    private static final String TAG = NoteDetailsDialogFragment.class.getSimpleName();
    static final String PARAM_ACCOUNT = "account";
    static final String PARAM_NOTE_ID = "noteId";
    private DialogNoteDetailsBinding binding;


    private Account account;
    private long noteId;
    private NoteDetailsViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Bundle args = requireArguments();
        account = (Account) args.getSerializable(PARAM_ACCOUNT);
        noteId = args.getLong(PARAM_NOTE_ID);
        viewModel = new ViewModelProvider(requireActivity()).get(NoteDetailsViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogNoteDetailsBinding.inflate(getLayoutInflater(), null, false);

        final Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setCancelable(true)
                .setNegativeButton(R.string.simple_close, null)
                .create();
        dialog.setOnShowListener((d) -> {
            if (!(isRequestPinShortcutSupported(requireActivity()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                binding.pin.setVisibility(View.GONE);
            }
            final ApiVersion preferredApiVersion = ApiVersionUtil.getPreferredApiVersion(account.getApiVersion());
            final boolean supportsTitle = preferredApiVersion != null && preferredApiVersion.compareTo(ApiVersion.API_VERSION_1_0) >= 0;
            if (!supportsTitle) {
                binding.titleWrapper.setVisibility(View.GONE);
            }

            viewModel.getNote$(noteId).observe(this, (note) -> {
                binding.favorite.setImageResource(note.getFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
                if (supportsTitle) {
                    binding.title.setText(note.getTitle());
                }
                binding.category.setText(note.getCategory());
                binding.modified.setText(DateUtils.getRelativeDateTimeString(
                        getContext(),
                        note.getModified().getTimeInMillis(),
                        DateUtils.SECOND_IN_MILLIS,
                        DateUtils.WEEK_IN_MILLIS,
                        0
                ));

                binding.pin.setOnClickListener((v) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        final ShortcutManager shortcutManager = requireActivity().getSystemService(ShortcutManager.class);
                        if (shortcutManager != null) {
                            if (shortcutManager.isRequestPinShortcutSupported()) {
                                final ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getActivity(), String.valueOf(note.getId()))
                                        .setShortLabel(note.getTitle())
                                        .setIcon(Icon.createWithResource(requireActivity().getApplicationContext(), TRUE.equals(note.getFavorite()) ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                        .setIntent(new Intent(getActivity(), EditNoteActivity.class).putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId()).setAction(ACTION_SHORTCUT))
                                        .build();

                                shortcutManager.requestPinShortcut(pinShortcutInfo, PendingIntent.getBroadcast(getActivity(), 0, shortcutManager.createShortcutResultIntent(pinShortcutInfo), 0).getIntentSender());
                            } else {
                                Log.i(TAG, "RequestPinShortcut is not supported");
                            }
                        } else {
                            Log.e(TAG, ShortcutManager.class.getSimpleName() + " is null");
                        }
                    }
                });
                binding.share.setOnClickListener((v) -> ShareUtil.openShareDialog(requireContext(), note.getTitle(), note.getContent()));
                binding.favorite.setOnClickListener((v) -> viewModel.toggleFavorite(noteId));
            });
        });
        return dialog;
    }

    public static DialogFragment newInstance(@NonNull Account account, long noteId) {
        final DialogFragment fragment = new NoteDetailsDialogFragment();
        final Bundle args = new Bundle();
        args.putSerializable(PARAM_ACCOUNT, account);
        args.putLong(PARAM_NOTE_ID, noteId);
        fragment.setArguments(args);
        return fragment;
    }
}
