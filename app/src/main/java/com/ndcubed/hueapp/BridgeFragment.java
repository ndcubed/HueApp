package com.ndcubed.hueapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.RoundedSwitch;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHSceneListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan on 7/21/2015.
 */
public class BridgeFragment extends Fragment {

    ViewGroup rootView;
    boolean lightIsOn = false;
    RoundedSwitch lightSwitch;

    GroupControlArrayAdapter groupAdapter;

    final int NEW_GROUP_INTENT = 1;
    final int CHANGE_COLOR_INTENT = 2;

    FragmentReceiver fragmentReceiver = new FragmentReceiver();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().registerReceiver(fragmentReceiver, new IntentFilter("fragmentUpdater"));

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        System.out.println("DP: " + metrics.densityDpi);
        rootView = (ViewGroup)inflater.inflate(R.layout.bridge_layout, container, false);
        SharedPreferences prefs = Common.getPreferences(getActivity());

        ((TextView)rootView.findViewById(R.id.bridgeIPLabel)).setText(prefs.getString("ip", "..."));

        PHHueSDK sdk = PHHueSDK.getInstance();
        final PHBridge bridge = sdk.getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();

        final ListView groupList = (ListView)rootView.findViewById(R.id.groupsListView);
        groupAdapter = new GroupControlArrayAdapter(getActivity(), R.layout.light_control_listview_item_layout);

        /*
        List<PHScene> scenes = cache.getAllScenes();
        for(PHScene scene : scenes) {

            bridge.deleteScene(scene.getSceneIdentifier(), new PHSceneListener() {
                @Override
                public void onScenesReceived(List<PHScene> list) {

                }

                @Override
                public void onSceneReceived(PHScene phScene) {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                }
            });
        }

*/
        List<PHGroup> groups = cache.getAllGroups();
        List<PHLight> lights = cache.getAllLights();
        for(PHGroup group : groups) {

            List<String> groupLightIDs = group.getLightIdentifiers();
            ArrayList<Integer> colorArray = new ArrayList<>();

            for(String lightID : groupLightIDs) {

                for(PHLight light : lights) {

                    if(lightID.equals(light.getIdentifier())) {
                        float[] xy = {light.getLastKnownLightState().getX(), light.getLastKnownLightState().getY()};
                        colorArray.add(PHUtilities.colorFromXY(xy, light.getModelNumber()));
                        break;
                    }
                }
            }

            int[] colors = new int[colorArray.size() + 1];
            for(int i = 0; i < colorArray.size(); i++) {
                colors[i] = colorArray.get(i);
            }
            colors[colors.length-1] = colors[0];

            if(!group.getName().startsWith("HueApp")) {
                groupAdapter.add(new GroupItem(group, colors));
            }

        }
        groupList.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();

        groupAdapter.setColorDotClickListener(new GroupControlArrayAdapter.OnColorDotClickListener() {
            @Override
            public void onClick(PHGroup group, int color) {
                Intent intent = new Intent(getActivity(), ColorChooseActivity.class);
                intent.putExtra("setGroup", true);
                intent.putExtra("groupIdentifier", group.getIdentifier());
                intent.putExtra("color", color);
                startActivityForResult(intent, CHANGE_COLOR_INTENT);
            }
        });
        groupAdapter.setLightSwitchClickedListener(new GroupControlArrayAdapter.OnLightSwitchClickedListener() {
            @Override
            public void onClick(int lightSwitchState) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception err) {
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                softRefresh();
                            }
                        });
                    }
                }).start();
            }
        });
        groupList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final GroupItem groupItem = groupAdapter.getItem(i);

                final WhiteDialog dialog = new WhiteDialog(getActivity());
                dialog.setMessageText("Delete " + groupItem.getGroup().getName() + "?");
                dialog.setPositiveButtonText("Delete");
                dialog.setDismissButtonText("Cancel");
                dialog.setHideOnClick(false);
                dialog.addDialogListener(new WhiteDialog.DialogListener() {
                    @Override
                    public void dismissButtonClicked() {
                        dialog.hide();
                    }

                    @Override
                    public void positiveButtonClicked() {
                        dialog.setIsLoadingDialog(true);
                        dialog.setMessageText("Talking With Bridge");

                        groupAdapter.remove(groupAdapter.getItem(i));
                        groupAdapter.notifyDataSetChanged();

                        final PHGroup group = groupItem.getGroup();
                        group.setName("HueAppTempGroup");

                        PHHueSDK sdk = PHHueSDK.getInstance();
                        sdk.getSelectedBridge().updateGroup(group, new PHGroupListener() {
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
                            public void onSuccess() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        PHHueSDK sdk = PHHueSDK.getInstance();
                                        sdk.getSelectedBridge().deleteGroup(group.getIdentifier(), null);
                                        System.out.println("DELETE");
                                        dialog.hide();
                                    }
                                });
                            }

                            @Override
                            public void onError(int i, String s) {

                            }

                            @Override
                            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                            }
                        });
                    }
                });
                dialog.show();
                return true;
            }
        });

        boolean lightsOn = false;
        for(PHLight light : lights) {
            if(light.getLastKnownLightState().isOn()) {
                lightsOn = true;
                break;
            }
        }
        lightSwitch = (RoundedSwitch)rootView.findViewById(R.id.lightSwitch);
        lightSwitch.setSwitchState(lightsOn ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        lightSwitch.setRoundedSwitchListener(new RoundedSwitch.RoundedSwitchListener() {
            @Override
            public void onPress() {
                ((ViewPager) getActivity().findViewById(R.id.pager)).requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onRelease() {
                ((ViewPager) getActivity().findViewById(R.id.pager)).requestDisallowInterceptTouchEvent(false);
            }

            @Override
            public void onStateChange(int switchState) {

                if (switchState == RoundedSwitch.STATE_OFF) {
                    PHLightState state = new PHLightState();
                    state.setOn(false);
                    bridge.setLightStateForDefaultGroup(state);
                } else {
                    PHLightState state = new PHLightState();
                    state.setOn(true);
                    bridge.setLightStateForDefaultGroup(state);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception err) {
                            err.printStackTrace();
                        }

                        if(isAdded()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    groupAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        ((SimpleColorButton)rootView.findViewById(R.id.disconnectButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {
                startActivity(new Intent(getActivity(), FindBridgeActivity.class));
                getActivity().finish();
            }
        });

        rootView.findViewById(R.id.addGroupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CLICK");
                startActivityForResult(new Intent(getActivity(), GroupActivity.class), NEW_GROUP_INTENT);
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        refresh();
    }

    @Override
    public void onDestroyView() {
        System.out.println("UNREGISTER");
        getActivity().unregisterReceiver(fragmentReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_GROUP_INTENT) {
            refresh();
        } else if(requestCode == CHANGE_COLOR_INTENT && resultCode == Activity.RESULT_OK) {
            int color = data.getIntExtra("color", 0);
            String groupIdentifier = data.getStringExtra("groupIdentifier");

            SharedPreferences prefs = getActivity().getSharedPreferences("HueAppPreferences", Activity.MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();
            e.putInt("group" + groupIdentifier + "Color", color);
            e.commit();

            Intent intent = new Intent("fragmentUpdater");
            getActivity().sendBroadcast(intent);

            refresh();
        }
    }

    public void refresh() {
        System.out.println("REFRESH::");

        if(isAdded()) {
            groupAdapter.clear();

            PHHueSDK sdk = PHHueSDK.getInstance();
            List<PHGroup> groups = sdk.getSelectedBridge().getResourceCache().getAllGroups();
            List<PHLight> lights = sdk.getSelectedBridge().getResourceCache().getAllLights();

            for(PHGroup group : groups) {

                List<String> groupLightIDs = group.getLightIdentifiers();
                ArrayList<Integer> colorArray = new ArrayList<>();

                for(String lightID : groupLightIDs) {

                    for(PHLight light : lights) {

                        if(lightID.equals(light.getIdentifier())) {
                            float[] xy = {light.getLastKnownLightState().getX(), light.getLastKnownLightState().getY()};
                            colorArray.add(PHUtilities.colorFromXY(xy, light.getModelNumber()));
                            break;
                        }
                    }
                }

                int[] colors = new int[colorArray.size() + 1];
                for(int i = 0; i < colorArray.size(); i++) {
                    colors[i] = colorArray.get(i);
                }
                colors[colors.length-1] = colors[0];

                if(!group.getName().startsWith("HueApp")) {
                    groupAdapter.add(new GroupItem(group, colors));
                }

            }
            groupAdapter.notifyDataSetChanged();

            boolean lightsOn = false;
            for(PHLight light : lights) {
                if(light.getLastKnownLightState().isOn()) {
                    lightsOn = true;
                    break;
                }
            }

            lightSwitch.setSwitchState(lightsOn ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        }
    }

    public void softRefresh() {

        if(isAdded()) {
            groupAdapter.notifyDataSetChanged();

            PHHueSDK sdk = PHHueSDK.getInstance();
            List<PHLight> lights = sdk.getSelectedBridge().getResourceCache().getAllLights();

            boolean lightsOn = false;
            for(PHLight light : lights) {
                if(light.getLastKnownLightState().isOn()) {
                    lightsOn = true;
                    break;
                }
            }

            lightSwitch.setSwitchState(lightsOn ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        }
    }

    public class FragmentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }
}
