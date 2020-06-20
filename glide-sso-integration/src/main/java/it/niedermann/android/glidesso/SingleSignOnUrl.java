package it.niedermann.android.glidesso;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.net.URL;
import java.util.Map;

import static it.niedermann.android.glidesso.SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME;

/**
 * Use this as kind of {@link GlideUrl} if you want to do a {@link Glide} request from a {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper#setCurrentAccount(Context, String)}.
 */
public class SingleSignOnUrl extends GlideUrl {

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull String url) {
        this(ssoAccount.name, url);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull URL url) {
        this(ssoAccount.name, url);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull String url, @NonNull Headers headers) {
        this(ssoAccount.name, url, headers);
    }

    public SingleSignOnUrl(@NonNull SingleSignOnAccount ssoAccount, @NonNull URL url, @NonNull Headers headers) {
        this(ssoAccount.name, url, headers);
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull String url) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull URL url) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull String url, @NonNull Headers headers) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName, headers));
    }

    public SingleSignOnUrl(@NonNull String ssoAccountName, @NonNull URL url, @NonNull Headers headers) {
        super(url, new SingleSignOnOriginHeader(ssoAccountName, headers));
    }

    private static class SingleSignOnOriginHeader implements Headers {

        private Headers headers;

        /**
         * Use this as {@link Headers} if you want to do a {@link Glide} request for an {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper} as current {@link SingleSignOnAccount}.
         *
         * @param ssoAccountName Account name from which host the request should be fired (needs to match {@link SingleSignOnAccount#name})
         */
        public SingleSignOnOriginHeader(@NonNull String ssoAccountName) {
            this.headers = new LazyHeaders.Builder().addHeader(X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build();
        }

        /**
         * Use this as {@link Headers} if you want to do a {@link Glide} request for an {@link SingleSignOnAccount} which is not set by {@link SingleAccountHelper} as current {@link SingleSignOnAccount}.
         *
         * @param ssoAccountName Account name from which host the request should be fired (needs to match {@link SingleSignOnAccount#name})
         */
        public SingleSignOnOriginHeader(@NonNull String ssoAccountName, Headers headers) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();
            for (Map.Entry<String, String> entry : headers.getHeaders().entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            builder.addHeader(X_HEADER_SSO_ACCOUNT_NAME, ssoAccountName).build();
            this.headers = builder.build();
        }

        @Override
        public Map<String, String> getHeaders() {
            return this.headers.getHeaders();
        }
    }
}
