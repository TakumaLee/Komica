package at.huber.youtubeExtractor;

import android.os.Parcelable;
import android.util.SparseArray;

/**
 * Created by TakumaLee on 2017/2/3.
 */

public interface OnYoutubeParseListener extends Parcelable {
    void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles);
}
