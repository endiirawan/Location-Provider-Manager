package com.jagdev.locationprovidermanager.event;

import android.location.Location;

/**
 * Created by EndiIrawan on 12/26/15.
 */
public interface ILocationProvider {
    void onStarted();

    void onCompleted();

    void onSuccess(Location location);

    void onFailed(int errorCode, String message);
}
