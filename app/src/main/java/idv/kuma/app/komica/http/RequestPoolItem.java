package idv.kuma.app.komica.http;

import okhttp3.MultipartBody;
import okhttp3.Request;

/**
 * Created by TakumaLee on 2016/10/27.
 */

public class RequestPoolItem {
    private Request request;
    private MultipartBody.Builder multipartBodyBuilder = null;
    private String body = null;
    private NetworkCallback networkCallback;

    public RequestPoolItem(Request request, NetworkCallback networkCallback) {
        this.request = request;
        this.networkCallback = networkCallback;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public NetworkCallback getNetworkCallback() {
        return networkCallback;
    }

    public void setNetworkCallback(NetworkCallback networkCallback) {
        this.networkCallback = networkCallback;
    }

    public MultipartBody.Builder getMultipartBodyBuilder() {
        return multipartBodyBuilder;
    }

    public void setMultipartBodyBuilder(MultipartBody.Builder multipartBodyBuilder) {
        this.multipartBodyBuilder = multipartBodyBuilder;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
