package idv.kuma.app.komica.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.afollestad.materialdialogs.MaterialDialog;

import at.huber.youtubeExtractor.OnYoutubeParseListener;
import at.huber.youtubeExtractor.YouTubeUriExtractor;
import idv.kuma.app.komica.R;

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
