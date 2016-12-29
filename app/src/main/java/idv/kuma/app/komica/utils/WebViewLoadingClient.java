package idv.kuma.app.komica.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewLoadingClient extends WebViewClient {
    private static final String TAG = WebViewLoadingClient.class.getSimpleName();

    ProgressBar progressBar;

    public WebViewLoadingClient(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        KLog.v(TAG, "onPageStarted");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Uri.parse(url).getScheme().equals("market")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                Activity host = (Activity) view.getContext();
                host.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                // Google Play app is not installed, you may want to open the app store link
                Uri uri = Uri.parse(url);
                view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery());
                return false;
            } finally {
                ((Activity) view.getContext()).finish();
            }

        }
        return false;

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        KLog.v(TAG, "onPageFinished");
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        KLog.v(TAG, "onLoadResource");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        progressBar.setVisibility(View.GONE);
    }

}
