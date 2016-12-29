package idv.kuma.app.komica.http;

import java.io.IOException;

/**
 * Created by TakumaLee on 2016/3/24.
 */
public interface NetworkCallback {
    void onFailure(IOException e);
    void onResponse(int responseCode, final String result);
}
