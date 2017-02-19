package idv.kuma.app.komica.manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.utils.KLog;


/**
 * Created by TakumaLee on 2016/9/9.
 */
public class ShareManager {
    private static final String TAG = ShareManager.class.getSimpleName();

    private MaterialDialog materialShareDialog = null;
    private ShareDialog shareDialog = null;

    public class Platform {
        public static final int OTHER = 0;
        public static final int FACEBOOK = 1;
    }

    public interface ShareEventCallback {
        void onSuccess();
        void onCancel();
        void onError();
    }

    private ShareManager() {
    }

    public static ShareManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static ShareManager INSTANCE = new ShareManager();
    }

    public MaterialDialog getShareDialog(Activity activity, ShareAdapter.OnPlatformSelectedListener onPlatformSelectedListener) {
        return getShareDialog(activity, null, onPlatformSelectedListener);
    }

    public MaterialDialog getShareDialog(Activity activity, ShareEventCallback shareEventCallback, ShareAdapter.OnPlatformSelectedListener onPlatformSelectedListener) {
        return getShareDialog(activity, null, shareEventCallback, onPlatformSelectedListener);
    }

    public MaterialDialog getShareDialog(Activity activity, String title, final ShareEventCallback shareEventCallback, ShareAdapter.OnPlatformSelectedListener onPlatformSelectedListener) {
        final ShareAdapter shareAdapter = new ShareAdapter();
        shareAdapter.setTitle(title);
        GridLayoutManager shareGLM = new GridLayoutManager(activity, 2);
        materialShareDialog = new MaterialDialog.Builder(activity)
                .title(R.string.share_with)
                .adapter(shareAdapter, shareGLM)
                .build();

        if (null == shareDialog) {
            shareDialog = new ShareDialog(activity);
        }

        shareDialog.registerCallback(FacebookManager.getInstance().getCallbackManager(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                KLog.v(TAG, "share onSuccess() " + result.getPostId());
//                Toast.makeText(ServerCallbackManager.getInstance().currentActivity, "Share Success!", Toast.LENGTH_SHORT).show();
                if (null != shareEventCallback) {
                    shareEventCallback.onSuccess();
                }
            }

            @Override
            public void onCancel() {
                KLog.v(TAG, "share onCancel()");
//                Toast.makeText(ServerCallbackManager.getInstance().currentActivity, "Share Cancel!", Toast.LENGTH_SHORT).show();
                if (null != shareEventCallback) {
                    shareEventCallback.onCancel();
                }
            }

            @Override
            public void onError(FacebookException error) {
                KLog.v(TAG, "share onError() " + error.getMessage());
//                Toast.makeText(ServerCallbackManager.getInstance().currentActivity, "Share Error!", Toast.LENGTH_SHORT).show();
                if (null != shareEventCallback) {
                    shareEventCallback.onError();
                }
            }
        });

        shareAdapter.setOnPlatformSelectedListener(onPlatformSelectedListener);

        return materialShareDialog;
    }

    public void share(final Activity activity, int platform, String imageUrl, String url, String title, String desc) {
        switch (platform) {
            case ShareManager.Platform.FACEBOOK:
                shareFacebook(activity, imageUrl, url, title, desc);
                break;
            case ShareManager.Platform.OTHER:
                shareIntentUrl(activity, url);
                break;
            default:
               activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show();
                    }
                });

                break;
        }
    }

    public void share(final Activity activity, int platform, Bitmap bitmap, Uri uri, String title, String desc) {
        switch (platform) {
            case ShareManager.Platform.FACEBOOK:
                shareFacebook(activity, bitmap, title, desc);
                break;
            case ShareManager.Platform.OTHER:
                shareIntentUrl(activity, uri);
                break;
            default:
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    private void shareFacebook(Activity activity, String imageUrl, String url, String title, String desc) {
        ShareLinkContent fbShare = new ShareLinkContent.Builder()
//                .setContentTitle("".equals(title) || title == null ? "MangaChat" : title)
                .setContentDescription("".equals(desc) || desc == null ? "share by " + activity.getString(R.string.app_name) : desc)
                .setImageUrl(Uri.parse(imageUrl))
                .setContentUrl(Uri.parse(url))
                .setShareHashtag(new ShareHashtag.Builder().setHashtag("#Komica+").build())
                .build();

        ShareDialog.show(activity, fbShare);
    }

    private void shareFacebook(Activity activity, Bitmap bitmap, String title, String desc) {
        SharePhoto sharePhoto = new SharePhoto.Builder()
//                .setImageUrl(bitmap)
                .setCaption("".equals(desc) || desc == null ? "share by " + activity.getString(R.string.app_name) : desc)
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(sharePhoto)
                .setShareHashtag(new ShareHashtag.Builder().setHashtag("#" + activity.getString(R.string.app_name)).build())
                .build();

        ShareDialog.show(activity, content);
    }

    private void shareIntentUrl(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_with)));
    }

    private void shareIntentUrl(Activity activity, String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        activity.startActivity(intent);
    }



}
