package idv.kuma.app.komica.manager;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

import idv.kuma.app.komica.BuildConfig;
import idv.kuma.app.komica.entity.KomicaMenuGroup;
import idv.kuma.app.komica.entity.KomicaMenuMember;
import idv.kuma.app.komica.utils.KLog;

/**
 * Created by TakumaLee on 2016/12/6.
 */

public class KomicaManager {
    private static final String TAG = KomicaManager.class.getSimpleName();

    public interface OnUpdateConfigListener {
        void onUpdated();
    }

    private List<OnUpdateConfigListener> onUpdateConfigListeners;

    private List<KomicaMenuGroup> menuGroupList;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean switchLogin = false;

    public class WebType {
        // new, live
        public static final int NORMAL = 1;
        // 綜合
        public static final int INTEGRATED = 2;
        public static final int WEB = 10;
    }

    private static class SingletonHolder {
        private static KomicaManager INSTANCE = new KomicaManager();
    }

    public static KomicaManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public KomicaManager() {
        this.onUpdateConfigListeners = new ArrayList<>();
        this.menuGroupList = new ArrayList<>();
        initConfig();
    }

    public void registerUpdateListener(OnUpdateConfigListener listener) {
        onUpdateConfigListeners.add(listener);
    }

    public void unRegisterUpdateListener(OnUpdateConfigListener listener) {
        onUpdateConfigListeners.remove(listener);
    }

    private void initConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        fetchNewConfig();
    }

    public void fetchNewConfig() {
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    boolean tmpSwitch = mFirebaseRemoteConfig.getBoolean("switch_login");
                    if (tmpSwitch != switchLogin) {
                        switchLogin = tmpSwitch;
                        KomicaAccountManager.getInstance().applyNoClearPreference();
                        for (OnUpdateConfigListener listener : onUpdateConfigListeners) {
                            listener.onUpdated();
                        }
                    }
                    KLog.v(TAG, "switch: " + switchLogin);
                } else {

                }
            }
        });
    }

    public void setMenuGroupList(List<KomicaMenuGroup> menuGroupList) {
        this.menuGroupList = menuGroupList;
    }

    public KomicaMenuMember findMemberByMemberId(int memberId) {
        KomicaMenuMember member = null;
        int itemCount = 0;
        for (KomicaMenuGroup group : menuGroupList) {
            itemCount += group.getMemberList().size();
            if (memberId >= itemCount) {
                continue;
            } else {
                member = group.getMemberList().get(memberId - group.getMemberList().get(0).getMemberId());
                break;
            }
        }
        return member;
    }

    public List<KomicaMenuGroup> getMenuGroupList() {
        return menuGroupList;
    }

    public boolean isSwitchLogin() {
        return switchLogin;
    }

    protected void enableSwitchLogin(boolean enable) {
        this.switchLogin = enable;
    }

    public boolean checkVisible(String memberTitle) {
        if (!switchLogin || !ThirdPartyManager.getInstance().isFacebookLogin()) {
            switch (checkWebType(memberTitle)) {
                case WebType.INTEGRATED:
                case WebType.NORMAL:
                    return true;
                default:
                    return false;
            }
        } else {
            switch (memberTitle) {
                case "角色配對":
                case "Komica2":
                    return false;
                case "祭典":
                case "萌":
                case "巫女":
                case "魔女":
                case "蘿莉":
                case "御姐":
                case "妹系":
                case "寫真":
                case "高解析度":
                    return ThirdPartyManager.getInstance().isFacebookLogin();
                default:
                    return true;
            }
        }
    }

    public int checkWebType(String menuStr) {
        switch (menuStr) {
            case "影視":
            case "綜合":
            case "氣象":
            case "歡樂惡搞":
            case "模型":
            case "蘿蔔":
            case "攝影":
            case "軍武":
            case "改造":
            case "委託":
            case "鋼普拉":
                return WebType.INTEGRATED;
            case "動畫":
//            case "螢幕攝":
//            case "漫畫":
            case "新番捏他":
            case "新番實況":
            case "綜合學術":
            case "職業":
            case "財經":
            case "生活消費":
            case "法律":
            case "閒談@香港":
            case "藝術":
            case "生存遊戲":
            case "燃":
            case "猜謎":
            case "故事接龍":
            case "大自然":
            case "星座命理":
            case "戀愛":
            case "超常現象":
            case "流言終結":
            case "旅遊":
            case "手工藝":
            case "圖書":
            case "短片":
                return WebType.NORMAL;
            default:
                return WebType.WEB;
        }
    }
}
