package idv.kuma.app.komica.manager;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import idv.kuma.app.komica.BuildConfig;
import idv.kuma.app.komica.configs.WebUrlFormaterUtils;
import idv.kuma.app.komica.entity.KomicaMenuGroup;
import idv.kuma.app.komica.entity.KomicaMenuMember;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.utils.KLog;

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
        this.onUpdateMenuListeners = new ArrayList<>();
        this.onUpdateConfigListeners = new ArrayList<>();
        this.menuGroupList = new ArrayList<>();
        if (!switchLogin) {
            initConfig();
        }
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
        OkHttpClientConnect.excuteAutoGet(WebUrlFormaterUtils.getKomicaMenuUrl(), new NetworkCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onResponse(int responseCode, String result) {
                Document document = Jsoup.parse(result);
                Element element = document.getElementsByTag("font").remove(1);
                int count = 0;
                int memberId = 0;
                KomicaMenuGroup group = null;
                List<KomicaMenuGroup> groupList = new ArrayList<>();
                List<KomicaMenuMember> members = new ArrayList<>();
                for (Element elem : element.children()) {
                    if ("b".equals(elem.tagName())) {
                        if (group != null && members.size() > 0) {
                            group.setMemberList(members);
                            groupList.add(group);
                            members = new ArrayList<>();
                        }
                        group = new KomicaMenuGroup();
                        group.setGroupId(count);
                        group.setGroupPosition(count++);
                        group.setTitle(elem.text());
                        continue;
                    }
                    if ("a".equals(elem.tagName())) {
                        KomicaMenuMember member = new KomicaMenuMember();
                        String title = "";
                        Pattern pattern = Pattern.compile("\\, '(.*?)'\\)");
                        Matcher matcher = pattern.matcher(elem.attr("onclick"));
                        if (matcher.find()) {
                            title = matcher.group(1);
                        }
                        if ("".equals(title) || title == null) {
                            title = elem.text();
                        }
                        member.setTitle(title);
                        if ("動物綜合".equals(title)) {
                            member.setLinkUrl("http://2nyan.org/animal/");
                        } else {
                            member.setLinkUrl(elem.attr("href"));
                        }
                        member.setMemberId(memberId++);
                        members.add(member);
                    }
                }
                group.setMemberList(members);
                groupList.add(group);
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

//                fragment.notifyDrawerBuild();
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
//            case "影視":
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
            case "遊戲速報":
//            case "行動遊戲":
            case "網路遊戲":
            case "塗鴉王國":
            case "飲食":
            case "體育":

            case "電腦":
            case "文化交流":
            case "新聞":
            case "寫真":
            case "女性角色":
            case "男性角色":
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

            case "FEZ":
            case "GTA":
            case "TOS":
            case "戰車世界":
            case "戰車風雲":
            case "戰車雷霆":
            case "戰機世界":
            case "戰艦世界":
            case "艦隊收藏":
            case "魔物獵人":
//            case "Minecraft":
            case "葉鍵":
            case "涼宮":
            case "Homestuck":
            case "IM@S":
            case "Pokemon":
            case "Pretty Cure":
            case "Strike Witches":
            case "GAINAX":
            case "KOEI":
            case "SQEX":
            case "TYPE-MOON":
//            case "京都動畫":
            case "聲優綜合":
            case "釘宮":
//            case "田村/堀江/水樹":
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

            case "塗鴉工廠":
            case "同人2":
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

            case "Apple":
                return WebType.NORMAL;
            default:
                return WebType.WEB;
        }
    }
}
