package it.niedermann.owncloud.notes.exception;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
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

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogExceptionBinding;
import it.niedermann.owncloud.notes.exception.tips.TipsAdapter;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static it.niedermann.owncloud.notes.shared.util.ClipboardUtil.copyToClipboard;

public class ExceptionDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_THROWABLES = "throwables";
    public static final String INTENT_EXTRA_BUTTON_TEXT = "button_text";

    @NonNull
    private ArrayList<Throwable> throwables = new ArrayList<>();

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

        final String debugInfos = ExceptionUtil.getDebugInfos(requireContext(), throwables);

        binding.tips.setAdapter(adapter);
        binding.statusMessage.setText(getString(R.string.error_sync, throwables.size() > 0 ? throwables.get(0).getLocalizedMessage() : getString(R.string.error_unknown)));
        binding.stacktrace.setText(debugInfos);

        for (Throwable t : throwables) {
            if (t instanceof TokenMismatchException) {
                adapter.add(R.string.error_dialog_tip_token_mismatch_retry);
                adapter.add(R.string.error_dialog_tip_token_mismatch_clear_storage);
                Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                        .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_deck_info);
                adapter.add(R.string.error_dialog_tip_clear_storage, intent);
                adapter.add(R.string.error_dialog_tip_clear_storage);
            } else if (t instanceof NextcloudFilesAppNotSupportedException) {
                adapter.add(R.string.error_dialog_tip_files_outdated);
            } else if (t instanceof NextcloudApiNotRespondingException) {
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    adapter.add(R.string.error_dialog_tip_disable_battery_optimizations, new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_battery_settings));
                } else {
                    adapter.add(R.string.error_dialog_tip_disable_battery_optimizations);
                }
                adapter.add(R.string.error_dialog_tip_files_force_stop);
                adapter.add(R.string.error_dialog_tip_files_delete_storage);
            } else if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
                adapter.add(R.string.error_dialog_timeout_instance);
                adapter.add(R.string.error_dialog_timeout_toggle, new Intent(Settings.ACTION_WIFI_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_network));
            } else if (t instanceof JSONException || t instanceof NullPointerException) {
                adapter.add(R.string.error_dialog_check_server);
            } else if (t instanceof NextcloudHttpRequestFailedException) {
                int statusCode = ((NextcloudHttpRequestFailedException) t).getStatusCode();
                switch (statusCode) {
                    case 302:
                        adapter.add(R.string.error_dialog_server_app_enabled);
                        adapter.add(R.string.error_dialog_redirect);
                        break;
                    case 500:
                        adapter.add(R.string.error_dialog_check_server_logs);
                        break;
                    case 503:
                        adapter.add(R.string.error_dialog_check_maintenance);
                        break;
                    case 507:
                        adapter.add(R.string.error_dialog_insufficient_storage);
                        break;
                }
            }
        }

        return new AlertDialog.Builder(requireActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.error_dialog_title)
                .setPositiveButton(android.R.string.copy, (a, b) -> copyToClipboard(requireContext(), getString(R.string.simple_exception), "```\n" + debugInfos + "\n```"))
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
