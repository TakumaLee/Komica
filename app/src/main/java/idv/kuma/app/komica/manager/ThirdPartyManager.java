package idv.kuma.app.komica.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import idv.kuma.app.komica.entity.MyAccount;
import idv.kuma.app.komica.manager.platform.facebook.Attributes;
import idv.kuma.app.komica.manager.platform.facebook.FacebookPermission;
import idv.kuma.app.komica.manager.platform.facebook.PictureAttributes;
import idv.kuma.app.komica.utils.AppTools;
import idv.kuma.app.komica.utils.KLog;


/**
 * Created by TakumaLee on 2016/6/2.
 */
public class ThirdPartyManager {
    private static final String TAG = ThirdPartyManager.class.getSimpleName();

    public ThirdPartyManager() {
        onGetProfileListeners = new ArrayList<>();
        onLogoutListeners = new ArrayList<>();
        registerFacebookCallback();
    }

    public static ThirdPartyManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private List<FacebookManager.OnGetProfileListener> onGetProfileListeners;
    private List<FacebookManager.OnLogoutListener> onLogoutListeners;

    private static class SingletonHolder {
        private static ThirdPartyManager INSTANCE = new ThirdPartyManager();
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        FacebookManager.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    private void registerFacebookCallback() {
        LoginManager.getInstance().registerCallback(FacebookManager.getInstance().getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fetchProfile();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    public void registerProfileListener(FacebookManager.OnGetProfileListener listener) {
        onGetProfileListeners.add(listener);
    }

    public void unRegisterProfileListener(FacebookManager.OnGetProfileListener listener) {
        onGetProfileListeners.remove(listener);
    }

    public void registerLogoutListener(FacebookManager.OnLogoutListener listener) {
        onLogoutListeners.add(listener);
    }

    public void unRegisterLogoutListener(FacebookManager.OnLogoutListener listener) {
        onLogoutListeners.remove(listener);
    }

    protected void logout() {
        LoginManager.getInstance().logOut();
        for (FacebookManager.OnLogoutListener listener : onLogoutListeners) {
            listener.onLogout();
        }
    }

    private void fetchProfile() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        if (object == null) {
                            return;
                        }
                        KLog.v(TAG, object + "\n" + response);
                        MyAccount myAccount = KomicaAccountManager.getInstance().getMyAccount();
                        try {
                            myAccount.setEmail(object.optString("email"));
                            if (object.has("id")) {
                                myAccount.setFbId(object.getString("id"));
                            }
                            if (object.has("name")) {
                                myAccount.setUsername(object.getString("name"));
                            }
                            if (object.has("picture")) {
                                myAccount.setHeaderPic(object.getJSONObject("picture").getJSONObject("data").getString("url"));
                            }
                            if (object.has("cover")) {
                                myAccount.setCoverPic(object.getJSONObject("cover").getString("source"));
                            }
                            if (object.has("birthday")) {
                                myAccount.setBirthday(object.getString("birthday"));
                            }
                            if (object.has("gender")) {
                                myAccount.setGenderStr(object.getString("gender"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            KomicaAccountManager.getInstance().setMyAccount(myAccount);
                            KomicaAccountManager.getInstance().savedMyAccout();
                            if (null != onGetProfileListeners) {
                                for (FacebookManager.OnGetProfileListener listener : onGetProfileListeners) {
                                    listener.onGetProfile();
                                }
                            }
                        }
                    }
                });
        Bundle parameters = new Bundle();
        Iterator<String> iterator = getProperties().iterator();
        String fields = AppTools.join(iterator, ",");
        parameters.putString("fields", fields);
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static Collection<String> permissions = Arrays.asList(
//            FacebookPermission.ID,
            FacebookPermission.PUBLIC_PROFILE,
            FacebookPermission.EMAIL,
            FacebookPermission.USER_FRIENDS,
            FacebookPermission.USER_BIRTHDAY
//            FacebookPermission.PUBLISH_ACTIONS,
//            FacebookPermission.USER_ABOUT_ME,
//            FacebookPermission.USER_STATUS
//            FacebookPermission.USER_PHOTOS,
    );

    public static Set<String> getProperties() {
        Set<String> properties = new HashSet<String>();
        properties.add(FacebookPermission.ID);
        properties.add(FacebookPermission.NAME);
        properties.add(FacebookPermission.COVER);
        properties.add(FacebookPermission.EMAIL);
        properties.add("birthday");
        properties.add(FacebookPermission.GENDER);

        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(500);
        pictureAttributes.setWidth(500);
        pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);
        Map<String, String> map = pictureAttributes.getAttributes();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FacebookPermission.PICTURE);
        stringBuilder.append('.');
        stringBuilder.append(AppTools.join(map, '.', '(', ')'));
        properties.add(stringBuilder.toString());
        return properties;
    }

    public synchronized void loginFacebook(Activity activity) {
        if (isFacebookLogin()) {
            fetchProfile();
        } else {
            LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
        }
    }

    public synchronized void loginFacebook(Fragment fragment) {
        if (isFacebookLogin()) {
            fetchProfile();
        } else {
            LoginManager.getInstance().logInWithReadPermissions(fragment, permissions);
        }
    }

    public void logoutFacebook() {
        LoginManager.getInstance().logOut();
    }

    public boolean isFacebookLogin() {
        return AccessToken.getCurrentAccessToken() != null;
    }

}
