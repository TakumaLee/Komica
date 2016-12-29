package idv.kuma.library.appversionmanager;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Created by TakumaLee on 15/6/22.
 */
public class OkHttpClientConnect {

    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
            .build();
    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(spec)).build();

    public void excuteSSLPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();
        okHttpClient.newCall(request).execute();
    }

    public void excuteSSLPost(String url, String json, final NetworkCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response.code(), response.body().string());
            }
        });
    }

    public void excutePost(String url, String json, final NetworkCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response.code(), response.body().string());
            }
        });
    }

    public void excutePost(String header, String headerValue, String url, String json, final NetworkCallback callback) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader(header, headerValue)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response.code(), response.body().string());
            }
        });
    }

    public void excuteGet(String url, final NetworkCallback callback) {
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();
        //okHttpClient.newCall(request).enqueue(callback);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null || !response.isSuccessful()) {
                    return;
                }
                callback.onResponse(response.code(), response.body().string());
            }
        });
    }

    public void excuteSSLGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();
        okHttpClient.newCall(request).execute();
    }

    public void excuteSSLGet(String url, final NetworkCallback callback) {
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();
        //okHttpClient.newCall(request).enqueue(callback);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response.code(), response.body().string());
            }
        });
    }

    public void cancelRequest() {
        okHttpClient.dispatcher().cancelAll();
    }

}
