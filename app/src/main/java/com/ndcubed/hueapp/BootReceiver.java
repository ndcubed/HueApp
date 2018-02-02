package com.ndcubed.hueapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.ndcubed.hueapp.service.BackgroundLightService;
import com.ndcubed.hueapp.service.GeofenceTransitionsIntentService;

import java.util.ArrayList;

/**
 * Created by Nathan on 12/17/2016.
 */

public class BootReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Context context;
    PendingIntent geofencePendingIntent;
    ArrayList<Geofence> geofenceList = new ArrayList<>();

    GoogleApiClient googleApiClient;

    boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            ConnectivityManager conMngr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conMngr.getActiveNetworkInfo();

            SharedPreferences prefs = context.getSharedPreferences(Common.GEOFENCE_PREFERENCES, Context.MODE_PRIVATE);
            isConnected = prefs.getBoolean("isConnected", false);
            boolean didLeaveHome = prefs.getBoolean("didLeaveHome", false);

            if(networkInfo != null && didLeaveHome) {
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if(networkInfo.isConnected() && !isConnected) {
                        isConnected = true;
                        System.out.println("WIFI CONNECTED!");
                        context.startService(new Intent(context, BackgroundLightService.class));
                    }
                } else {
                    isConnected = false;
                }
            } else {
                isConnected = false;
            }

            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean("isConnected", isConnected);
            e.commit();
        }

        if(intent.getAction().equals("android.location.MODE_CHANGED") || intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            System.out.println("EVENT RECEIVED " + intent.getAction());

            SharedPreferences huePreferences = Common.getPreferences(context);
            if(huePreferences.getBoolean("locationEnabled", false)) {
                /** ADD GEOFENCE TO LIST **/
                SharedPreferences prefs = context.getSharedPreferences(Common.GEOFENCE_PREFERENCES, Context.MODE_PRIVATE);
                double lat = Double.parseDouble(prefs.getString("geoLat", "0"));
                double lon = Double.parseDouble(prefs.getString("geoLon", "0"));
                float radius = prefs.getFloat("geoRadius", 100f);

                if(radius != 0) {
                    geofenceList.clear();
                    geofenceList.add(new Geofence.Builder()
                            .setCircularRegion(lat, lon, radius)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setRequestId("ndcubedHomeGeofence")
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()
                    );

                    /** CONNECT TO GOOGLE API CLIENT **/
                    googleApiClient = new GoogleApiClient.Builder(context)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                    googleApiClient.connect();
                }
            }
        }

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {

        if(geofencePendingIntent != null) {
            return geofencePendingIntent;
        } else {
            Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("RE_ADDED_GEOFENCE!");
            LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
