package com.ndcubed.hueapp;

import com.philips.lighting.model.PHScene;

/**
 * Created by Nathan on 7/28/2015.
 */
public class SceneItem {

    private PHScene scene;

    public SceneItem(PHScene scene) {
        this.scene = scene;
    }

    public PHScene getScene() {
        return scene;
    }

    public void setScene(PHScene scene) {
        this.scene = scene;
    }
}
