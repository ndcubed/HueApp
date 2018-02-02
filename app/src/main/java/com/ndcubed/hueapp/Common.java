package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nathan on 7/20/2015.
 */
public class Common {

    public static final int NATURAL_LIGHT_COLOR = Color.rgb(254, 189, 145);
    public static boolean IS_BRIDGE_CONNECTED = false;
    public static String HUE_SHARED_PREFERENCES = "HueAppPreferences";
    public static final String GEOFENCE_PREFERENCES = "GeofencePreferences";

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(HUE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static float dpToPx(Context context, float dp){

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static int getAttributeColor(Context context, int attribute) {

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    public static void setLightBrightness(PHLight light, int brightness) {

        PHHueSDK sdk = PHHueSDK.getInstance();
        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setBrightness(brightness);

        bridge.updateLightState(light, lightState, null);
        sdk = null;
    }

    public static void flashLight(final PHLight light) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                PHHueSDK sdk = PHHueSDK.getInstance();
                PHBridge bridge = sdk.getSelectedBridge();

                PHLightState state = light.getLastKnownLightState();
                float lastX = state.getX();
                float lastY = state.getY();

                PHLightState lastState = new PHLightState();
                lastState.setTransitionTime(0);
                lastState.setBrightness(state.getBrightness());
                lastState.setOn(state.isOn());
                if(lastState.isOn()) {
                    lastState.setX(state.getX());
                    lastState.setY(state.getY());
                } else {
                    float[] xy = PHUtilities.calculateXYFromRGB(255, 255, 255, light.getModelNumber());
                    lastState.setX(xy[0]);
                    lastState.setY(xy[1]);
                }

                PHLightState flashState = new PHLightState();
                flashState.setBrightness(254, true);
                flashState.setTransitionTime(0);

                float[] xy = PHUtilities.calculateXYFromRGB(255, 255, 255, light.getModelNumber());
                flashState.setX(xy[0]);
                flashState.setY(xy[1]);

                boolean flash = true;
                for(int i = 0; i < 4; i++) {

                    bridge.updateLightState(light, flash ? flashState : lastState);
                    flash = !flash;

                    try {
                        Thread.sleep(500);
                    } catch(Exception err) {
                        err.printStackTrace();
                    }
                }

                lastState.setX(lastX);
                lastState.setY(lastY);
                bridge.updateLightState(light, lastState);
            }
        }).start();
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

    public static void showSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public static void showSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
