package idv.kuma.app.komica.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import idv.kuma.app.komica.manager.FirebaseManager;
import idv.kuma.app.komica.utils.KLog;

/**
 * Created by TakumaLee on 2016/5/24.
 */
public class KomicaFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = KomicaFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        KLog.d(TAG, "Refreshed token: " + refreshedToken);
        FirebaseManager.getInstance().updateUserPushData();
    }
}
