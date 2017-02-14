package idv.kuma.app.komica;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by TakumaLee on 2017/2/12.
 */

public class JsonUnitTest {

    @Test
    public void promotInfo_testString() throws Exception, JSONException {
        String info =
                "<br>更新資訊： 版本 0.6.0" +
                "<br>" +
                "<br />新增<font color=\"#ff0000\">最新消息</font>" +
                "<br />新增回覆單一貼文功能" +
                "<br />UI 稍做修改" +
                "<br />影片播放功能已可正常執行" +
                "<br />其他頁面也將在最近慢慢補上。";
        JSONObject object = new JSONObject();
        object.put("promoteInfo", info);
        System.out.print(object);
        Assert.assertNotEquals(object.toString(), "{" + info + "}");
    }
}
