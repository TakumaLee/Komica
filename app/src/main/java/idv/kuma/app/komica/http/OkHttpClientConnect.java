package idv.kuma.app.komica.http;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Created by TakumaLee on 15/6/22.
 */
public class OkHttpClientConnect {
    private static final String TAG = OkHttpClientConnect.class.getSimpleName();

    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=utf-8";

    static ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
            .build();
    private static ConnectionPool threadPoolExecutor = new ConnectionPool(5, 15000, TimeUnit.MILLISECONDS);

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectionPool(threadPoolExecutor).build();

    public static RequestPoolItem excuteAutoGet(String url) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        return new RequestPoolItem(url.contains("https") ? excuteSSLGet(url) : excuteGet(url, null), null);
    }

    public static RequestPoolItem excuteAutoGet(String url, NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        return new RequestPoolItem(url.contains("https") ? excuteSSLGet(url, callback) : excuteGet(url, callback), callback);
    }

    public static RequestPoolItem excuteAutoGet(String url, String contentType, NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        return new RequestPoolItem(url.contains("https") ? excuteSSLGet(url, callback) : excuteGet(url, contentType, callback), callback);
    }

    public static RequestPoolItem excuteAutoPost(String url, String json, @Nullable NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        RequestPoolItem item = new RequestPoolItem(url.contains("https") ? excuteSSLPost(url, json, CONTENT_TYPE_JSON, callback)
                : excutePost(url, json, CONTENT_TYPE_JSON, callback), callback);
        item.setBody(json);
        return item;
    }

    public static RequestPoolItem excuteAutoPost(String url, String json, String contentType, @Nullable NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        RequestPoolItem item = new RequestPoolItem(url.contains("https") ? excuteSSLPost(url, json, contentType, callback)
                : excutePost(url, json, contentType, callback), callback);
        item.setBody(json);
        return item;
    }

    public static RequestPoolItem excuteAutoPost(String header, String headerValue, String url, String json, String contentType, @Nullable NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        RequestPoolItem item = new RequestPoolItem(url.contains("https") ? excuteSSLPost(header, headerValue, url, json, contentType, callback)
                : excutePost(header, headerValue, url, json, contentType, callback), callback);
        item.setBody(json);
        return item;
    }

    public static RequestPoolItem excuteAutoMultiPartRequest(String url, MultipartBody.Builder bodyBuilder, NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        RequestPoolItem item = excuteAutoMultiPartRequest(url, bodyBuilder.build(), callback);
        item.setMultipartBodyBuilder(bodyBuilder);
        return item;
    }

    private static RequestPoolItem excuteAutoMultiPartRequest(String url, RequestBody body, NetworkCallback callback) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        return new RequestPoolItem(url.contains("https") ? excuteSSLMultiPartRequest(url, body, callback)
                : excuteMultiPartRequest(url, body, callback), callback);
    }

    private static Request excuteSSLPost(String url, String json) {
        initSSL();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(null);
        return request;
    }

    private static Request excuteSSLPost(String url, String json, String contentType, final NetworkCallback callback) {
        initSSL();
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", contentType)
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
        return request;
    }

    private static Request excutePost(String url, String json, String contentType, final NetworkCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", contentType)
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
        return request;
    }

    private static Request excuteSSLPost(String header, String headerValue, String url, String json, String contentType, final NetworkCallback callback) {
        initSSL();
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader(header, headerValue)
                .addHeader("Content-Type", contentType)
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
        return request;
    }

    private static Request excutePost(String header, String headerValue, String url, String json, String contentType, final NetworkCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader(header, headerValue)
                .addHeader("Content-Type", contentType)
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
        return request;
    }

    private static Request excuteGet(String url, final NetworkCallback callback) {
        return excuteGet(url, CONTENT_TYPE_JSON, callback);
    }

    private static Request excuteGet(String url, String contentType, final NetworkCallback callback) {
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("Content-Type", contentType)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36")
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
        return request;
    }

    private static Request excuteSSLGet(String url) {
        initSSL();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();
        okHttpClient.newCall(request).enqueue(null);
        return request;
    }

    private static Request excuteSSLGet(String url, final NetworkCallback callback) {
        return excuteSSLGet(url, CONTENT_TYPE_JSON, callback);
    }

    private static Request excuteSSLGet(String url, String contentType, final NetworkCallback callback) {
        initSSL();
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("Content-Type", contentType)
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
        return request;
    }

    private static Request excuteMultiPartRequest(String url, RequestBody requestBody, final NetworkCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(requestBody)
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
        return request;

    }

    private static Request excuteSSLMultiPartRequest(String url, RequestBody requestBody, final NetworkCallback callback) {
        initSSL();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(requestBody)
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
        return request;
    }

    public static Request excuteRequest(Request request, final NetworkCallback callback) {
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
        return request;
    }

    public static void initSSL(OkHttpClient okHttpClient) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
//                    Log.v(TAG, "SSL verify: " + session.getCipherSuite());
//                    if (hostname.equals("dramarket.chocolabs.com") && session.getCipherSuite().equals(spec.cipherSuites().get(1).name())) {
                    return true;
//                    }
//                    return false;
                }
            });

            okHttpClient = builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void initSSL() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // FIXME: 2016/12/23 this is not secure, please find time to fix.
                    return true;
                }
            });

            okHttpClient = builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
