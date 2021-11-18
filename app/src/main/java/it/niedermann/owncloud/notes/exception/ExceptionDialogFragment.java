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
        final var args = getArguments();
        if (args != null) {
            final var throwablesArgument = args.getSerializable(KEY_THROWABLES);
            if (throwablesArgument instanceof Iterable<?>) {
                for (final var arg : (Iterable<?>) throwablesArgument) {
                    if (arg instanceof Throwable) {
                        throwables.add((Throwable) arg);
                    } else {
                        throw new IllegalArgumentException("Expected all " + KEY_THROWABLES + " to be instance of " + Throwable.class.getSimpleName());
                    }
                }
            } else {
                throw new IllegalArgumentException(KEY_THROWABLES + " needs to be an " + Iterable.class.getSimpleName() + "<" + Throwable.class.getSimpleName() + ">");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final var view = View.inflate(getContext(), R.layout.dialog_exception, null);
        final var binding = DialogExceptionBinding.bind(view);

        final var adapter = new TipsAdapter((actionIntent) -> requireActivity().startActivity(actionIntent));

        final String debugInfos = ExceptionUtil.INSTANCE.getDebugInfos(requireContext(), throwables, BuildConfig.FLAVOR);

        binding.tips.setAdapter(adapter);
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
        final var args = new Bundle();
        args.putSerializable(KEY_THROWABLES, exceptions);
        final var fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DialogFragment newInstance(Throwable exception) {
        final var args = new Bundle();
        final var list = new ArrayList<Throwable>(1);
        list.add(exception);
        args.putSerializable(KEY_THROWABLES, list);
        final var fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
