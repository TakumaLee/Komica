package idv.kuma.app.komica.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.util.SparseArray;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import at.huber.youtubeExtractor.OnYoutubeParseListener;
import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;
import idv.kuma.app.komica.R;
import idv.kuma.app.komica.utils.KLog;

/**
 * Created by TakumaLee on 2017/2/3.
 */

public class YoutubeManager {
    private static final String TAG = YoutubeManager.class.getSimpleName();

    private static final String YOUTUBE_URL = "youtube_url";
    private static final String YOUTUBE_INTERFACE = "youtube_interface";

    private Context context;
    private static YoutubeManager INSTANCE = null;
    private YouTubeUriExtractor youTubeUriExtractor = null;

    private HandlerThread handlerThread = new HandlerThread(TAG);
    private Handler handler;
    private Handler mainHandler;

    private MaterialDialog progressDialog;

    public synchronized static YoutubeManager getInstance() {
        return INSTANCE;
    }

    public YoutubeManager(Context context) {
        this.context = context;
        youTubeUriExtractor = new YouTubeUriExtractor(context);
        handlerThread.start();
        mainHandler = new Handler(Looper.getMainLooper());
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                dismissProgressDialog();
                if (msg == null || msg.getData() == null) {
                    return;
                }
                String url = msg.getData().getString(YOUTUBE_URL);
                OnYoutubeParseListener onYoutubeParseListener = msg.getData().getParcelable(YOUTUBE_INTERFACE);
                youTubeUriExtractor.parseInBackground(url, onYoutubeParseListener);
            }
        };
    }

    public static void initialize(Context context) {
        INSTANCE = new YoutubeManager(context);
    }

    public void startParseYoutubeUrl(Activity activity, String url, OnYoutubeParseListener onYoutubeParseListener) {
        progressDialog = new MaterialDialog.Builder(activity)
                .content(R.string.com_facebook_loading)
                .progress(true, 0)
                .build();
        progressDialog.show();
        startParseYoutubeUrl(url, onYoutubeParseListener);
    }

    public void startParseYoutubeUrl(String url, OnYoutubeParseListener onYoutubeParseListener) {
        if (handlerThread.isInterrupted()) {
            handlerThread.start();
        }
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(YOUTUBE_URL, url);
        bundle.putParcelable(YOUTUBE_INTERFACE, onYoutubeParseListener);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public void playYoutube(final Context context, String url) {
        YoutubeManager.getInstance().startParseYoutubeUrl((Activity) context, url, new OnYoutubeParseListener() {
            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }

            @Override
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                KLog.v(TAG, "onYoutube id: " + videoId);
                KLog.v(TAG, "onYoutube title: " + videoTitle);
                startPlayer(context, videoTitle, ytFiles);
            }
        });
    }

    private void startPlayer(Context context, String videoTitle, SparseArray<YtFile> ytFiles) {
        if (ytFiles == null) {
            Toast.makeText(context, "此影片目前無法觀看", Toast.LENGTH_LONG).show();
            return;
        }
//                            KLog.v(TAG, "onYoutube files: " + ytFiles.size() + "_" + ytFiles.get(22).getUrl());
        int index = ytFiles.size() > 1 ? 1 : 0;
        KomicaManager.getInstance().startPlayerActivity(context, videoTitle, ytFiles.get(ytFiles.keyAt(index)).getUrl());
    }

    public void dismissProgressDialog() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != progressDialog) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
