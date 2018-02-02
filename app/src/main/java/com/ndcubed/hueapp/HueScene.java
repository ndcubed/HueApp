package com.ndcubed.hueapp;

import com.philips.lighting.model.PHLight;

import java.util.ArrayList;


public class HueScene {

    private ArrayList<Integer> hues = new ArrayList<Integer>();
    private ArrayList<String> lightIdentifiers = new ArrayList<String>();
    private String sceneName;
    private String sceneID;

    public HueScene(String sceneName, String sceneID) {
        this.sceneName = sceneName;
        this.sceneID = sceneID;
    }

    public void addLight(PHLight light) {

        hues.add(light.getLastKnownLightState().getHue());
        lightIdentifiers.add(light.getIdentifier());
    }

    public ArrayList<Integer> getHues() {
        return hues;
    }

    public void setHues(ArrayList<Integer> hues) {
        this.hues = hues;
    }

    public ArrayList<String> getLightIdentifiers() {
        return lightIdentifiers;
    }

    public void setLightIdentifiers(ArrayList<String> lightIdentifiers) {
        this.lightIdentifiers = lightIdentifiers;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getSceneID() {
        return sceneID;
    }

    public void setSceneID(String sceneID) {
        this.sceneID = sceneID;
    }
}
