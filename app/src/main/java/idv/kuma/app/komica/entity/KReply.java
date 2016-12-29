package idv.kuma.app.komica.entity;

import org.jsoup.nodes.Element;

/**
 * Created by TakumaLee on 2016/12/11.
 */

public class KReply extends KPost {

    /**
     * <a href="pixmicat.php?res=2212840&amp;page_num=all#r2212847" class="qlink">No.2212847</a>
     *
     *
     * <Another>
     *     <div class="quote">
     *         <span class="resquote">
     *             <a class="qlink" href="#r2212849" onclick="replyhl(2212849);">&gt;&gt;No.2212849</a>
     *         </span>
     *         <br />結婚後的厚志先生看起來有夠像強姦魔J
     *      </div>
     *
     * */
    private String qlinkUrl;
    private String linker;

    public KReply() {
    }

    public KReply(Element element, String domainUrl) {
        super(element, "reply", domainUrl);
        setQlinkUrl(element.getElementsByClass("qlink").attr("href"));
        setLinker(element.getElementsByClass("qlink").text());
    }

    public String getQlinkUrl() {
        return qlinkUrl;
    }

    public void setQlinkUrl(String qlinkUrl) {
        this.qlinkUrl = qlinkUrl;
    }

    public String getLinker() {
        return linker;
    }

    public void setLinker(String linker) {
        this.linker = linker;
    }
}
