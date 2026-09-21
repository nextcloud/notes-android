package it.niedermann.owncloud.notes.shared.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;

public class NoteImageHelper {
    private static final String TAG = "NoteImageHelper";

    /**
     * Resolves the notes folder path on the Nextcloud server (defaults to "Notes").
     */
    @NonNull
    public static String getNotesPath(@NonNull Context context, @NonNull SingleSignOnAccount ssoAccount, @Nullable NotesRepository repo) {
        if (repo == null) {
            repo = NotesRepository.getInstance(context.getApplicationContext());
        }
        try {
            final var call = repo.getServerSettings(ssoAccount, ApiVersion.API_VERSION_1_0);
            final var response = call.execute();
            final var settings = response.body();
            if (settings != null && settings.getNotesPath() != null && !settings.getNotesPath().isEmpty()) {
                return settings.getNotesPath();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to query server settings for notes path, falling back to 'Notes'", e);
        }
        return "Notes";
    }

    /**
     * Downsamples, compresses, and uploads an image to the Nextcloud WebDAV attachments folder.
     * Automatically resolves the notes path.
     *
     * @return The relative attachment path, e.g. "attachments/abc-123.jpg"
     */
    @NonNull
    public static String uploadImage(@NonNull Context context,
                                     @NonNull SingleSignOnAccount ssoAccount,
                                     @NonNull Uri imageUri) throws Exception {
        String notesPath = getNotesPath(context, ssoAccount, null);
        return uploadImage(context, ssoAccount, notesPath, imageUri);
    }

    /**
     * Downsamples, compresses, and uploads an image to the Nextcloud WebDAV attachments folder.
     *
     * @return The relative attachment path, e.g. "attachments/abc-123.jpg"
     */
    @NonNull
    public static String uploadImage(@NonNull Context context,
                                     @NonNull SingleSignOnAccount ssoAccount,
                                     @NonNull String notesPath,
                                     @NonNull Uri imageUri) throws Exception {
        final ContentResolver cr = context.getContentResolver();

        // 1. Determine format
        String mimeType = cr.getType(imageUri);
        boolean isPng = mimeType != null && mimeType.contains("png");
        String extension = isPng ? ".png" : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // 2. Safely decode bounds
        BitmapFactory.Options boundsOpts = new BitmapFactory.Options();
        boundsOpts.inJustDecodeBounds = true;
        try (InputStream is = cr.openInputStream(imageUri)) {
            if (is == null) throw new IOException("Cannot open input stream for " + imageUri);
            BitmapFactory.decodeStream(is, null, boundsOpts);
        }

        // 3. Compute sample size (target max 1920px)
        int sampleSize = 1;
        int maxDim = 1920;
        while (boundsOpts.outWidth / sampleSize / 2 >= maxDim || boundsOpts.outHeight / sampleSize / 2 >= maxDim) {
            sampleSize *= 2;
        }

        // 4. Decode bitmap
        BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
        decodeOpts.inSampleSize = sampleSize;
        Bitmap bm;
        try (InputStream is = cr.openInputStream(imageUri)) {
            if (is == null) throw new IOException("Cannot open input stream for " + imageUri);
            bm = BitmapFactory.decodeStream(is, null, decodeOpts);
        }
        if (bm == null) throw new IOException("Failed to decode image bitmap");

        // 5. Scale down if still wider than maxDim
        if (bm.getWidth() > maxDim) {
            float ratio = (float) maxDim / bm.getWidth();
            int targetHeight = Math.round(bm.getHeight() * ratio);
            bm = Bitmap.createScaledBitmap(bm, maxDim, targetHeight, true);
        }

        // 6. Compress to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = isPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        bm.compress(format, 85, baos);
        byte[] imageBytes = baos.toByteArray();
        bm.recycle();

        // 7. Get Nextcloud SSO API
        NextcloudAPI api = ApiProvider.getInstance().getNextcloudAPI(context, ssoAccount);

        // Ensure attachments directory exists (MKCOL)
        String attachmentsUrl = "/remote.php/webdav/" + notesPath + "/attachments";
        try {
            NextcloudRequest mkcol = new NextcloudRequest.Builder()
                    .setMethod("MKCOL")
                    .setUrl(attachmentsUrl)
                    .build();
            api.performNetworkRequestV2(mkcol);
            Log.d(TAG, "Created attachments folder: " + attachmentsUrl);
        } catch (Exception e) {
            // Folder likely already exists
            Log.d(TAG, "MKCOL returned (likely exists): " + e.getMessage());
        }

        // Upload the file (PUT)
        String fileUrl = attachmentsUrl + "/" + filename;
        NextcloudRequest put = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(fileUrl)
                .setRequestBodyAsStream(new ByteArrayInputStream(imageBytes))
                .build();
        api.performNetworkRequestV2(put);
        Log.i(TAG, "Uploaded image successfully: " + fileUrl + " (" + imageBytes.length + " bytes)");

        return "attachments/" + filename;
    }

    /**
     * Formats a Markdown image tag from an attachment path.
     */
    @NonNull
    public static String formatMarkdownImage(@NonNull String attachmentPath) {
        String filename = attachmentPath.contains("/")
                ? attachmentPath.substring(attachmentPath.lastIndexOf('/') + 1)
                : attachmentPath;
        return "\n\n![" + filename + "](" + attachmentPath + ")\n\n";
    }
}
