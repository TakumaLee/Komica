package idv.kuma.app.komica.entity;

/**
 * Created by TakumaLee on 2017/1/16.
 */

public class KPostImage {
    protected String imageUrl;
    protected String hideImgUrl;
    protected String imageFileName;

    public KPostImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public KPostImage(String imageUrl, String imageFileName) {
        this(imageUrl, null, imageFileName);
    }

    public KPostImage(String imageUrl, String hideImgUrl, String imageFileName) {
        if (!imageUrl.startsWith("http")) {
            imageUrl = "http:" + imageUrl;
        }
        if (hideImgUrl != null && !hideImgUrl.startsWith("http")) {
            hideImgUrl = "http:" + hideImgUrl;
        }
        this.imageUrl = imageUrl;
        this.hideImgUrl = hideImgUrl;
        this.imageFileName = imageFileName;
    }

    public boolean isHide() {
        return hideImgUrl == null;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHideImgUrl() {
        return hideImgUrl;
    }

    public void setHideImgUrl(String hideImgUrl) {
        this.hideImgUrl = hideImgUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
