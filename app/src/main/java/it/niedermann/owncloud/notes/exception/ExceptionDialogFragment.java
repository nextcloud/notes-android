package it.niedermann.owncloud.notes.exception;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import it.niedermann.android.util.ClipboardUtil;
import it.niedermann.nextcloud.exception.ExceptionUtil;
import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogExceptionBinding;
import it.niedermann.owncloud.notes.exception.tips.TipsAdapter;

public class ExceptionDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_THROWABLES = "throwables";
    public static final String INTENT_EXTRA_BUTTON_TEXT = "button_text";

    @NonNull
    private final ArrayList<Throwable> throwables = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Bundle args = getArguments();
        if (args != null) {
            final Object throwablesArgument = args.getSerializable(KEY_THROWABLES);
            if (throwablesArgument != null) {
                throwables.addAll((ArrayList<Throwable>) throwablesArgument);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = View.inflate(getContext(), R.layout.dialog_exception, null);
        final DialogExceptionBinding binding = DialogExceptionBinding.bind(view);

        final TipsAdapter adapter = new TipsAdapter((actionIntent) -> requireActivity().startActivity(actionIntent));

        final String debugInfos = ExceptionUtil.INSTANCE.getDebugInfos(requireContext(), throwables, BuildConfig.FLAVOR);

        binding.tips.setAdapter(adapter);
        binding.statusMessage.setText(getString(R.string.error_sync, throwables.size() > 0 ? throwables.get(0).getLocalizedMessage() : getString(R.string.error_unknown)));
        binding.stacktrace.setText(debugInfos);

        adapter.setThrowables(throwables);

        return new AlertDialog.Builder(requireActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.error_dialog_title)
                .setPositiveButton(android.R.string.copy, (a, b) -> ClipboardUtil.INSTANCE.copyToClipboard(requireContext(), getString(R.string.simple_exception), "```\n" + debugInfos + "\n```"))
                .setNegativeButton(R.string.simple_close, null)
                .create();
    }

    public static DialogFragment newInstance(ArrayList<Throwable> exceptions) {
        final Bundle args = new Bundle();
        args.putSerializable(KEY_THROWABLES, exceptions);
        final DialogFragment fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DialogFragment newInstance(Throwable exception) {
        final Bundle args = new Bundle();
        final ArrayList<Throwable> list = new ArrayList<>(1);
        list.add(exception);
        args.putSerializable(KEY_THROWABLES, list);
        final DialogFragment fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
