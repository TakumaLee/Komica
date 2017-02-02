package idv.kuma.app.komica.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import at.huber.youtubeExtractor.OnYoutubeParseListener;
import at.huber.youtubeExtractor.YouTubeUriExtractor;

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

    public synchronized static YoutubeManager getInstance() {
        return INSTANCE;
    }

    public YoutubeManager(Context context) {
        this.context = context;
        youTubeUriExtractor = new YouTubeUriExtractor(context);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
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
}
