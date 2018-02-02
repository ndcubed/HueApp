package com.ndcubed.hueapp;

import android.Manifest;
import android.app.Fragment;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ndcubed.hueapp.service.GeofenceTransitionsIntentService;
import com.ndcubed.nappsupport.views.ColorDot;
import com.ndcubed.nappsupport.views.RoundedSwitch;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHScene;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nathan on 12/16/2016.
 */

public class LocationFragment extends Fragment implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final int COLOR_CHOOSE_INTENT = 0;
    final int REQUEST_LOCATION_PERMISSION = 1;
    final int LOCATION_REQUEST = 2;
    final String GEOFENCE_ID = "ndcubedHomeGeofence";

    View rootView;
    Spinner groupSpinner;
    ArrayAdapter<String> groupArrayAdapter;

    TextView lightLabel;

    PHGroup selectedGroup;
    PHHueSDK sdk;
    PHBridge bridge;
    List<PHGroup> groups;
    List<PHScene> scenes;

    boolean locationEnabled = false;
    boolean useScene = false;
    int color = 0;
    String groupID = "";
    String sceneID = "";
    String sceneName = "";
    int brightness = 254;

    RadioButton sceneRadioButton, groupRadioButton;
    SeekBar brightnessBar;

    GoogleApiClient googleApiClient;

    PendingIntent geofencePendingIntent;
    ArrayList<Geofence> geofenceList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.location_fragment_layout, container, false);

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        SharedPreferences prefs = Common.getPreferences(getActivity());
        color = prefs.getInt("locationGroupsColor", Common.NATURAL_LIGHT_COLOR);
        groupID = prefs.getString("locationGroupID", "-1");
        sceneID = prefs.getString("locationSceneID", "-1");
        useScene = prefs.getBoolean("locationUseScene", false);
        brightness = prefs.getInt("locationBrightness", 254);
        locationEnabled = prefs.getBoolean("locationEnabled", false);

        sdk = PHHueSDK.getInstance();
        bridge = sdk.getSelectedBridge();
        groups = bridge.getResourceCache().getAllGroups();
        scenes = bridge.getResourceCache().getAllScenes();

        ((RoundedSwitch)rootView.findViewById(R.id.locationSwitch)).setRoundedSwitchListener(new RoundedSwitch.RoundedSwitchListener() {
            @Override
            public void onPress() {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onStateChange(int switchState) {
                /*
                                if(switchState == RoundedSwitch.STATE_ON) {
                    rootView.findViewById(R.id.disabledOverlay).setVisibility(View.GONE);
                    checkPermissions();
                } else {
                    ArrayList<String> geofenceList = new ArrayList<>();
                    geofenceList.add(GEOFENCE_ID);

                    rootView.findViewById(R.id.disabledOverlay).setVisibility(View.VISIBLE);
                    LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeofencePendingIntent());
                    LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceList);
                }

                locationEnabled = (switchState == RoundedSwitch.STATE_ON);
                 */

                locationEnabled = false;
                //save();
            }
        });
        ((RoundedSwitch)rootView.findViewById(R.id.locationSwitch)).setSwitchState(locationEnabled ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        rootView.findViewById(R.id.disabledOverlay).setVisibility(locationEnabled ? View.GONE : View.VISIBLE);

        brightnessBar = (SeekBar)rootView.findViewById(R.id.brightnessBar);
        groupRadioButton = (RadioButton)rootView.findViewById(R.id.groupRadioButton);
        sceneRadioButton = (RadioButton)rootView.findViewById(R.id.sceneRadioButton);
        ((ColorDot)rootView.findViewById(R.id.colorDot)).setDotColor(color);

        groupSpinner = (Spinner)rootView.findViewById(R.id.groupSpinner);
        groupArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dark_spinner_item);
        groupArrayAdapter.setDropDownViewResource(R.layout.simple_dropdown_spinner_item);

        if(groupID.equals("-1")) {
            groupID = groups.get(0).getIdentifier();
        }
        if(sceneID.equals("-1")) {
            sceneID = scenes.get(0).getSceneIdentifier();
        }

        for(PHGroup group : groups) {
            if(!useScene) groupArrayAdapter.add(group.getName());
            if(groupID.equals(group.getIdentifier())) {
                selectedGroup = group;
            }
        }
        for(PHScene scene : scenes) {
            if(useScene) groupArrayAdapter.add(scene.getName());
            if(scene.getSceneIdentifier().equals(sceneID)) {
                sceneName = scene.getName();
            }
        }

        groupSpinner.setAdapter(groupArrayAdapter);
        groupArrayAdapter.notifyDataSetChanged();
        groupSpinner.setOnItemSelectedListener(this);

        lightLabel = (TextView)rootView.findViewById(R.id.lightLabel);
        lightLabel.setText("All Lights");

        ((ColorDot)rootView.findViewById(R.id.colorDot)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ColorChooseActivity.class);

                intent.putExtra(ColorChooseActivity.INTENT_SET_GROUP_FLAG_KEY, true);
                intent.putExtra(ColorChooseActivity.INTENT_SET_GROUP_KEY, selectedGroup.getIdentifier());
                intent.putExtra(ColorChooseActivity.INTENT_SET_COLOR_KEY, color);
                startActivityForResult(intent, COLOR_CHOOSE_INTENT);
            }
        });

        ((SeekBar)rootView.findViewById(R.id.brightnessBar)).setProgress(brightness);
        ((SeekBar)rootView.findViewById(R.id.brightnessBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                brightness = seekBar.getProgress();

                PHLightState lightState = new PHLightState();
                lightState.setBrightness(brightness);
                lightState.setOn(brightness > 1);

                bridge.setLightStateForGroup(selectedGroup.getIdentifier(), lightState);

                save();
            }
        });

        groupRadioButton.setOnCheckedChangeListener(this);
        sceneRadioButton.setOnCheckedChangeListener(this);

        if(useScene) {
            sceneRadioButton.setChecked(true);
        } else {
            groupRadioButton.setChecked(true);
        }
        System.out.println("COLOR: " + color);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == COLOR_CHOOSE_INTENT && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            color = extras.getInt("color", 0);

            ((ColorDot)rootView.findViewById(R.id.colorDot)).setDotColor(color);
            save();
        } else if(requestCode == LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                final LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setExpirationDuration(30000);

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, LocationFragment.this);
            }
        }
    }

    public void save() {
        System.out.println("COLOR: " + color);
        color = ((ColorDot)rootView.findViewById(R.id.colorDot)).getDotColor();
        groupID = selectedGroup.getIdentifier();

        SharedPreferences prefs = Common.getPreferences(getActivity());
        SharedPreferences.Editor e = prefs.edit();

        e.putBoolean("locationUseScene", useScene);
        e.putInt("locationGroupsColor", color);
        e.putString("locationGroupID", groupID);
        e.putString("locationSceneID", sceneID);
        e.putInt("locationBrightness", brightness);
        e.putBoolean("locationEnabled", locationEnabled);

        e.apply();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(!useScene) {
            lightLabel.setText(groupArrayAdapter.getItem(i));

            for(PHGroup group : groups) {
                if(group.getName().equals(lightLabel.getText().toString())) {
                    selectedGroup = group;
                    groupID = selectedGroup.getIdentifier();
                    ((TextView)rootView.findViewById(R.id.lightLabel)).setText(selectedGroup.getName());
                    break;
                }
            }
        } else {
            for(PHScene scene : scenes) {
                if(scene.getName().equals(groupArrayAdapter.getItem(i))) {
                    sceneID = scene.getSceneIdentifier();
                    sceneName = scene.getName();
                }
            }
        }

        save();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if(compoundButton.getId() == R.id.groupRadioButton) {
            if(b) {
                useScene = false;

                groupArrayAdapter.clear();
                for(PHGroup group : groups) {
                    groupArrayAdapter.add(group.getName());
                }

                groupSpinner.setSelection(groupArrayAdapter.getPosition(selectedGroup.getName()));

                ((TextView)rootView.findViewById(R.id.groupSceneHeader)).setText("Choose a Group");
                rootView.findViewById(R.id.groupControlsLayout).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.groupControlsHeader).setVisibility(View.VISIBLE);

                groupArrayAdapter.notifyDataSetChanged();

                save();
            }
        } else if(compoundButton.getId() == R.id.sceneRadioButton) {
            if(b) {
                useScene = true;

                ((TextView)rootView.findViewById(R.id.groupSceneHeader)).setText("Choose a Scene");
                rootView.findViewById(R.id.groupControlsLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.groupControlsHeader).setVisibility(View.GONE);

                groupArrayAdapter.clear();
                for(PHScene scene : scenes) {
                    groupArrayAdapter.add(scene.getName());
                }

                groupSpinner.setSelection(groupArrayAdapter.getPosition(sceneName));

                groupArrayAdapter.notifyDataSetChanged();

                save();
            }
        }
    }

    public void checkPermissions() {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            } else {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ((RoundedSwitch)rootView.findViewById(R.id.locationSwitch)).setSwitchState(RoundedSwitch.STATE_OFF);
        }
    }



    public void getCurrentLocation() {
        System.out.println("GO CHECK");
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setExpirationDuration(20000);
        mLocationRequest.setSmallestDisplacement(50f);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        /** CHECK IF THE LOCATION REQUEST NEEDS ADDITIONAL SETTINGS CHANGED TO FUNCTION **/
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch(status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        System.out.println("SUCCESS");

                        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, LocationFragment.this);
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        System.out.println("RESOLVE!");
                        try {
                            status.startResolutionForResult(getActivity(), LOCATION_REQUEST);
                        } catch(IntentSender.SendIntentException err) {
                            err.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("CONNECTED!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("FAIL!");
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("LOCATION: " + location.getLatitude() + "  " + location.getLongitude());

        /** CREATE AND ADD GEOFENCE **/
        SharedPreferences prefs = getActivity().getSharedPreferences(Common.GEOFENCE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();

        e.putString("geoLat", Double.toString(location.getLatitude()));
        e.putString("geoLon", Double.toString(location.getLongitude()));
        e.putFloat("geoRadius", 100f);
        e.apply();

        geofenceList.clear();
        geofenceList.add(new Geofence.Builder()
                .setCircularRegion(location.getLatitude(), location.getLongitude(), 100f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setRequestId("ndcubedHomeGeofence")
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setNotificationResponsiveness(0)
                .build()
        );

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                }
            });
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
            Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
            return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}
