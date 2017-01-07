package idv.kuma.app.komica.entity;

/**
 * Created by TakumaLee on 2017/1/7.
 */

public class Promotion {
    private String linkUrl;
    private String imageUrl;
    private String title;

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
