package idv.kuma.app.komica.manager;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import idv.kuma.app.komica.entity.MyAccount;
import idv.kuma.app.komica.entity.push.PushDevice;
import idv.kuma.app.komica.entity.push.PushUser;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.utils.KLog;


/**
 * Created by TakumaLee on 2016/7/31.
 */
public class FirebaseManager {
    private static final String TAG = FirebaseManager.class.getSimpleName();

    public class KeyData {
        public static final String TO = "to";
        public static final String DATA = "data";
        public static final String TO_USER_ID = "toUserId";
        public static final String FROM_USER_ID = "fromUserId";
        public static final String FROM_USER_NAME = "fromUserName";
        public static final String FROM_USER_HEAD_PIC = "fromUserHeadPic";
        public static final String MESSAGE = "message";
        public static final String PRIORITY = "priority";

        // for iOS device
        public static final String CONTENT_AVAILABLE = "content_available";
    }

    public class Priority {
        public static final String NORMAL = "normal";
        public static final String HIGH = "high";
    }

    public class KeyNotification {
        public static final String NOTIFICATION = "notification";
        public static final String NOTIFICATION_BODY = "body";
        public static final String NOTIFICATION_TITLE = "title";
//        public static final String NOTIFICATION_ICON = "icon";
        public static final String NOTIFICATION_SOUND = "sound";
        public static final String NOTIFICATION_BADGE = "badge";
    }

    private DatabaseReference databaseRefUsers;

    public FirebaseManager() {
        databaseRefUsers = FirebaseDatabase.getInstance().getReference();
    }

    private static class SingletonHolder {
        private static FirebaseManager INSTANCE = new FirebaseManager();
    }

    public static final FirebaseManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void updateUserPushData() {
        if (KomicaAccountManager.getInstance().getMyAccount().getAdId() != null || KomicaAccountManager.getInstance().getAdIdTmp() != null) {
            KLog.v(TAG, "update user push data.");
            if (KomicaAccountManager.getInstance().getMyAccount().getAdId() != null) {
                MyAccount myAccount = KomicaAccountManager.getInstance().getMyAccount();
                myAccount.setAdId(KomicaAccountManager.getInstance().getAdIdTmp());
                KomicaAccountManager.getInstance().setMyAccount(myAccount);
                KomicaAccountManager.getInstance().savedMyAccout();
            }
            postUserPushData();
        } else {
            KLog.v(TAG, "User push data cannot get adId.");
        }
    }

    private void postUserPushData() {
        if (FirebaseInstanceId.getInstance().getToken() == null) {
            KLog.v(TAG, "push token is null.");
            return;
        }
        String object = "uuid=" + KomicaAccountManager.getInstance().getMyAccount().getFbId()
                + "&deviceToken=" + FirebaseInstanceId.getInstance().getToken()
                + "&devType=" + "android";


        final PushDevice device = new PushDevice(FirebaseInstanceId.getInstance().getToken());
        final MyAccount myAccount = KomicaAccountManager.getInstance().getMyAccount();
        databaseRefUsers.child(PushUser.DATABASE_USERS).child(myAccount.getAdId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PushUser pushUser = dataSnapshot.getValue(PushUser.class);
                if (pushUser == null) {
                    pushUser = new PushUser();
                    if (null != myAccount) {
                        pushUser.setName(myAccount.getUsername());
                        pushUser.setFbId(myAccount.getFbId());
                        pushUser.setPoints(myAccount.getPoints());
                    }
                } else {
                    myAccount.setPoints(pushUser.getPoints());
                    KomicaAccountManager.getInstance().setMyAccount(myAccount);
                    KomicaAccountManager.getInstance().savedMyAccout();
                }
                boolean hasDevice = false;
                for (PushDevice pushDevice : pushUser.getDeviceList()) {
                    if (pushDevice.getToken().equals(device.getToken())) {
                        hasDevice = true;
                        break;
                    }
//                    if (pushDevice.getDevice() != null && pushDevice.getDevice().equals("android")) {
//                        return;
//                    }
                }
                if (!hasDevice) {
                    pushUser.addDeivce(device);
                }
                databaseRefUsers.child(PushUser.DATABASE_USERS).child(myAccount.getAdId()).setValue(pushUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Deprecated
    public void sendUserPush(final String accountId, final String message) {
        databaseRefUsers.child(PushUser.DATABASE_USERS).child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final PushUser pushUser = dataSnapshot.getValue(PushUser.class);
                if (pushUser == null || pushUser.getDeviceList() == null || pushUser.getDeviceList().size() == 0) {
                    return;
                }
                final FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                        .setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build();
                final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                firebaseRemoteConfig.setConfigSettings(configSettings);
                long cacheExpiration = 86400;
                firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            KLog.d(TAG, "Fetch succeeded.");
                            firebaseRemoteConfig.activateFetched();
                            if (pushUser.getDeviceList() != null) {
                                for (PushDevice device : pushUser.getDeviceList()) {
                                    if (device.getDevice() == null) {
                                        continue;
                                    }
                                    boolean isIOS = !device.getDevice().equals("android");
                                    sendPush(firebaseRemoteConfig, message, accountId, device.getToken(), isIOS);
                                }
                            }
                        } else {
                            KLog.d(TAG, "Fetch Failed." + task.getException().getMessage());
                        }
                    }
                });
                firebaseRemoteConfig.fetch(cacheExpiration).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        KLog.d(TAG, "onFailure push request: " + e.getMessage());
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Deprecated
    private void sendPush(FirebaseRemoteConfig firebaseRemoteConfig, String message, String accountId, String regId, boolean isIOS) {
        String url = firebaseRemoteConfig.getString("server_push_url");
        String key = firebaseRemoteConfig.getString("server_push_key");
        KLog.v(TAG, url + "\n" + key);
        JSONObject object = new JSONObject();
        JSONObject dataObj = new JSONObject();
        JSONObject notificationObj = new JSONObject();
        try {
            dataObj.put(KeyData.MESSAGE, message);
            dataObj.put(KeyData.FROM_USER_HEAD_PIC, KomicaAccountManager.getInstance().getMyAccount().getHeaderPic());
            dataObj.put(KeyData.FROM_USER_NAME, KomicaAccountManager.getInstance().getMyAccount().getUsername());
            dataObj.put(KeyData.FROM_USER_ID, KomicaAccountManager.getInstance().getMyAccount().getFbId());
            dataObj.put(KeyData.TO_USER_ID, accountId);

            // for iOS device
            dataObj.put(KeyData.CONTENT_AVAILABLE, true);

            object.put(KeyData.TO, regId);
            object.put(KeyData.PRIORITY, Priority.HIGH);
            object.put(KeyData.DATA, dataObj);

            if (isIOS) {
                notificationObj.put(KeyNotification.NOTIFICATION_BODY, message);
                notificationObj.put(KeyNotification.NOTIFICATION_TITLE, KomicaAccountManager.getInstance().getMyAccount().getUsername());
                notificationObj.put(KeyNotification.NOTIFICATION_SOUND, "default");
                notificationObj.put(KeyNotification.NOTIFICATION_BADGE, "1");
//            notificationObj.put(KeyNotification.NOTIFICATION_ICON, KomicaAccountManager.getInstance().getMyAccount().getAvatarImgUrl());
                object.put(KeyNotification.NOTIFICATION, notificationObj);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        KLog.v(TAG, object.toString());
        if ((url == null || url.isEmpty()) && (key == null || key.isEmpty())) {
            return;
        }
        OkHttpClientConnect.excuteAutoPost("Authorization", key, url, object.toString(), OkHttpClientConnect.CONTENT_TYPE_JSON, new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {
                KLog.v(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(int responseCode, String result) {
                KLog.v(TAG, result);
            }
        });
    }

}
