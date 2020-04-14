package it.niedermann.owncloud.notes.android.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import it.niedermann.owncloud.notes.ExceptionUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogExceptionBinding;
import it.niedermann.owncloud.notes.databinding.ItemTipBinding;

import static it.niedermann.owncloud.notes.util.ClipboardUtil.copyToClipboard;

public class ExceptionDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_THROWABLES = "throwables";
    private static final String KEY_STATUS_MESSAGE = "statusMessage";

    private DialogExceptionBinding binding;

    private String statusMessage;
    @NonNull
    private ArrayList<Throwable> throwables = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        if (args != null) {
            Object throwablesArgument = args.getSerializable(KEY_THROWABLES);
            if (throwablesArgument != null) {
                throwables.addAll((ArrayList<Throwable>) throwablesArgument);
            }
            statusMessage = args.getString(KEY_STATUS_MESSAGE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_exception, null);
        binding = DialogExceptionBinding.bind(view);

        final String debugInfos = ExceptionUtil.getDebugInfos(requireContext(), throwables);

        binding.statusMessage.setText(statusMessage);
        binding.stacktrace.setText(debugInfos);

        for (Throwable t : throwables) {
            if (t instanceof TokenMismatchException) {
                addTip(R.string.error_dialog_tip_token_mismatch);
            } else if (t instanceof NextcloudFilesAppNotSupportedException) {
                addTip(R.string.error_dialog_tip_files_outdated);
            } else if (t instanceof NextcloudApiNotRespondingException) {
                addTip(R.string.error_dialog_tip_files_force_stop);
                addTip(R.string.error_dialog_tip_files_delete_storage);
            } else if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
                addTip(R.string.error_dialog_timeout_instance);
                addTip(R.string.error_dialog_timeout_toggle);
            } else if (t instanceof JSONException || t instanceof NullPointerException) {
                addTip(R.string.error_dialog_check_server);
            } else if (t instanceof NextcloudHttpRequestFailedException) {
                int statusCode = ((NextcloudHttpRequestFailedException) t).getStatusCode();
                switch (statusCode) {
                    case 500:
                        addTip(R.string.error_dialog_check_server_logs);
                    case 503:
                        addTip(R.string.error_dialog_check_maintenance);
                        break;
                    case 507:
                        addTip(R.string.error_dialog_insufficient_storage);
                        break;
                }
            }
        }

        return new AlertDialog.Builder(requireActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.error_dialog_title)
                .setPositiveButton(android.R.string.copy, (a, b) -> {
                    copyToClipboard(requireContext(), getString(R.string.simple_exception), "```\n" + debugInfos + "\n```");
                    a.dismiss();
                })
                .setNegativeButton(R.string.simple_close, null)
                .create();
    }

    private void addTip(@StringRes int tip) {
        final ItemTipBinding tipBinding = ItemTipBinding.inflate(getLayoutInflater());
        tipBinding.tip.setText(tip);
        binding.tips.addView(tipBinding.getRoot());
    }

    public static DialogFragment newInstance(String statusMessage, ArrayList<Throwable> exceptions) {
        final Bundle args = new Bundle();

        args.putSerializable(KEY_THROWABLES, exceptions);
        args.putString(KEY_STATUS_MESSAGE, statusMessage);

        final DialogFragment fragment = new ExceptionDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
