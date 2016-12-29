package idv.kuma.app.komica.manager;

import com.facebook.CallbackManager;

/**
 * Created by TakumaLee on 2016/10/13.
 */
public class FacebookManager {

    private CallbackManager callbackManager;

    public static class SingletonHolder {
        private static FacebookManager INSTANCE = new FacebookManager();
    }

    public static FacebookManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private FacebookManager() {
        callbackManager = CallbackManager.Factory.create();
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public interface OnGetProfileListener {
        void onGetProfile();

        void onSuccess();

        void onFail();
    }

    public interface OnLoginListener {
        void onLogin();
        void onFail();
    }

    public interface OnLogoutListener {
        void onLogout();
    }

}
