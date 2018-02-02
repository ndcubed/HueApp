package com.ndcubed.hueapp;

import com.philips.lighting.model.PHLight;


public class LightItem {

    private PHLight light;
    private boolean isSelected = false;

    public LightItem() {}

    public LightItem(PHLight light) {
        this.light = light;
    }

    public PHLight getLight() {
        return light;
    }

    public void setLight(PHLight light) {
        this.light = light;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
