package idv.kuma.app.komica.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import idv.kuma.app.komica.entity.KPostImage;
import idv.kuma.app.komica.entity.KReply;
import idv.kuma.app.komica.entity.KTitle;
import idv.kuma.app.komica.manager.KomicaManager;

/**
 * Created by TakumaLee on 2016/12/23.
 */

public class CrawlerUtils {
    public static final String TAG = CrawlerUtils.class.getSimpleName();

    public static List<KTitle> getPostList(Document document, String url, int webType) {
        switch (webType) {
            case KomicaManager.WebType.NORMAL:
                return parseShinBangumiPost(document, url);
            case KomicaManager.WebType.INTEGRATED:
                return getIntegratedPostList(document, url);
            default:
                return getIntegratedPostList(document, url);
        }
    }

    public static List<KTitle> parseShinBangumiPost(Document document, String url) {
        if (null == document.getElementById("threads")) {
            return new ArrayList<>();
        }
        Elements elements = document.getElementById("threads").children();
        List<KTitle> titlePostList = new ArrayList<>();
        KTitle titlePost = null;
        List<KReply> replyList = new ArrayList<KReply>();
        for (Element element : elements) {
            KLog.v(TAG, "Element: " + element);
            if (element.className().startsWith("threadpost")) {
                // TODO post head
                // TODO new a post head, and continue reply to next new.
                if (null != titlePost) {
                    titlePost.setReplyList(replyList);
                    titlePostList.add(titlePost);
                    replyList = new ArrayList<KReply>();
                }
                titlePost = new KTitle(element, url);
            } else if (element.className().startsWith("reply")) {
                // TODO post reply
                KReply replyPost = new KReply(element, url);
                replyList.add(replyPost);
            }
        }
        titlePost.setReplyList(replyList);
        titlePostList.add(titlePost);
        return titlePostList;
    }

    public static List<KTitle> getIntegratedPostList(Document document, String url) {
        List<KTitle> headList = new ArrayList<KTitle>();
        Elements container = document.getElementsByClass("container");
        if (container != null && container.first() != null) {
            Elements containerChildren = container.first().getElementsByTag("form").first().children();
            headList.addAll(parseIntegratedPostList(containerChildren, url));
            Elements elements = document.getElementsByClass("container").parents().get(0).children();
            headList.addAll(parseIntegratedPostList(elements, url));
        }
        return headList;
    }

    private static List<KTitle> parseIntegratedPostList(Elements elements, String url) {
        KTitle head = null;
        List<KTitle> headList = new ArrayList<KTitle>();
        List<KReply> replyList = new ArrayList<KReply>();
        for (Element element : elements) {
            if ("a".equals(element.tagName())) {
                // post start

                if (head != null && ("返信".equals(element.text()) || "Reply".equals(element.text()))) {
                    head.setDetailLink(url.substring(0, url.lastIndexOf("/") + 1) + element.attr("href"));
                } else if (!element.hasClass("del")) {
                    if (head == null) {
                        head = new KTitle();
                    }
                    Elements webmElements = element.getElementsByAttributeValueContaining("onclick", "expandWebm");
                    if (webmElements != null && webmElements.size() > 0) {
                        head.setVideoUrl(element.attr("href"));
                        head.setHasVideo(true);
                        head.addPostImage(new KPostImage(element.getElementsByTag("img").attr("src")));
                    } else {
                        head.addPostImage(new KPostImage(element.attr("href"), element.text()));
                    }
                    head.setHasImage(true);
                }
            }
            if ("input".equals(element.tagName())) {
                String idName = element.attr("name");
                if (null == head) {
                    head = new KTitle();
                }
                head.setId(idName);
            }
            if (head != null && "font".equals(element.tagName())) {
                String size = element.attr("size");
                if (size != null && !size.isEmpty()) {
                    head.setTitle(element.text());
                } else {
                    if (element.children().size() > 0) {
                        head.setName(element.text());
                    } else {
                        head.setWarnText(element.text());
                    }
                }
            }
            if ("blockquote".equals(element.tagName())) {
                head.setQuote(element.html());
            }
            if (head != null && "table".equals(element.tagName())) {
                // add reply to head
                KReply reply = new KReply();
                String idName = element.getElementsByTag("input").attr("name");
                if ("mode".equals(idName)) {
                    break;
                }
                reply.setId(idName);
                Elements titleName = element.select("font");
                reply.setTitle(titleName.get(0).text());
                reply.setName(titleName.get(1).text());
                reply.setQuote(element.getElementsByTag("blockquote").html());
                if (element.getElementsByTag("a").size() > 1) {
                    reply.addPostImage(new KPostImage(element.getElementsByTag("a").get(1).attr("href"), element.getElementsByTag("a").get(1).text()));
                    reply.setHasImage(true);
                }
                replyList.add(reply);
            }
            if (element.hasClass("spacer")) {
                // end post with reply and done one item
                head.setReplyList(replyList);
                headList.add(head);
                head = new KTitle();
                replyList = new ArrayList<KReply>();
            }
        }
        return headList;
    }
}
