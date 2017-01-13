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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
