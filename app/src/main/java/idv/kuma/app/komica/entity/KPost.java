package idv.kuma.app.komica.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import idv.kuma.app.komica.utils.KLog;

/**
 * Created by TakumaLee on 2016/12/11.
 */

public class KPost {
    private static final String TAG = KPost.class.getSimpleName();

    /**
     * id="r2212869"
     * <label for="2212869">
     *      <span class="title">Helck 84(後)</span>Name
     *      <span class="name"></span> [16/12/20(火)02:40 ID:BVpAKriM]
     *  </label>
     * */
    protected String id;
    protected String idStr;
    protected String title;
    protected String name;
    protected String time;

    /**
     * <div class="quote">作者不是蘿莉控嗎 結婚的都沒有幼女體型的</div>
     * <div class="quote">本文なし</div>
     *
     * <div class="quote">結束,啊戰爭部份??
     * <br />那不重要啦XDD
     * </div>
     *
     * */
    protected String quote;

    protected boolean hasImage = false;
    protected String videoUrl;
    protected String videoFileName;
    protected boolean hasVideo = false;

    /**
     * Pixel
     * style="width: 153px; height: 240px;"
     * class="img" alt="20 KB" title="20 KB"
     * [file name] - (20 KB, 153x240)
     * */
    protected List<KPostImage> postImageList = new ArrayList<>();
    protected String thumbUrl;
    protected String thumbName;
    protected int thumbWidth;
    protected int thumbHeight;

    public KPost() {

    }

    public KPost(Element element, String postType, String domainUrl) {
        if (!element.getElementsByClass("post-head").isEmpty()) {
            Element postHead = element.getElementsByClass("post-head").first();
            setTitle(postHead.getElementsByClass("title").text());
            setName(postHead.getElementsByClass("name").text());
            setTime(postHead.getElementsByClass("now").text());
            setIdStr(postHead.getElementsByClass("id").attr("data-id"));
            setId(postHead.getElementsByClass("qlink").attr("data-no"));
            Element quoteElem = element.getElementsByClass("quote").first();
            String quote = quoteElem.outerHtml();
            if (!quoteElem.getElementsByClass("-expand-line").isEmpty()) {
                element.getElementsByClass("quote").first().removeClass("-expand-line");
            }
            if (!element.getElementsByClass("backquote").isEmpty()) {
                quote += "<br>" + element.getElementsByClass("backquote").outerHtml();
            }
            quote.replaceAll("<span class=\"hide\">(.*?)</span>", "$1");
            KLog.v(TAG, quote);
            setQuote(quote);
            if (!element.getElementsByClass("file-text").isEmpty()) {
                setHasImage(true);
                setThumbName(element.getElementsByClass("file-text").first().getElementsByTag("a").text());
                setThumbUrl("http:" + element.getElementsByClass("file-text").first().getElementsByTag("a").attr("href"));
                postImageList.add(
                        new KPostImage("http:" + element.getElementsByClass("file-text").first().getElementsByTag("a").attr("href")
                                , element.getElementsByClass("file-text").first().getElementsByTag("a").text()));
            }
        } else {
            String inputId = element.getElementsByTag("input").attr("id");
            if ("".equals(inputId) || inputId == null) {
                inputId = element.getElementsByAttributeValue("type", "checkbox").attr("name");
            }
            setId(inputId);
            if (!element.getElementsByClass(postType).isEmpty()) {
                setIdStr(element.getElementsByClass(postType).attr("id"));
            }
            Element label = element.getElementsByTag("label").first();
            if (label == null) {
                setTitle(element.getElementsByClass("title").text());
                setName(element.getElementsByClass("name").text());
            } else {
                setTitle(label.getElementsByClass("title").text());
                setName(label.getElementsByClass("name").text());// loss the time date
            }
            Element quoteElement = element.getElementsByClass("quote").first();
            if (!quoteElement.getElementsByTag("img").isEmpty()) {
                for (Element imgElem : quoteElement.getElementsByTag("img")) {
                    postImageList.add(new KPostImage(imgElem.attr("src")));
                    imgElem.remove();
                }
            }
            Elements moeVideo = quoteElement.getElementsByTag("moe-video");
            if (!moeVideo.isEmpty()) {
                setHasVideo(true);
                Pattern pattern = Pattern.compile("=.*?(\\[.*?\\]);");
                Matcher matcher = pattern.matcher(quoteElement.html());
                if (matcher.find()) {
                    try {
                        JSONArray array = new JSONArray(matcher.group(1));
                        JSONObject object = array.getJSONObject(0);
                        setVideoUrl(object.getString("url"));
                        setVideoFileName(object.getString("title"));
                        String thumbUrl = object.getString("thumb");
                        if (!thumbUrl.startsWith("http")) {
                            thumbUrl = "http:" + thumbUrl;
                        }
                        setThumbUrl(thumbUrl);
                        postImageList.add(new KPostImage(object.getString("thumb")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                quoteElement.getElementsByTag("moe-video").first().remove();
                quoteElement.getElementsByTag("script").first().remove();
            }
            setQuote(quoteElement.html());
            Element thumbElement = element.getElementsByTag("img").first();
            Elements imgElements = element.getElementsByTag("img");
            setHasImage(thumbElement != null);
            if (hasImage) {
                Element imgElement = element.getElementsByAttributeValue("rel", "_blank").first();
                if (imgElement == null) {
                    imgElement = element.getElementsByAttributeValue("target", "_blank").first();
                }
                if (imgElement != null) {
                    String link = imgElement.attr("href");
                    if (!link.startsWith("http")) {
                        if (link.contains("..") && !imgElement.getElementsByTag("img").isEmpty()) {
                            link = imgElement.getElementsByTag("img").attr("src");
//                            link = domainUrl + link.replaceAll("(.*?old/\\.\\.\\/src)", "src");
//                            if (link.contains("2nyan")) {
//                                link = link.replaceAll("2nyan", "img.2nyan");
//                            }
                        }
                    }
                    postImageList.add(new KPostImage(link, imgElement.text()));
                } else {
                    if (!element.getElementsByClass("img_container").isEmpty()) {
                        String hideImgUrl = element.getElementsByClass("img_container").first().getElementsByTag("img").attr("title");
                        String showImgUrl = element.getElementsByClass("img_container").first().getElementsByTag("img").attr("src");
                        postImageList.add(new KPostImage(showImgUrl, hideImgUrl, "Hide"));
                    }
                }
                if (imgElements.size() > 1) {
                    for (Element img : imgElements) {
                        postImageList.add(new KPostImage(img.attr("src")));
                    }
                } else {
                    setThumbUrl(thumbElement.attr("src"));
                    setThumbWidth(findWidthPixel(thumbElement.attr("style")));
                    setThumbHeight(findHeightPixel(thumbElement.attr("style")));
                }
            }
            setHasImage(postImageList.size() > 0);
        }
    }

    private int findWidthPixel(String source) {
        Pattern pattern = Pattern.compile("width: (.*?)px;");
        return matcherPattern(pattern, source);
    }

    private int findHeightPixel(String source) {
        Pattern pattern = Pattern.compile("height: (.*?)px;");
        return matcherPattern(pattern, source);
    }

    private int matcherPattern(Pattern pattern, String source) {
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getThumbName() {
        return thumbName;
    }

    public void setThumbName(String thumbName) {
        this.thumbName = thumbName;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public void addPostImage(KPostImage postImage) {
        postImageList.add(postImage);
    }

    public List<KPostImage> getPostImageList() {
        return postImageList;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    public boolean hasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }
}
