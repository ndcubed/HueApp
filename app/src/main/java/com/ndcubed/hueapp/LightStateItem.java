package com.ndcubed.hueapp;

import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;


public class LightStateItem {

    private PHLightState lightState;
    private PHLight light;

    public LightStateItem(PHLight light, PHLightState lightState) {
        this.light = light;
        this.lightState = lightState;
    }

    public PHLightState getLightState() {
        return lightState;
    }

    public void setLightState(PHLightState lightState) {
        this.lightState = lightState;
    }

    public PHLight getLight() {
        return light;
    }

    public void setLight(PHLight light) {
        this.light = light;
    }
}
