package com.jagdev.locationprovidermanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.jagdev.locationprovidermanager.event.ILocationProvider;
import com.jagdev.locationprovidermanager.event.LocationChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by EndiIrawan on 12/26/15.
 */
public class LocationProviderManager {
    public static final String TAG = LocationManager.class.getSimpleName();
    private static final int MINIMUM_TIME = 1000 * 30;// IN SECONDS
    private static final int MINIMUM_DISTANCE = 600;// IN METERS
    private static final int SEARCH_LOCATION_TIMEOUT = 60000;
    public static final int REQ_LOCATION_PERMISSION = 1;
    public static final int ERROR_LOCATION_SERVICE_STATE = 2;
    public static final int ERROR_LOCATION_PERMISSION = 3;
    public static final int ERROR_NO_RESULT = 4;
    public static final int ERROR_NO_PROVIDER = 5;

    private LocationManager mLocationManager;
    private LocationChangeListener mLocationChangeListener;
    private static LocationProviderManager INSTANCE;
    private Context mContext;

    public static LocationProviderManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocationProviderManager();
        }
        if (INSTANCE.getContext() == null) {
            throw new IllegalArgumentException();
        }
        return INSTANCE;
    }

    private LocationProviderManager() {
        this.mLocationChangeListener = new LocationChangeListener(getContext());
    }

    private LocationChangeListener getLocationChangeListener() {
        return mLocationChangeListener;
    }

    public static void Factory(Context context) {
        LocationProviderManager locMgr = new LocationProviderManager();
        LocationProviderManager.INSTANCE = locMgr;
        locMgr.mContext = context;
        locMgr.mLocationManager = (LocationManager) locMgr.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public Context getContext() {
        return mContext;
    }

    public Location getMyLastKnownLocation() {
        return mLocationChangeListener.getMyLastKnownLocation();
    }

    public boolean isPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= 23 && (
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission(Activity activity) {
        List<String> perms = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (perms.size() > 0) {
            ActivityCompat.requestPermissions(activity, (String[]) perms.toArray(), REQ_LOCATION_PERMISSION);
            return false;
        }
        return true;

    }


    /**
     * START LISTEN MY LOCATION CHANGE
     */
    public void listenMyLocation() {
        // Getting LocationManager object from System Service
        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = mLocationManager.getBestProvider(criteria, true);
        Log.d(TAG, "Start Monitoring Location Provider : " + provider);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(provider, MINIMUM_TIME, MINIMUM_DISTANCE, mLocationChangeListener);
        }
    }

    /**
     * STOP LISTEN MY LOCATION
     *
     * @param context
     */
    public void stopListenMyLocation(Context context) {
        // STOPPING LOCATION UPDATE
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (mLocationManager != null && mLocationChangeListener != null)
                mLocationManager.removeUpdates(mLocationChangeListener);
        }
    }

    private String getBestProvider() {
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // Getting the name of the best provider
        String provider = mLocationManager.getBestProvider(criteria, true);
        Log.d(TAG, "Best Location Provider is " + provider);
        return provider;
    }

    private boolean checkRequirement(final Context context) {
        return !(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Search My Location Right now once
     *
     * @param locationProvider
     */
    public void requestSingleUpdate(Activity activity, final ILocationProvider locationProvider) {
        if (locationProvider == null)
            return;

        if (!checkLocationPermission(activity)) {
            locationProvider.onFailed(ERROR_LOCATION_PERMISSION, "Location Access Permission not granted");
            return;
        }

        if (!checkRequirement(getContext())) {
            locationProvider.onFailed(ERROR_LOCATION_SERVICE_STATE, "Location Services is not active, please enable location services and GPS");
            return;
        }

        locationProvider.onStarted();
        final String provider = getBestProvider();
        if (provider == null) {
            Log.e(TAG, "Cannot find best Location Provider!");
            locationProvider.onFailed(ERROR_NO_PROVIDER, "Cannot find best Location Provider!");
        } else {
            final Timer timer = new Timer();
            final LocationListener locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "on status changed");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d(TAG, "on Provider enabled");

                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d(TAG, "on status disabled");
                }

                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "on location changed");
                    timer.cancel();
                    locationProvider.onSuccess(location);
                    locationProvider.onCompleted();
                }
            };

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mLocationManager.removeUpdates(locationListener);
                            }

                            Location lastLocation = mLocationManager.getLastKnownLocation(provider);
                            if (lastLocation != null) {
                                locationProvider.onSuccess(lastLocation);
                            } else {
                                locationProvider.onFailed(ERROR_NO_RESULT, "Your location currently not available!");
                            }
                            locationProvider.onCompleted();
                        }
                    });
                }
            };
            timer.schedule(task, SEARCH_LOCATION_TIMEOUT);

            mLocationManager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());
        }

    }
}
