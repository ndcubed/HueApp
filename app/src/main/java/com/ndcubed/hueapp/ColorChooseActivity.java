package com.ndcubed.hueapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ndcubed.nappsupport.views.ColorChooser;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.*;
import com.ndcubed.hueapp.R;
import java.util.List;

/**
 * Created by Nathan on 7/27/2015.
 */
public class ColorChooseActivity extends Activity {

    public static final String INTENT_SET_GROUP_FLAG_KEY = "setGroup";
    public static final String INTENT_SET_LIGHT_KEY = "lightID";
    public static final String INTENT_SET_GROUP_KEY = "groupIdentifier";
    public static final String INTENT_SET_COLOR_KEY = "color";

    PHLight light;
    boolean setGroup = false;
    String groupIdentifier = "";
    PHGroup group;
    int color = 0;

    private PHLightState lastState = new PHLightState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_choose_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.argb(80, 0, 0, 0));
        }

        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("lightID", "null");
        groupIdentifier = extras.getString("groupIdentifier", "null");
        setGroup = extras.getBoolean("setGroup", false);
        color = extras.getInt("color", Color.rgb(255, 255, 255));

        if(!setGroup) {
            for(PHLight l : lights) {
                if(l.getIdentifier().equals(id)) {
                    light = l;
                    break;
                }
            }
        } else {
            List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
            for(PHGroup g : groups) {
                if(g.getIdentifier().equals(groupIdentifier)) {
                    group = g;
                    break;
                }
            }
        }

        ((ColorChooser)findViewById(R.id.colorChooser)).setColorChooseListener(new ColorChooser.ColorChooseListener() {
            @Override
            public void onColorChosen(int color) {
                ColorChooseActivity.this.color = color;

                if(!setGroup) {
                    PHHueSDK phHueSDK = PHHueSDK.getInstance();
                    LightUtils.updateLightState(phHueSDK, light, color, light.getLastKnownLightState().getBrightness());
                } else {
                    if(group != null) {
                        LightUtils.updateLightColor(PHHueSDK.getInstance(), group, color);
                    }
                }
            }

            @Override
            public void onInteract(int color) {

            }

            @Override
            public void onDone(int color, float x, float y) {
                ColorChooseActivity.this.color = color;

                if(!setGroup) {
                    PHHueSDK phHueSDK = PHHueSDK.getInstance();
                    LightUtils.updateLightState(phHueSDK, light, color, light.getLastKnownLightState().getBrightness());
                }
            }

            /**
             * Does not follow one second call limit. To be Removed...
             * Use OnColorChosen(int color) instead.
             *
             * @param color
             */
            @Override
            public void onColorChange(int color) {

            }
        });

        ((SimpleColorButton)findViewById(R.id.doneButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {
                getIntent().putExtra("color", color);
                getIntent().putExtra("groupIdentifier", groupIdentifier);
                setResult(Activity.RESULT_OK, getIntent());
                finish();
            }
        });
    }
}
