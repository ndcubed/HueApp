package com.ndcubed.hueapp.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ndcubed.hueapp.Common;

/**
 * Created by Nathan on 12/17/2016.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super(".service.GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            System.out.println("ENTER GEO");
        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            SharedPreferences prefs = getSharedPreferences(Common.GEOFENCE_PREFERENCES, Activity.MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();

            e.putBoolean("didLeaveHome", true);
            e.commit();

            System.out.println("EXIT GEO");
        }
    }
}
