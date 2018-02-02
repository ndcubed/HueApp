package com.ndcubed.hueapp;

import android.content.Context;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHScene;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class SceneManager {

    public static SceneManager instance = null;

    public static SceneManager getInstance() {
        if(instance == null) {
            return new SceneManager();
        } else {
            return instance;
        }
    }

    public void saveScene(PHScene scene, Context context) {

        ArrayList<Integer> hues = new ArrayList<Integer>();
        ArrayList<Integer> brightness = new ArrayList<Integer>();
        ArrayList<String> lightIdentifiers = new ArrayList<String>();

        for(String id : scene.getLightIdentifiers()) {
            lightIdentifiers.add(id);
        }

        int i = 0;
        List<PHLight> lights = PHHueSDK.getInstance().getSelectedBridge().getResourceCache().getAllLights();
        for(PHLight light : lights) {

            if(light.getIdentifier().equals(lightIdentifiers.get(i))) {
                hues.add(light.getLastKnownLightState().getHue());
                brightness.add(light.getLastKnownLightState().getBrightness());
            }
            i++;
        }

        String sceneName = scene.getName();
        String sceneIdentifier = scene.getSceneIdentifier();

        //WRITE TO SCENE FILE;

        try {
            System.out.println("-----------------------------");
            String fileName = sceneName + ".scene";

            // you want to output to file
            // BufferedWriter writer = new BufferedWriter(new FileWriter(file3, true));
            // but let's print to console while debugging
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

            writer.write(sceneName);
            writer.newLine();
            writer.write(sceneIdentifier);
            writer.newLine();

            //WRITE LIGHT IDS TO FILE
            for(String lID : lightIdentifiers) {
                writer.write(lID);
                writer.newLine();
            }

            //WRITE LIGHT HUES TO FILE
            for(int hue : hues) {
                writer.write(Integer.toString(hue));
                writer.newLine();
            }

            //WRITE LIGHT BRIGHTNESS TO FILE
            for(int bri : brightness) {
                writer.write(Integer.toString(bri));
                writer.newLine();
            }

            writer.close();

            System.out.println("-----------------------------");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public PHScene createSceneFromCurrentLightState(String sceneName) {

        PHHueSDK sdk = PHHueSDK.getInstance();
        PHBridge bridge = sdk.getSelectedBridge();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        PHScene scene = new PHScene();
        ArrayList<String> lightIDs = new ArrayList<String>();

        int i = 0;
        for(PHLight light : lights) {
            lightIDs.add(light.getIdentifier());
            i++;
        }

        scene.setLightIdentifiers(lightIDs);
        scene.setSceneIdentifier(LightUtils.createSceneIdentifier());
        scene.setName(sceneName);

        return scene;
    }
}
