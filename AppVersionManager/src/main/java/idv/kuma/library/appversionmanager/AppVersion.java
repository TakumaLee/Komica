package idv.kuma.library.appversionmanager;

import java.util.HashMap;

public class AppVersion {

    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String BUILD = "build";

    private HashMap<String, Integer> currentVersion;
    private String updateSwitch;
    private String updateLink;
    private int updateVersionCode;
    private boolean isForceUpdate = false;

    public AppVersion() {
        currentVersion = new HashMap<>();
        currentVersion.put(MAJOR, 0);
        currentVersion.put(MINOR, 0);
        currentVersion.put(BUILD, 0);
    }

    public boolean hasNewVersion() {
        HashMap<String, Integer> updateVersionMap = parseVersion(getUpdateSwitch());
        if (updateVersionMap.get(MAJOR) > currentVersion.get(MAJOR)) {
            return true;
        } else if (updateVersionMap.get(MAJOR) < currentVersion.get(MAJOR)) {
            return false;
        }

        if (updateVersionMap.get(MINOR) > currentVersion.get(MINOR)) {
            return true;
        } else if (updateVersionMap.get(MINOR) < currentVersion.get(MINOR)) {
            return false;
        }

        if (updateVersionMap.get(BUILD) > currentVersion.get(BUILD)) {
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = parseVersion(currentVersion);
    }

    private HashMap<String, Integer> parseVersion(String version) {
        String[] s = version.split("\\.");
        HashMap<String, Integer> mapVersion = new HashMap<>();
        mapVersion.put(MAJOR, Integer.parseInt(s[0]));
        mapVersion.put(MINOR, Integer.parseInt(s[1]));
        mapVersion.put(BUILD, Integer.parseInt(s[2]));
        return mapVersion;
    }

    public String getUpdateSwitch() {
        return updateSwitch;
    }

    public void setUpdateSwitch(String updateSwitch) {
        this.updateSwitch = updateSwitch;
    }

    public String getUpdateLink() {
        return updateLink;
    }

    public void setUpdateLink(String updateLink) {
        this.updateLink = updateLink;
    }

    public int getUpdateVersionCode() {
        return updateVersionCode;
    }

    public void setUpdateVersionCode(int updateVersionCode) {
        this.updateVersionCode = updateVersionCode;
    }

    public boolean isForceUpdate() {
        return isForceUpdate;
    }

    public void setForceUpdate(boolean isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }

}
