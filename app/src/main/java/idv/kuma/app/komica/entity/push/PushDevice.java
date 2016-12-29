package idv.kuma.app.komica.entity.push;

import idv.kuma.app.komica.BuildConfig;

/**
 * Created by TakumaLee on 2016/8/10.
 */
public class PushDevice {
    private static final String TAG = PushUser.class.getSimpleName();

    public static class Device {
        public static final String IOS = "iOS";
        public static final String ANDROID = "android";
    }

    public static class Mode {
        public static final String DEBUG = "debug";
        public static final String RELEASE = "release";
    }

    private String device;
    private String mode;
    private String token;

    public PushDevice() {
    }

    public PushDevice(String token) {
        setDevice("android");
        setMode(BuildConfig.DEBUG ? "debug" : "android");
        setToken(token);
    }

    public PushDevice(String device, String mode, String token) {
        setDevice(device);
        setMode(mode);
        setToken(token);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
