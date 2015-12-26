package com.jagdev.locationprovidermanager.event;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by EndiIrawan on 12/26/15.
 */
public class LocationChangeListener implements LocationListener {
    private static final String TAG = LocationChangeListener.class.getSimpleName();
    private Context context;
    private Location lastKnownLocation;

    public LocationChangeListener(Context context) {
        super();
        this.context = context;
    }

    public Location getMyLastKnownLocation() {
        return lastKnownLocation;
    }

    // private void broadcastUpdateLocation(Context context, Location location) {
    // Log.d(TAG, String.format("Location Changed > Lat : %s | Lon : %s ", location.getLatitude(), location.getLongitude()));
    // lastKnownLocation = location;
    //
    // // BROADCAST UPDATE TO LOCAL BROADCAST
    // Intent intent = new Intent(MeetService.BROADCAST_MY_LOCATION_UPDATE);
    // intent.putExtra(PacketConstant.PD_C_LAT, location.getLatitude());
    // intent.putExtra(PacketConstant.PD_C_LON, location.getLongitude());
    // intent.putExtra("speed", location.getSpeed());
    // intent.putExtra("time", location.getTime());
    // LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    // // SEND UPDATE TO SERVER
    // MeetManager.getInstance().sendUpdateLocation(context, location.getLatitude(), location.getLongitude(), location.getTime(), location.getSpeed());
    // }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, String.format("Status of Location Provider %s has changed, status : %s", provider, status));
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, String.format("Provider %s enabled", provider));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, String.format("Provider %s disabled", provider));
    }

    @Override
    public void onLocationChanged(Location location) {
        // broadcastUpdateLocation(this.context, location);
    }
}
