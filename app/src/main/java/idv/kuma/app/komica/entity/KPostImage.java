package idv.kuma.app.komica.entity;

/**
 * Created by TakumaLee on 2017/1/16.
 */

public class KPostImage {
    protected String imageUrl;
    protected String imageFileName;

    public KPostImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public KPostImage(String imageUrl, String imageFileName) {
        this.imageUrl = imageUrl;
        this.imageFileName = imageFileName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
