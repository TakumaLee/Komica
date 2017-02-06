package idv.kuma.app.komica.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.lang.ref.WeakReference;

import idv.kuma.app.komica.entity.MyAccount;
import idv.kuma.app.komica.utils.KLog;

/**
 * Created by TakumaLee on 2016/6/2.
 */
public class KomicaAccountManager {
    private static final String TAG = KomicaAccountManager.class.getSimpleName();

    private static KomicaAccountManager INSTANCE = null;
    WeakReference<Context> contextWeakReference;

    private static final String KOMICA_ACCOUNT_AD_ID = "komica_account_ad_id";
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

    private String adIdTmp;

    private HandlerThread handlerThread = new HandlerThread(TAG);
    private Handler handler;

    public synchronized static KomicaAccountManager getInstance() {
        return INSTANCE;
    }

    public KomicaAccountManager(Context context) {
        contextWeakReference = new WeakReference<Context>(context);
        myAccount = new MyAccount();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contextWeakReference.get());
        editor = sharedPreferences.edit();
        myAccount.setAdId(sharedPreferences.getString(KOMICA_ACCOUNT_AD_ID, null));
        myAccount.setFbId(sharedPreferences.getString(KOMICA_ACCOUNT_FB_ID, ""));
        myAccount.setUsername(sharedPreferences.getString(KOMICA_ACCOUNT_USERNAME, "Guest"));
        myAccount.setEmail(sharedPreferences.getString(KOMICA_ACCOUNT_EMAIL, ""));
        myAccount.setHeaderPic(sharedPreferences.getString(KOMICA_ACCOUNT_PHOTO, ""));
        myAccount.setCoverPic(sharedPreferences.getString(KOMICA_ACCOUNT_COVER, ""));

        noClearSharedPreferences = contextWeakReference.get().getSharedPreferences("NO_CLEAR", Context.MODE_PRIVATE);
        KomicaManager.getInstance().enableSwitchLogin(noClearSharedPreferences.getBoolean(PREFERENCE_SWITCH_LOGIN, false));

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                try {
                    String adId = AdvertisingIdClient.getAdvertisingIdInfo(contextWeakReference.get()).getId();
                    adIdTmp = adId;
                    if (null != myAccount) {
                        myAccount.setAdId(adId);
                    }
                    FirebaseManager.getInstance().updateUserPushData();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } finally {
                    handlerThread.quit();
                    handlerThread.interrupt();
                }
            }
        };
        if (myAccount.getAdId() != null) {
            KLog.v(TAG, "Ad Id is not null.");
            handlerThread.quit();
            handlerThread.interrupt();
        } else {
            KLog.v(TAG, "Ad Id is null.");
            handler.sendEmptyMessage(0);
        }
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

    public String getAdIdTmp() {
        return adIdTmp;
    }

    public boolean isLogin() {
        return ThirdPartyManager.getInstance().isFacebookLogin();
    }

}
