package com.AlForce.android.runvolution.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

/**
 * Created by iqbal on 21/02/18.
 */

public class LocationService implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = LocationService.class.getSimpleName();
    public static final long INTERVAL = 1000 * 10;
    public static final long FASTEST_INTERVAL = 1000 * 5;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;

    private Context context;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationServiceListener mListener;
    private Location mCurrentLocation;

    public LocationService(Context context) {
        this.context = context;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        buildGoogleClient();
        Log.d(TAG, "LocationService: Created.");
    }

    public void buildGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    public void setLocationServiceListener(LocationServiceListener listener) {
        this.mListener = listener;
    }

    public interface LocationServiceListener {
        public void onLocationChanged(Location location);
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GoogleApiAvailability.getInstance().showErrorNotification(context, status);
            return false;
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location.toString());
        mListener.onLocationChanged(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + mGoogleApiClient.isConnected());
        updateLocation();
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

        } else {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "updateLocation: Updating location.");
        }
    }

    public void startLocationUpdates() {
        buildGoogleClient();
        mGoogleApiClient.connect();
        Log.d(TAG, "startLocationUpdates: Location update started.");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult.toString());
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "stopLocationUpdates: Location update stopped.");
        mGoogleApiClient.disconnect();
    }
}
