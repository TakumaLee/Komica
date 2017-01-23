package idv.kuma.app.komica.entity;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TakumaLee on 2016/12/11.
 */

public class KReply extends KPost {

    private List<KReplyObj> replyObjList;

    public KReply() {
        replyObjList = new ArrayList<>();
    }

    public KReply(Element element, String domainUrl) {
        super(element, "reply", domainUrl);
        replyObjList = new ArrayList<>();
        Elements qlinks = element.getElementsByClass("qlink");
        for (Element link : qlinks) {
            KReplyObj replyObj = new KReplyObj();
            replyObj.setQlinkUrl(domainUrl + link.attr("href"));
            replyObj.setLinker(link.text());
            replyObjList.add(replyObj);
        }

    }

}
