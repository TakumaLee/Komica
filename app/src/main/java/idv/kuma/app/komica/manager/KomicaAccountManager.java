package idv.kuma.app.komica.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import idv.kuma.app.komica.BuildConfig;
import idv.kuma.app.komica.entity.MyAccount;

/**
 * Created by TakumaLee on 2016/6/2.
 */
public class KomicaAccountManager {
    private static final String TAG = KomicaAccountManager.class.getSimpleName();

    private static KomicaAccountManager INSTANCE = null;

    private static final String KOMICA_ACCOUNT_FB_ID = "komica_account_fb_id";
    private static final String KOMICA_ACCOUNT_USERNAME = "komica_account_username";
    private static final String KOMICA_ACCOUNT_EMAIL = "komica_account_email";
    private static final String KOMICA_ACCOUNT_PHOTO = "komica_account_photo";
    private static final String KOMICA_ACCOUNT_COVER = "komica_account_cover";

    private static final String PREFERENCE_SWITCH_LOGIN = "switch_login";

    private MyAccount myAccount;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences noClearSharedPreferences;

    public synchronized static KomicaAccountManager getInstance() {
        return INSTANCE;
    }

    public KomicaAccountManager(Context context) {
        myAccount = new MyAccount();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        myAccount.setFbId(sharedPreferences.getString(KOMICA_ACCOUNT_FB_ID, ""));
        myAccount.setUsername(sharedPreferences.getString(KOMICA_ACCOUNT_USERNAME, "Guest"));
        myAccount.setEmail(sharedPreferences.getString(KOMICA_ACCOUNT_EMAIL, ""));
        myAccount.setHeaderPic(sharedPreferences.getString(KOMICA_ACCOUNT_PHOTO, ""));
        myAccount.setCoverPic(sharedPreferences.getString(KOMICA_ACCOUNT_COVER, ""));

        noClearSharedPreferences = context.getSharedPreferences("NO_CLEAR", Context.MODE_PRIVATE);
        KomicaManager.getInstance().enableSwitchLogin(noClearSharedPreferences.getBoolean(PREFERENCE_SWITCH_LOGIN, false));
    }

    public static void initialize(Context context) {
        INSTANCE = new KomicaAccountManager(context);
    }

    public void logout() {
        myAccount.setFbId("");
        myAccount.setUsername("Guest");
        myAccount.setEmail("");
        myAccount.setHeaderPic("");
        myAccount.setCoverPic("");
        editor.clear();
        ThirdPartyManager.getInstance().logout();
    }

    protected void applyNoClearPreference() {
        SharedPreferences.Editor noClearEditor = noClearSharedPreferences.edit();
        noClearEditor.putBoolean(PREFERENCE_SWITCH_LOGIN, KomicaManager.getInstance().isSwitchLogin());
        noClearEditor.commit();
    }

    public void setMyAccount(MyAccount myAccount) {
        this.myAccount = myAccount;
    }

    public void savedMyAccout() {
        editor.putString(KOMICA_ACCOUNT_FB_ID, myAccount.getFbId());
        editor.putString(KOMICA_ACCOUNT_USERNAME, myAccount.getUsername());
        editor.putString(KOMICA_ACCOUNT_EMAIL, myAccount.getEmail());
        editor.putString(KOMICA_ACCOUNT_PHOTO, myAccount.getHeaderPic());
        editor.putString(KOMICA_ACCOUNT_COVER, myAccount.getCoverPic());
        editor.commit();
    }

    public MyAccount getMyAccount() {
        return myAccount;
    }

    public boolean isLogin() {
        return BuildConfig.DEBUG || ThirdPartyManager.getInstance().isFacebookLogin();
    }

}
