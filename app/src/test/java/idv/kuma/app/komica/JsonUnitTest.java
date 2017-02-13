package idv.kuma.app.komica;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by TakumaLee on 2017/2/12.
 */

public class JsonUnitTest {

    @Test
    public void promotInfo_testString() throws Exception, JSONException {
        String info = "<![CDATA[" +
                "        <br>\n" +
                "        <br>Komica 這個網站有太多的格式，故為了解析每個不同的頁面需要耗費大量的時間。\n" +
                "        <br />同時解析後為了支援相同架構也是一份大工程。\n" +
                "        <br />目前已完成多頁\n" +
                "        <br />觀看更多的 <font color=\"#0099ff\">返信</font> 已可點擊。\n" +
                "        <br />影片播放功能還未製作完畢，\n" +
                "        <br />其他頁面也將在最近慢慢補上。\n" +
                "        <br />\n" +
                "        <br /><font color=\"#ff0000\">重要聲明：</font>\n" +
                "        <br />由於 Google 服務條款的關係：\n" +
                "        <br /><font color=\"#ff0000\">「禁止任何應用程式納入或宣傳煽情露骨內容」</font>\n" +
                "        <br />又因糟糕島性質問題故導致無法完全符合規範\n" +
                "        <br />所以產品上將會<font color=\"#ff0000\">有些頁面與圖片無法看見</font>\n" +
                "        目前依然偏向加速完成狀態，架構上有許多地方需要修改請見諒。]]>";
        JSONArray array = new JSONArray();
        array.put(info);
        System.out.print(array);
        Assert.assertNotEquals(array.toString(), "[" + info + "]");
    }
}
