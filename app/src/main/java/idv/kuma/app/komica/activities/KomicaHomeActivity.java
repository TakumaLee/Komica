package idv.kuma.app.komica.activities;

import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.base.BaseActivity;
import idv.kuma.app.komica.configs.WebUrlFormaterUtils;
import idv.kuma.app.komica.entity.KomicaMenuGroup;
import idv.kuma.app.komica.entity.KomicaMenuMember;
import idv.kuma.app.komica.fragments.KomicaHomeFragment;
import idv.kuma.app.komica.fragments.base.BaseFragment;
import idv.kuma.app.komica.http.NetworkCallback;
import idv.kuma.app.komica.http.OkHttpClientConnect;
import idv.kuma.app.komica.manager.KomicaManager;

public class KomicaHomeActivity extends BaseActivity {

    KomicaHomeFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_komica_home);

        tracker.setScreenName("首頁");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        fragment = new KomicaHomeFragment();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_komica_home, fragment).commit();
        }
        loadKomicaMenu();
    }

    private void loadKomicaMenu() {
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
                        member.setLinkUrl(elem.attr("href"));
                        member.setMemberId(memberId++);
                        members.add(member);
                    }
                }
                group.setMemberList(members);
                groupList.add(group);
                KomicaManager.getInstance().setMenuGroupList(groupList);
                fragment.notifyDrawerBuild();
            }
        });
    }

    @Override
    public void onBackPressed() {
        try {
            BaseFragment fragmentBase = (BaseFragment) getSupportFragmentManager().getFragments().get(0);
            if (fragmentBase == null || fragmentBase.isBackPressed()) {
                super.onBackPressed();
            }
        } catch (ClassCastException e) {

        }
    }
}
