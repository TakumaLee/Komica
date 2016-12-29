package idv.kuma.app.komica.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by TakumaLee on 2016/12/5.
 */

public class KomicaMenuMember implements Parcelable {
    private String linkUrl;
    private String title;
    private int memberId = 0;

    public KomicaMenuMember() {
    }

    protected KomicaMenuMember(Parcel in) {
        linkUrl = in.readString();
        title = in.readString();
        memberId = in.readInt();
    }

    public static final Creator<KomicaMenuMember> CREATOR = new Creator<KomicaMenuMember>() {
        @Override
        public KomicaMenuMember createFromParcel(Parcel in) {
            return new KomicaMenuMember(in);
        }

        @Override
        public KomicaMenuMember[] newArray(int size) {
            return new KomicaMenuMember[size];
        }
    };

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(linkUrl);
        parcel.writeString(title);
        parcel.writeInt(memberId);
    }
}
