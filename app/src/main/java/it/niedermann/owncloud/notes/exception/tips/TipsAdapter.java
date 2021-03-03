package it.niedermann.owncloud.notes.exception.tips;

import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.R;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static it.niedermann.owncloud.notes.exception.ExceptionDialogFragment.INTENT_EXTRA_BUTTON_TEXT;

public class TipsAdapter extends RecyclerView.Adapter<TipsViewHolder> {

    @NonNull
    private final Consumer<Intent> actionButtonClickedListener;
    @NonNull
    private final List<TipsModel> tips = new LinkedList<>();

    public TipsAdapter(@NonNull Consumer<Intent> actionButtonClickedListener) {
        this.actionButtonClickedListener = actionButtonClickedListener;
    }

    @NonNull
    @Override
    public TipsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new TipsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsViewHolder holder, int position) {
        holder.bind(tips.get(position), actionButtonClickedListener);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    public void setThrowables(@NonNull List<Throwable> throwables) {
        for (Throwable t : throwables) {
            if (t instanceof TokenMismatchException) {
                add(R.string.error_dialog_tip_token_mismatch_retry);
                add(R.string.error_dialog_tip_token_mismatch_clear_storage);
                Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                        .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_deck_info);
                add(R.string.error_dialog_tip_clear_storage, intent);
            } else if (t instanceof NextcloudFilesAppNotSupportedException) {
                add(R.string.error_dialog_tip_files_outdated);
            } else if (t instanceof NextcloudApiNotRespondingException) {
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    add(R.string.error_dialog_tip_disable_battery_optimizations, new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_battery_settings));
                } else {
                    add(R.string.error_dialog_tip_disable_battery_optimizations);
                }
                add(R.string.error_dialog_tip_files_force_stop);
                add(R.string.error_dialog_tip_files_delete_storage);
                Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                        .putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_deck_info);
                add(R.string.error_dialog_tip_clear_storage, intent);
            } else if (t instanceof SocketTimeoutException || t instanceof ConnectException) {
                add(R.string.error_dialog_timeout_instance);
                add(R.string.error_dialog_timeout_toggle, new Intent(Settings.ACTION_WIFI_SETTINGS).putExtra(INTENT_EXTRA_BUTTON_TEXT, R.string.error_action_open_network));
            } else if (t instanceof JSONException || t instanceof NullPointerException) {
                add(R.string.error_dialog_check_server);
            } else if (t instanceof NextcloudHttpRequestFailedException) {
                int statusCode = ((NextcloudHttpRequestFailedException) t).getStatusCode();
                switch (statusCode) {
                    case 302:
                        add(R.string.error_dialog_server_app_enabled);
                        add(R.string.error_dialog_redirect);
                        break;
                    case 500:
                        add(R.string.error_dialog_check_server_logs);
                        break;
                    case 503:
                        add(R.string.error_dialog_check_maintenance);
                        break;
                    case 507:
                        add(R.string.error_dialog_insufficient_storage);
                        break;
                }
            }
        }
        notifyDataSetChanged();
    }

    private void add(@StringRes int text) {
        add(text, null);
    }

    private void add(@StringRes int text, @Nullable Intent primaryAction) {
        tips.add(new TipsModel(text, primaryAction));
    }
}