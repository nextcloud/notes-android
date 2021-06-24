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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.stream.Collectors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.DialogNoteDetailsBinding;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static it.niedermann.owncloud.notes.edit.EditNoteActivity.ACTION_SHORTCUT;
import static java.lang.Boolean.TRUE;

public class NoteDetailsDialogFragment extends DialogFragment {

    private static final String TAG = NoteDetailsDialogFragment.class.getSimpleName();
    private static final String PARAM_ACCOUNT = "account";
    private static final String PARAM_NOTE_ID = "noteId";

    private DialogNoteDetailsBinding binding;
    private NoteDetailsViewModel viewModel;
    private CategoryViewModel categoryViewModel;

    private NoteDetailsListener listener;

    private Account account;
    private long noteId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Bundle args = requireArguments();
        account = (Account) args.getSerializable(PARAM_ACCOUNT);
        noteId = args.getLong(PARAM_NOTE_ID);

        if (getTargetFragment() instanceof NoteDetailsListener) {
            listener = (NoteDetailsListener) getTargetFragment();
        } else if (getActivity() instanceof NoteDetailsListener) {
            listener = (NoteDetailsListener) getActivity();
        } else {
            throw new IllegalArgumentException("Calling activity or target fragment must implement " + NoteDetailsListener.class.getSimpleName());
        }

        viewModel = new ViewModelProvider(requireActivity()).get(NoteDetailsViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogNoteDetailsBinding.inflate(getLayoutInflater(), null, false);

        final Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, (d, w) -> {
                    listener.onNoteDetailsEdited(binding.title.getText().toString(), binding.category.getText().toString());
                    viewModel.commit(account, noteId, binding.title.getText().toString(), binding.category.getText().toString());
                })
                .setNeutralButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener((d) -> {
            BrandingUtil.applyBrandToEditTextInputLayout(account.getColor(), binding.titleWrapper);
            BrandingUtil.applyBrandToEditTextInputLayout(account.getColor(), binding.categoryWrapper);
            if (!isRequestPinShortcutSupported(requireContext()) || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                binding.pin.setVisibility(View.GONE);
            }
            final ApiVersion preferredApiVersion = ApiVersionUtil.getPreferredApiVersion(account.getApiVersion());
            final boolean supportsTitle = preferredApiVersion != null && preferredApiVersion.compareTo(ApiVersion.API_VERSION_1_0) >= 0;
            if (!supportsTitle) {
                binding.titleWrapper.setVisibility(View.GONE);
            }
            binding.category.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Nothing to do here...
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    categoryViewModel.postSearchTerm(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Nothing to do here...
                }
            });

            final CategoryAdapter adapter = new CategoryAdapter(requireContext(), category -> binding.category.setText(category));
            binding.categories.setAdapter(adapter);

            viewModel.isFavorite$(noteId).observe(this, (isFavorite) -> binding.favorite.setImageResource(isFavorite ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp));
            viewModel.getCategory$(noteId).observe(this, (category) -> {
                if (!TextUtils.equals(binding.category.getText(), category)) {
                    binding.category.setText(category);
                }
                categoryViewModel.postSearchTerm(category);
            });
            categoryViewModel.getCategories(account.getId()).observe(this, categories -> adapter.setCategoryList(categories.stream().filter(category -> !TextUtils.equals(category.category, binding.category.getText())).collect(Collectors.toList())));
            viewModel.getTitle$(noteId).observe(this, (title) -> {
                if (!TextUtils.equals(binding.title.getText(), title)) {
                    binding.title.setText(title);
                }
            });
            viewModel.getModified$(noteId).observe(this, (modified) -> binding.modified.setText(DateUtils.getRelativeDateTimeString(
                    getContext(),
                    modified.getTimeInMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0
            )));
            binding.pin.setOnClickListener((v) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    final ShortcutManager shortcutManager = requireActivity().getSystemService(ShortcutManager.class);
                    if (shortcutManager != null) {
                        if (shortcutManager.isRequestPinShortcutSupported()) {
                            viewModel.getNoteById(noteId, new IResponseCallback<Note>() {
                                @Override
                                public void onSuccess(Note note) {
                                    final ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getActivity(), String.valueOf(note.getId()))
                                            .setShortLabel(note.getTitle())
                                            .setIcon(Icon.createWithResource(requireActivity().getApplicationContext(), TRUE.equals(note.getFavorite()) ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                            .setIntent(new Intent(getActivity(), EditNoteActivity.class).putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId()).setAction(ACTION_SHORTCUT))
                                            .build();
                                    shortcutManager.requestPinShortcut(pinShortcutInfo, PendingIntent.getBroadcast(getActivity(), 0, shortcutManager.createShortcutResultIntent(pinShortcutInfo), 0).getIntentSender());
                                }

                                @Override
                                public void onError(@NonNull Throwable t) {
                                    requireActivity().runOnUiThread(NoteDetailsDialogFragment.this::dismiss);
                                }
                            });
                        } else {
                            Log.i(TAG, "RequestPinShortcut is not supported");
                        }
                    } else {
                        Log.e(TAG, ShortcutManager.class.getSimpleName() + " is null");
                    }
                }
            });
            binding.share.setOnClickListener((v) -> {
                viewModel.getNoteById(noteId, new IResponseCallback<Note>() {
                    @Override
                    public void onSuccess(Note note) {
                        ShareUtil.openShareDialog(requireContext(), note.getTitle(), note.getContent());
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        requireActivity().runOnUiThread(NoteDetailsDialogFragment.this::dismiss);
                    }
                });
            });
            binding.favorite.setOnClickListener((v) -> viewModel.toggleFavorite(account, noteId));
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

    public interface NoteDetailsListener {
        void onNoteDetailsEdited(String title, String category);
    }
}
