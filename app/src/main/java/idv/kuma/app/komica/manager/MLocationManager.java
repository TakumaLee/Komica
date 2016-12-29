package idv.kuma.app.komica.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationListener;

/**
 * Created by TakumaLee on 15/2/24.
 */
public class MLocationManager implements LocationListener {
    private static final String TAG = MLocationManager.class.getSimpleName();

    //    private LocationManager locationManager;
    private Location location;
    private static MLocationManager instance = null;

    public static void initSingleton(Context context) {
        if (null == instance)
            instance = new MLocationManager(context.getApplicationContext());
    }

    public MLocationManager(Context context) {
//        if ( ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
//
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION);
//        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    public static MLocationManager getInstance() {
        return instance;
    }

    public double getLatitude() {
        if (location == null)
            return 0;
        return location.getLatitude();
    }

    public double getLongititude() {
        if (location == null)
            return 0;
        return location.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
