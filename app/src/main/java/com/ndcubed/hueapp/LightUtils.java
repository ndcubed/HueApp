package com.ndcubed.hueapp;

import android.graphics.Color;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Nathan on 7/21/2015.
 */
public class LightUtils {

    public static void updateLightStateForDefaultGroup(PHHueSDK sdk, int brightness) {
        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setBrightness(brightness);
        lightState.setOn(brightness > 1);

        bridge.setLightStateForDefaultGroup(lightState);
    }

    public static void updateLightState(PHHueSDK sdk, PHGroup group, int color, int brightness) {

        PHBridge bridge = sdk.getSelectedBridge();
        PHLightState lightState = new PHLightState();
        float[] xy = PHUtilities.calculateXY(color, bridge.getResourceCache().getAllLights().get(0).getModelNumber());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        lightState.setBrightness(brightness);
        lightState.setOn(brightness > 1);

        bridge.setLightStateForGroup(group.getIdentifier(), lightState);
    }

    public static void updateLightColor(PHHueSDK sdk, PHGroup group, int color) {


        PHBridge bridge = sdk.getSelectedBridge();
        PHLightState lightState = new PHLightState();
        float[] xy = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), bridge.getResourceCache().getAllLights().get(0).getModelNumber());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);

        bridge.setLightStateForGroup(group.getIdentifier(), lightState);
    }

    public static void updateLightState(PHHueSDK sdk, PHGroup group, int brightness) {

        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setBrightness(brightness);
        lightState.setOn(brightness > 1);

        bridge.setLightStateForGroup(group.getIdentifier(), lightState);
    }

    public static void updateLightState(PHHueSDK sdk, PHLight light, int color, int brightness) {

        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setBrightness(brightness);
        lightState.setOn(brightness > 1);

        float[] xy = PHUtilities.calculateXYFromRGB(Color.red(color), Color.green(color), Color.blue(color), bridge.getResourceCache().getAllLights().get(0).getModelNumber());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);

        bridge.updateLightState(light, lightState, null);
    }


    public static List<PHLight> getAllLights(PHHueSDK sdk) {

        PHBridgeResourcesCache cache = sdk.getSelectedBridge().getResourceCache();
        return cache.getAllLights();
    }

    public static String createSceneIdentifier() {

        Random r = new Random();
        long rand = r.nextLong();

        String id = "nd" + Long.toString(System.currentTimeMillis() / 1000);

        System.out.println("HUE ID: " + id);

        id = id.substring(0, Math.min(id.length(), 16));

        return id;
    }

    public static ArrayList<Float> getRGBtoXY(int color) {
        // For the hue bulb the corners of the triangle are:
        // -Red: 0.675, 0.322
        // -Green: 0.4091, 0.518
        // -Blue: 0.167, 0.04
        double[] normalizedToOne = new double[3];
        float cred, cgreen, cblue;
        cred = Color.red(color);
        cblue = Color.blue(color);
        cgreen = Color.green(color);
        normalizedToOne[0] = (cred / 255);
        normalizedToOne[1] = (cgreen / 255);
        normalizedToOne[2] = (cblue / 255);
        float red, green, blue;

        // Make red more vivid
        if (normalizedToOne[0] > 0.04045) {
            red = (float) Math.pow(
                    (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
        } else {
            red = (float) (normalizedToOne[0] / 12.92);
        }

        // Make green more vivid
        if (normalizedToOne[1] > 0.04045) {
            green = (float) Math.pow((normalizedToOne[1] + 0.055)
                    / (1.0 + 0.055), 2.4);
        } else {
            green = (float) (normalizedToOne[1] / 12.92);
        }

        // Make blue more vivid
        if (normalizedToOne[2] > 0.04045) {
            blue = (float) Math.pow((normalizedToOne[2] + 0.055)
                    / (1.0 + 0.055), 2.4);
        } else {
            blue = (float) (normalizedToOne[2] / 12.92);
        }

        float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
        float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
        float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

        float x = X / (X + Y + Z);
        float y = Y / (X + Y + Z);

        double[] xy = new double[2];
        xy[0] = x;
        xy[1] = y;

        ArrayList<Float>  xyList = new ArrayList<Float>();
        xyList.add(x);
        xyList.add(y);

        return xyList;
    }
}
