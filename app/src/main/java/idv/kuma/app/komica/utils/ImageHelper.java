package idv.kuma.app.komica.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by TakumaLee on 2017/1/1.
 */

public class ImageHelper {
    public static void shareIntentUrl(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "分享至"));
    }
}
