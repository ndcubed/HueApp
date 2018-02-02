package com.ndcubed.hueapp;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.ColorChooser;
import com.ndcubed.nappsupport.views.ColorDot;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan on 7/21/2015.
 */
public class ColorFragment extends Fragment {

    SeekBar brightnessSlider;
    ViewPager pager;
    ScrollView scrollViewContainer;
    ViewGroup rootView;

    ArrayAdapter<CharSequence> groupAdapter;
    ArrayList<PHLight> activeLights = new ArrayList<PHLight>();
    Spinner groupSpinner;
    PHGroup activeGroup = null;

    float downX = 0f;
    float downY = 0f;
    float dX = 0f;
    float dY = 0f;
    float dragthreshold = 0f;

    boolean run = false;
    boolean singleLight = false;
    String activeLight = "All Lights";
    boolean isSingleLight = false;
    boolean allLights = true;

    ColorChooser colorChooser;
    ColorDot colorDot;

    final int CREATE_GROUP_ACTIVITY = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup)inflater.inflate(R.layout.color_layout, container, false);
        colorChooser = (ColorChooser)rootView.findViewById(R.id.colorChooser);
        colorDot = (ColorDot)rootView.findViewById(R.id.colorDot);

        groupSpinner = (Spinner)rootView.findViewById(R.id.groupSpinner);
        groupAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences prefs = Common.getPreferences(getActivity());
        if(prefs.getBoolean("bridgeConnected", false)) {


            ((SimpleColorButton)rootView.findViewById(R.id.newGroupButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
                @Override
                public void onClick(SimpleColorButton view) {
                    startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_ACTIVITY);
                }
            });

            ((SimpleColorButton)rootView.findViewById(R.id.deleteGroupButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
                @Override
                public void onClick(SimpleColorButton view) {
                    groupSpinner.setEnabled(false);

                    PHHueSDK sdk = PHHueSDK.getInstance();
                    PHBridge bridge = sdk.getSelectedBridge();
                    PHBridgeResourcesCache cache = bridge.getResourceCache();
                    List<PHGroup> groups = cache.getAllGroups();
                    String name = groupAdapter.getItem(groupSpinner.getSelectedItemPosition()).toString();

                    for (PHGroup group : groups) {

                        if (group.getName().equals(name)) {
                            SharedPreferences prefs = Common.getPreferences(getActivity());
                            SharedPreferences.Editor e = prefs.edit();
                            e.remove("group" + group.getIdentifier() + "Color");
                            e.apply();

                            bridge.deleteGroup(group.getIdentifier(), new PHGroupListener() {
                                @Override
                                public void onSuccess() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            refresh();
                                        }
                                    });
                                }

                                @Override
                                public void onError(int i, String s) {
                                    System.out.println("ERROR: " + s);
                                }

                                @Override
                                public void onCreated(PHGroup phGroup) {

                                }

                                @Override
                                public void onReceivingGroupDetails(PHGroup phGroup) {

                                }

                                @Override
                                public void onReceivingAllGroups(List<PHBridgeResource> list) {

                                }

                                @Override
                                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                                }
                            });
                            break;
                        }
                    }
                }
            });

            groupAdapter.add("All Lights");

            PHHueSDK sdk = PHHueSDK.getInstance();
            PHBridge bridge = sdk.getSelectedBridge();
            PHBridgeResourcesCache cache = bridge.getResourceCache();
            List<PHLight> lights = LightUtils.getAllLights(sdk);
            List<PHGroup> groups = cache.getAllGroups();

            for(PHGroup group : groups) {
                groupAdapter.add(group.getName());
            }

            //groupAdapter.add("+ New Group");

            groupAdapter.notifyDataSetChanged();
        }

        groupSpinner.setAdapter(groupAdapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO Match current light brightness when fragment is created and for "All Lights" option.

                colorChooser.clearColor();

                PHHueSDK phHueSDK = PHHueSDK.getInstance();
                PHBridge bridge = phHueSDK.getSelectedBridge();
                PHBridgeResourcesCache cache = bridge.getResourceCache();
                List<PHGroup> groups = cache.getAllGroups();
                List<PHLight> lights = LightUtils.getAllLights(phHueSDK);
                String name = groupAdapter.getItem(position).toString();

                if(groupAdapter.getItem(position).toString().equals("+ New Group")) {
                    startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_ACTIVITY);
                } else {

                    if(position == 0) {

                        allLights = true;
                        activeGroup = null;
                    } else {
                        if(!isSingleLight) {
                            allLights = false;

                            //check for groups
                            for(PHGroup group : groups) {
                                if(group.getName().equals(groupAdapter.getItem(position).toString())) {
                                    activeGroup = group;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        colorChooser.setColorChooseListener(new ColorChooser.ColorChooseListener() {

            @Override
            public void onColorChange(int color) {
                colorDot.setDotColor(color);
            }

            @Override
            public void onColorChosen(final int color) {
                PHHueSDK phHueSDK = PHHueSDK.getInstance();
                PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();

                if(allLights) {
                    PHLightState lightState = new PHLightState();
                    ArrayList<Float> xy = LightUtils.getRGBtoXY(color);
                    float x = xy.get(0);
                    float y = xy.get(1);
                    lightState.setY(y);
                    lightState.setX(x);
                    lightState.setBrightness(brightnessSlider.getProgress());
                    phHueSDK.getSelectedBridge().setLightStateForDefaultGroup(lightState);
                } else {
                    PHLightState lightState = new PHLightState();
                    ArrayList<Float> xy = LightUtils.getRGBtoXY(color);
                    float x = xy.get(0);
                    float y = xy.get(1);
                    lightState.setY(y);
                    lightState.setX(x);
                    lightState.setBrightness(brightnessSlider.getProgress());
                    phHueSDK.getSelectedBridge().setLightStateForGroup(activeGroup.getIdentifier(), lightState);
                }

                /*
                if(isSingleLight) {
                    LightUtils.updateLightState(activeLights.get(0), phHueSDK, color, brightnessSlider.getProgress());
                } else if(groupSpinner.getSelectedItemPosition() == 0) {
                    LightUtils.updateLightState(myLights, phHueSDK, color, brightnessSlider.getProgress());
                } else {
                    LightUtils.updateLightState(activeLights, phHueSDK, color, brightnessSlider.getProgress());
                }
                */
            }

            @Override
            public void onInteract(int color) {
                ScrollView scrollView = (ScrollView)rootView.findViewById(R.id.scrollViewContainer);
                scrollView.requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onDone(int color, float x, float y) {
                ScrollView scrollView = (ScrollView)rootView.findViewById(R.id.scrollViewContainer);
                scrollView.requestDisallowInterceptTouchEvent(false);

                SharedPreferences prefs = Common.getPreferences(getActivity());
                SharedPreferences.Editor e = prefs.edit();

                PHHueSDK phHueSDK = PHHueSDK.getInstance();
                PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
                List<PHLight> myLights = cache.getAllLights();

                /*
                if(isSingleLight) {
                    LightUtils.updateLightState(activeLights.get(0), phHueSDK, color, brightnessSlider.getProgress());

                    e.putFloat(activeLights.get(0).getName() + "X", x);
                    e.putFloat(activeLights.get(0).getName() + "Y", y);
                    e.commit();
                } else if(groupSpinner.getSelectedItemPosition() == 0) {
                    LightUtils.updateLightState(myLights, phHueSDK, color, brightnessSlider.getProgress());

                    e.putFloat("allLightsX", x);
                    e.putFloat("allLightsY", y);
                    e.commit();
                } else {
                    LightUtils.updateLightState(activeLights, phHueSDK, color, brightnessSlider.getProgress());
                }
                */
            }
        });

        brightnessSlider = (SeekBar)rootView.findViewById(R.id.brightnessBar);
        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if(!run) {
                    run = true;
                    new BrightnessThread().start();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                run = false;
                applyBrightness();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CREATE_GROUP_ACTIVITY) {

            groupAdapter.clear();
            groupAdapter.add("All Lights");

            PHHueSDK sdk = PHHueSDK.getInstance();
            PHBridge bridge = sdk.getSelectedBridge();
            PHBridgeResourcesCache cache = bridge.getResourceCache();

            List<PHGroup> groups = cache.getAllGroups();
            for(PHGroup group : groups) {
                groupAdapter.add(group.getName());
            }

            //groupAdapter.add("+ New Group");

            groupAdapter.notifyDataSetChanged();
        }
    }

    public void refresh() {
        groupAdapter.clear();
        groupAdapter.add("All Lights");

        PHHueSDK sdk = PHHueSDK.getInstance();
        PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        List<PHLight> lights = LightUtils.getAllLights(sdk);
        List<PHGroup> groups = cache.getAllGroups();

        for(PHGroup group : groups) {
            groupAdapter.add(group.getName());
        }

        //groupAdapter.add("+ New Group");
        groupAdapter.notifyDataSetChanged();

        groupSpinner.setEnabled(true);
        groupSpinner.setSelection(0);
    }

    public void applyBrightness() {
        System.out.println("APPLY: " +  brightnessSlider.getProgress());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PHHueSDK phHueSDK = PHHueSDK.getInstance();
                PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();

                if(activeGroup != null) {
                    LightUtils.updateLightState(phHueSDK, activeGroup, brightnessSlider.getProgress());
                } else {
                    LightUtils.updateLightStateForDefaultGroup(phHueSDK, brightnessSlider.getProgress());
                }
            }
        });
    }

    class BrightnessThread extends Thread {

        @Override
        public void run() {

            while(run) {
                try {
                    Thread.sleep(1000);
                } catch(Exception err){}

                applyBrightness();
            }
        }
    }
}
