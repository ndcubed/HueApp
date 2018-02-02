package com.ndcubed.hueapp.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.ndcubed.hueapp.Common;
import com.ndcubed.hueapp.LightUtils;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLightState;

import java.util.List;

/**
 * Created by Nathan on 12/18/2016.
 */

public class BackgroundLightService extends IntentService implements PHSDKListener {

    String username, ip;
    PHHueSDK sdk;

    public BackgroundLightService(String name) {
        super(name);
    }

    public BackgroundLightService() {
        super(".service.BackgroundLightService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences geoPrefs = getSharedPreferences(Common.GEOFENCE_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences prefs = Common.getPreferences(this);
        boolean didLeaveHome = geoPrefs.getBoolean("didLeaveHome", false);

        /** ARRIVED HOME TURN ON LIGHTS **/
        if(didLeaveHome) {
            sdk = PHHueSDK.getInstance();
            sdk.setDeviceName(Build.MODEL);
            sdk.getNotificationManager().registerSDKListener(this);

            if(prefs.getBoolean("usernameSet", false)) {
                username = prefs.getString("username", "default");
                ip = prefs.getString("ip", "0");

                System.out.println("CONNECT: " + ip + "  " + username);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception err) {
                            err.printStackTrace();
                        }
                        //connect
                        PHAccessPoint accessPoint = new PHAccessPoint();
                        accessPoint.setIpAddress(ip);
                        accessPoint.setUsername(username);
                        sdk.connect(accessPoint);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

    }

    @Override
    public void onBridgeConnected(PHBridge phBridge, String s) {

        sdk.setSelectedBridge(phBridge);
        sdk.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);

        System.out.println("BRIDGE CONNECTED DO LIGHT TASK!");

        SharedPreferences geoPrefs = getSharedPreferences(Common.GEOFENCE_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor e = geoPrefs.edit();
        e.putBoolean("didLeaveHome", false);
        e.apply();

        SharedPreferences prefs = Common.getPreferences(this);
        System.out.println("GOOOOOOO");
        if(!prefs.getBoolean("locationUseScene", false)) {

            int color = prefs.getInt("locationGroupsColor", Common.NATURAL_LIGHT_COLOR);
            int brightness = prefs.getInt("locationBrightness", Common.NATURAL_LIGHT_COLOR);
            String groupID = prefs.getString("locationGroupID", "0");

            System.out.println(groupID + "  " + brightness +  " " + color + "<------------");

            List<PHGroup> groups = phBridge.getResourceCache().getAllGroups();
            for(PHGroup group : groups) {
                if(group.getIdentifier().equals(groupID)) {
                    System.out.println("TURN ON LIGHTS");
                    LightUtils.updateLightState(sdk, group, color, brightness);
                    break;
                }
            }
        } else {
            int color = prefs.getInt("locationGroupColor", Common.NATURAL_LIGHT_COLOR);
            int brightness = prefs.getInt("locationBrightness", Common.NATURAL_LIGHT_COLOR);
            String groupID = prefs.getString("locationGroupID", "0");
            System.out.println(groupID + "  " + brightness +  " " + color + "<------------");
        }
        System.out.println("DDDDDDDDDD");
        sdk.disableAllHeartbeat();
        sdk.disconnect(phBridge);
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {

    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {

    }

    @Override
    public void onError(int i, String s) {

    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {

    }

    @Override
    public void onConnectionLost(PHAccessPoint phAccessPoint) {

    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {

    }
}
