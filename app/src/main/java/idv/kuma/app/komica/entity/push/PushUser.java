package idv.kuma.app.komica.entity.push;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TakumaLee on 2016/7/31.
 */
public class PushUser {
    private static final String TAG = PushUser.class.getSimpleName();

    public static final String DATABASE_USERS = "database_users";

    private List<PushDevice> deviceList;

    private String adId;
    private String fbId;
    private String name;
    private int age;
    private int gender = 1;
    private int points = 0;

    public PushUser() {
        deviceList = new ArrayList<>();
    }

    public PushUser(List<PushDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public void addDeivce(PushDevice device) {
        deviceList.add(device);
    }

    public List<PushDevice> getDeviceList() {
        return deviceList;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
