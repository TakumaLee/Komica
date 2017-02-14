package idv.kuma.app.komica.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import idv.kuma.app.komica.BuildConfig;
import idv.kuma.app.komica.configs.WebUrlFormaterUtils;
import idv.kuma.app.komica.context.ApplicationContextSingleton;
import idv.kuma.app.komica.entity.KomicaMenuGroup;
import idv.kuma.app.komica.entity.KomicaMenuMember;
import idv.kuma.app.komica.utils.KLog;
import idv.kuma.app.player.PlayerActivity;

/**
 * Created by TakumaLee on 2016/12/6.
 */

public class KomicaManager {
    private static final String TAG = KomicaManager.class.getSimpleName();

    public interface OnUpdateConfigListener {
        void onConfigUpdated();
    }

    public interface OnUpdateMenuListener {
        void onMenuUpdated();
    }

    private List<OnUpdateConfigListener> onUpdateConfigListeners;
    private List<OnUpdateMenuListener> onUpdateMenuListeners;

    private JSONObject menuKeyObj;
    private List<KomicaMenuGroup> menuGroupList;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean switchLogin = false;

    public class WebType {
        // new, live
        public static final int NORMAL = 1;
        // 綜合
        public static final int INTEGRATED = 2;
        public static final int THREADS = 3;
        public static final int THREADS_LIST = 4;
        public static final int WEB = 10;
    }

    private static class SingletonHolder {
        private static KomicaManager INSTANCE = new KomicaManager();
    }

    public static KomicaManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public KomicaManager() {
        this.onUpdateMenuListeners = new ArrayList<>();
        this.onUpdateConfigListeners = new ArrayList<>();
        this.menuGroupList = new ArrayList<>();
        if (!switchLogin) {
            initConfig();
        }
    }

    public void clearCache() {
        Glide.get(ApplicationContextSingleton.getApplicationContext()).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(ApplicationContextSingleton.getApplicationContext()).clearDiskCache();
            }
        }).start();
    }

    public void registerConfigUpdateListener(OnUpdateConfigListener listener) {
        onUpdateConfigListeners.add(listener);
    }

    public void unRegisterConfigUpdateListener(OnUpdateConfigListener listener) {
        onUpdateConfigListeners.remove(listener);
    }

    public void registerMenuUpdateListener(OnUpdateMenuListener listener) {
        onUpdateMenuListeners.add(listener);
    }

    public void unRegisterMenuUpdateListener(OnUpdateMenuListener listener) {
        onUpdateMenuListeners.remove(listener);
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
        if (switchLogin) {
            return;
        }
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    boolean tmpSwitch = mFirebaseRemoteConfig.getBoolean("switch_login");
                    if (tmpSwitch != switchLogin) {
                        switchLogin = tmpSwitch;
                        if (switchLogin) {
                            KomicaAccountManager.getInstance().applyNoClearPreference();
                        }
                        for (OnUpdateConfigListener listener : onUpdateConfigListeners) {
                            listener.onConfigUpdated();
                        }
                    }
                    KLog.v(TAG, "switch: " + switchLogin);
                } else {

                }
            }
        });
    }

    public void startPlayerActivity(Context context,String title, String url) {
        Intent intent = new Intent(context, PlayerActivity.class);
//                                intent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS, preferExtensionDecoders);
//                                if (drmSchemeUuid != null) {
//                                    intent.putExtra(PlayerActivity.DRM_SCHEME_UUID_EXTRA, drmSchemeUuid.toString());
//                                    intent.putExtra(PlayerActivity.DRM_LICENSE_URL, drmLicenseUrl);
//                                    intent.putExtra(PlayerActivity.DRM_KEY_REQUEST_PROPERTIES, drmKeyRequestProperties);
//                                }
        intent.setData(Uri.parse(url))
                .putExtra(PlayerActivity.PLAYER_TITLE, title)
//                                    .putExtra(PlayerActivity.EXTENSION_EXTRA, extension)
                .setAction(PlayerActivity.ACTION_VIEW);
        context.startActivity(intent);
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

    public void loadKomicaMenu() {
        AndroidNetworking.get(WebUrlFormaterUtils.getKomicaMenuKeyUrl())
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                try {
                    menuKeyObj = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {

            }
        });
        AndroidNetworking.get(WebUrlFormaterUtils.getKomicaMenuUrl())
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                int count = 0;
                int memberId = 0;
                List<KomicaMenuGroup> groupList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        String title = array.getJSONObject(i).getString("title");
                        KomicaMenuGroup group = new KomicaMenuGroup();
                        group.setGroupId(i);
                        group.setGroupPosition(i);
                        group.setTitle(title);
                        JSONArray memArr = array.getJSONObject(i).getJSONArray("member");
                        List<KomicaMenuMember> members = new ArrayList<>();
                        for (int j = 0; j < memArr.length(); j++) {
                            KomicaMenuMember member = new KomicaMenuMember();
                            JSONObject object = memArr.getJSONObject(j);
                            member.setTitle(object.getString("title"));
                            member.setLinkUrl(object.getString("url"));
                            member.setMemberId(memberId++);
                            members.add(member);
                        }
                        group.setMemberList(members);
                        groupList.add(group);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setMenuGroupList(groupList);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (OnUpdateMenuListener listener : onUpdateMenuListeners) {
                            listener.onMenuUpdated();
                        }
                    }
                });
            }

            @Override
            public void onError(ANError anError) {

            }
        });
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
        if (!switchLogin || !KomicaAccountManager.getInstance().isLogin()) {
            switch (checkWebType(memberTitle)) {
                case WebType.THREADS_LIST:
                case WebType.THREADS:
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
                    return KomicaAccountManager.getInstance().isLogin();
                default:
                    return true;
            }
        }
    }

    public int checkWebType(String menuStr) {
        try {
            return menuKeyObj.getInt(menuStr);
        } catch (NullPointerException e) {
            return checkLocalWebType(menuStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return checkLocalWebType(menuStr);
    }

    private int checkLocalWebType(String menuStr) {
        switch (menuStr) {
            case "繪師":
            case "天文":
            case "服飾":
            case "行動遊戲":
            case "體感遊戲":
            case "桌上遊戲":
            case "龍騎士07":
            case "咖啡/茶":
            case "鳥":
                return WebType.THREADS_LIST;
            case "影視":

            case "校園":
            case "消費電子":

            case "女性角色":
            case "男性角色":
            case "四格":

            case "遊戲速報":

            case "3D":
            case "單車":
            case "程設交流":
                return WebType.THREADS;
            case "綜合":
            case "掛圖":
            case "氣象":
            case "模型":
            case "玩偶":
            case "歡樂惡搞":
            case "蘿蔔":
            case "攝影":
            case "軍武":
            case "改造":
            case "委託":
            case "鋼普拉":
//            case "行動遊戲":
            case "網路遊戲":
            case "塗鴉王國":
            case "飲食":
            case "體育":

            case "電腦":
            case "文化交流":
            case "新聞":
            case "寫真":
            case "中性角色":
            case "擬人化":
            case "少女漫畫":
            case "音樂":
            case "布袋戲":
            case "小說":
            case "奇幻":
            case "紙牌":
            case "高解析度":
            case "GIF":
            case "FLASH":
            case "MAD":
            case "素材":
            case "求圖":
                return WebType.INTEGRATED;
            case "綜合2":
            case "動畫":
            case "螢幕攝":
            case "漫畫":
            case "新番捏他":
            case "新番實況":
            case "三次實況":
            case "特攝":
            case "車":
            case "COSPLAY":
            case "綜合學術":
            case "數學":
            case "歷史":
            case "地理":
            case "職業":
            case "財經":
            case "生活消費":
            case "法律":
            case "閒談@香港":
            case "藝術":
            case "生存遊戲":
            case "燃":
            case "笑話":
            case "猜謎":
            case "故事接龍":
            case "歐美動畫":
            case "大自然":
            case "星座命理":
            case "New Age":
            case "戀愛":
            case "超常現象":
            case "夢":
            case "流言終結":
            case "政治":
            case "旅遊":
            case "耳機":
            case "手機":
            case "美容":
            case "髮型":
            case "家政":
            case "手工藝":
            case "圖書":
            case "讀書筆記":

            case "短片":
            case "短片2":
            case "動作遊戲":
            case "格鬥遊戲":
            case "2D STG":
            case "3D STG":
            case "冒險遊戲":
            case "RPG":
            case "養成遊戲":
            case "戀愛遊戲":
            case "音樂遊戲":
            case "網頁遊戲":
            case "獨立遊戲":

            case "麻將":
            case "遊戲設計":
            case "RPG Maker":
            case "STEAM":

            case "CosmicBreak":
            case "Elsword":
            case "DNF":
            case "DOTA2":
            case "FEZ":
            case "GW2":
            case "GTA":
            case "LOL":
            case "Minecraft":
            case "PAD":
            case "PSO2":
            case "SDGO":
            case "StarCraft":
            case "T7S":
            case "TOS":
            case "白貓Project":
            case "流亡黯道 PoE":
            case "新瑪奇英雄傳":
            case "戰車世界":
            case "戰地風雲":
            case "戰爭雷霆":
            case "戰機世界":
            case "戰艦世界":
            case "艦隊收藏":
            case "魔物獵人":
            case "爐石戰記":
            case "星空幻想":
            case "葉鍵":
            case "涼宮":
            case "反逆":
            case "奈葉":
            case "廢怯少女":
            case "禁書":
            case "遊戲王":
            case "女王之刃":
            case "Digimon":
            case "Homestuck":
            case "IM@S":
            case "LoveLive!":
            case "Pokemon":
            case "Pretty Cure":
            case "Saki":
            case "Strike Witches":
            case "VOCALOID":

            case "Capcom":
            case "GAINAX":
            case "KOEI":
            case "SQEX":
            case "TYPE-MOON":
            case "京都動畫":
            case "聲優綜合":
            case "釘宮":
            case "田村/堀江/水樹":
            case "AKB48":

            case "角色配對":
            case "催淚":
            case "性轉換":
            case "Maid":
            case "巫女":
            case "魔女":
            case "蘿莉":
            case "正太":
            case "御姊":
            case "兄貴":
            case "妹系":
            case "人外":
            case "獸":
            case "機娘":
            case "返信娘":
            case "Lolita Fashion":
            case "傲嬌":

            case "塗鴉工廠":
            case "MMD":
            case "同人2":
            case "SOHO":
            case "宣傳":

            case "酒":
            case "素食":

            case "足球":
            case "武術":
            case "動物綜合":
            case "犬":
            case "貓":
            case "蟲":
            case "水族":
            case "認養":
//            case "二次壁":
            case "PSP壁":

            case "Pixmicat!":
//            case "Joyful Note":
            case "網頁設計":
            case "Apple":

                return WebType.NORMAL;
            default:
                return WebType.WEB;
        }
    }
}
