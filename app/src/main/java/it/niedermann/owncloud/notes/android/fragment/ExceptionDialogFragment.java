package it.niedermann.owncloud.notes.android.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.niedermann.owncloud.notes.ExceptionUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogExceptionBinding;
import it.niedermann.owncloud.notes.databinding.ItemTipBinding;

import static it.niedermann.owncloud.notes.util.ClipboardUtil.copyToClipboard;

public class ExceptionDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_THROWABLES = "throwables";

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

        final TipsAdapter adapter = new TipsAdapter();

        final String debugInfos = ExceptionUtil.getDebugInfos(requireContext(), throwables);

        binding.tips.setAdapter(adapter);
        binding.statusMessage.setText(getString(R.string.error_sync, throwables.size() > 0 ? throwables.get(0).getLocalizedMessage() : getString(R.string.error_unknown)));
        binding.stacktrace.setText(debugInfos);

        for (Throwable t : throwables) {
            if (t instanceof TokenMismatchException) {
                adapter.add(R.string.error_dialog_tip_token_mismatch_retry);
                adapter.add(R.string.error_dialog_tip_token_mismatch_clear_storage);
                adapter.add(R.string.error_dialog_tip_clear_storage);
            } else if (t instanceof NextcloudFilesAppNotSupportedException) {
                adapter.add(R.string.error_dialog_tip_files_outdated);
            } else if (t instanceof NextcloudApiNotRespondingException) {
                adapter.add(R.string.error_dialog_tip_files_force_stop);
                adapter.add(R.string.error_dialog_tip_files_delete_storage);
            } else if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
                adapter.add(R.string.error_dialog_timeout_instance);
                adapter.add(R.string.error_dialog_timeout_toggle);
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

    private static class TipsAdapter extends RecyclerView.Adapter<TipsViewHolder> {

        @NonNull
        private List<Integer> tips = new LinkedList<>();

        @NonNull
        @Override
        public TipsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
            return new TipsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TipsViewHolder holder, int position) {
            holder.binding.tip.setText(tips.get(position));
        }

        @Override
        public int getItemCount() {
            return tips.size();
        }

        private void add(@StringRes int tip) {
            tips.add(tip);
            notifyItemInserted(tips.size());
        }
    }

    private static class TipsViewHolder extends RecyclerView.ViewHolder {
        private final ItemTipBinding binding;

        private TipsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTipBinding.bind(itemView);
        }
    }
}
