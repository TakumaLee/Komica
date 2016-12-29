package idv.kuma.app.komica.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;

/**
 * Created by TakumaLee on 2016/12/5.
 */

public class KomicaMenuGroup implements Parcelable {
    private int groupId;
    private int groupPosition;
    private String title;
    private List<KomicaMenuMember> memberList = Collections.emptyList();

    public KomicaMenuGroup() {
    }

    protected KomicaMenuGroup(Parcel in) {
        groupId = in.readInt();
        groupPosition = in.readInt();
        title = in.readString();
    }

    public static final Creator<KomicaMenuGroup> CREATOR = new Creator<KomicaMenuGroup>() {
        @Override
        public KomicaMenuGroup createFromParcel(Parcel in) {
            return new KomicaMenuGroup(in);
        }

        @Override
        public KomicaMenuGroup[] newArray(int size) {
            return new KomicaMenuGroup[size];
        }
    };

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupPosition() {
        return groupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<KomicaMenuMember> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<KomicaMenuMember> memberList) {
        this.memberList = memberList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(groupId);
        parcel.writeInt(groupPosition);
        parcel.writeString(title);
    }
}
