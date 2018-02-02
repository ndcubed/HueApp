package com.ndcubed.hueapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.ActivityCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.IconViewPagerTabs;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.ndcubed.nappsupport.views.ViewPagerTabs;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHSceneListener;
import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager;
import com.philips.lighting.model.*;
import com.philips.lighting.model.sensor.PHSensor;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

//TODO When lights are off and app just started changing color does not turn them on when brightness is set to high (all lights)
//TODO Create on / off buttons for groups to quickly turn group lights on and off.
//TODO Capitalize each letter in create group name text field.
//TODO Create interactive list of lights for LightFragment
//TODO!! Fix missed bridge commands. Must limit commands to 1 per second for a total of 10 commands a second.

public class HueApp extends Activity {

    String username;
    ViewPager viewPager;
    ScreenSlidePagerAdapter adapter;
    ViewPagerTabs viewPagerTabs;
    TextView tabTitleLabel;
    BridgeListener bridgeListener = new BridgeListener();

    View sideView;
    SideView sideViewContainer;
    private float sideViewWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences geoPrefs = getSharedPreferences(Common.GEOFENCE_PREFERENCES, Activity.MODE_PRIVATE);
        System.out.println("DID LEAVE: " + geoPrefs.getBoolean("didLeaveHome", false));

        findViewById(R.id.rootAppContainer).setPadding(0, getStatusBarHeight(), 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Common.getAttributeColor(this, R.attr.actionBarColor));
        }

        SharedPreferences prefs = getSharedPreferences("HueAppPreferences", MODE_PRIVATE);
        if(!prefs.getBoolean("usernameSet", false)) {

            SharedPreferences.Editor e = prefs.edit();
            e.putString("username", username);
            e.putBoolean("usernameSet", true);
            e.commit();
        } else {
            username = prefs.getString("username", "default");
        }

        final PHHueSDK hueSDK = PHHueSDK.getInstance();
        final PHBridge bridge = hueSDK.getSelectedBridge();
        hueSDK.setDeviceName(Build.MODEL);
        hueSDK.getNotificationManager().registerSDKListener(bridgeListener);
        PHBridgeConfiguration config = bridge.getResourceCache().getBridgeConfiguration();
        System.out.println("API: " + config.getAPIVersion());

        for(PHSensor sensor : bridge.getResourceCache().getAllSensors()) {
            System.out.println("sensor: " + sensor.getTypeAsString());
        }

        //cleanup temp groups
        List<PHGroup> groups = hueSDK.getSelectedBridge().getResourceCache().getAllGroups();
        for(PHGroup g : groups) {
            if(g.getName().startsWith("HueApp")) {
                hueSDK.getSelectedBridge().deleteGroup(g.getIdentifier(), null);
            }
        }

        viewPager = (ViewPager)findViewById(R.id.pager);
        adapter = new ScreenSlidePagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        viewPagerTabs = (ViewPagerTabs)findViewById(R.id.viewPagerTabs);
        viewPagerTabs.setViewPager(viewPager);
        viewPagerTabs.setTabLabelColor(Color.rgb(255, 255, 255));

        viewPagerTabs.setTabUnderlineColor(Common.getAttributeColor(this, R.attr.actionBarColor));

        tabTitleLabel = (TextView)findViewById(R.id.tabTitleLabel);

        viewPagerTabs.setScrollListener(new ViewPagerTabs.ScrollListener() {
            @Override
            public void onScroll(float percent, int direction) {

            }

            @Override
            public void onTabSelected(int position, String title) {
                tabTitleLabel.setText(title);
                //viewPager.setIntercept(!(position == 1));
            }
        });


        sideViewContainer = (SideView)findViewById(R.id.sideViewContainer);
        sideView = findViewById(R.id.sideView);
        sideViewContainer.setTriggerView(findViewById(R.id.sideViewTrigger));
        sideViewContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                sideViewWidth = findViewById(R.id.sideView).getWidth();
                findViewById(R.id.sideViewContainer).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                System.out.println("SIDE VIEW: " + sideViewWidth);
                sideViewContainer.setVisibility(View.GONE);
            }
        });
        sideViewContainer.setAnimatedChildContainer(sideView);

        findViewById(R.id.eraseContentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
                        List<PHLight> lights = bridge.getResourceCache().getAllLights();
                        List<PHScene> scenes = bridge.getResourceCache().getAllScenes();

                        for(PHGroup group : groups) {
                            bridge.deleteGroup(group.getIdentifier(), new PHGroupListener() {
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

                                }

                                @Override
                                public void onError(int i, String s) {

                                }

                                @Override
                                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                                }
                            });
                            try {
                                Thread.sleep(100);
                            } catch(Exception err) {
                                err.printStackTrace();
                            }
                        }

                        for(PHLight light : lights) {
                            bridge.deleteLight(light.getIdentifier(), new PHLightListener() {
                                @Override
                                public void onReceivingLightDetails(PHLight phLight) {

                                }

                                @Override
                                public void onReceivingLights(List<PHBridgeResource> list) {

                                }

                                @Override
                                public void onSearchComplete() {

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
                            try {
                                Thread.sleep(100);
                            } catch(Exception err) {
                                err.printStackTrace();
                            }
                        }

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
                            try {
                                Thread.sleep(100);
                            } catch(Exception err) {
                                err.printStackTrace();
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch(Exception err) {
                            err.printStackTrace();
                        }

                        Intent startActivity = new Intent(HueApp.this, StartupActivity.class);
                        int intentID = 123456;
                        PendingIntent intent = PendingIntent.getActivity(HueApp.this, intentID, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)HueApp.this.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, intent);
                        finish();

                    }
                }).start();
            }
        });

        findViewById(R.id.dragHandle).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    sideViewContainer.setSideViewVisible(true);
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {

        if(sideViewContainer.isSideViewVisible()) {
            sideViewContainer.setSideViewVisible(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        System.out.println("FINISH!");

        PHHueSDK hueSDK = PHHueSDK.getInstance();

        List<PHGroup> groups = hueSDK.getSelectedBridge().getResourceCache().getAllGroups();
        for(PHGroup g : groups) {
            if(g.getName().startsWith("HueApp")) {
                hueSDK.getSelectedBridge().deleteGroup(g.getIdentifier(), null);
            }
        }

        hueSDK.disableAllHeartbeat();
        hueSDK.disconnect(hueSDK.getSelectedBridge());
        hueSDK.getNotificationManager().unregisterSDKListener(bridgeListener);

        super.onDestroy();
    }

    class BridgeListener implements PHSDKListener {

        WhiteDialog bridgeMessageDialog;

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String s) {

        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {

        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {

        }

        @Override
        public void onError(int i, String s) {

        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bridgeMessageDialog != null) {
                        bridgeMessageDialog.hide();
                        bridgeMessageDialog = null;
                    }
                }
            });
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bridgeMessageDialog != null) {
                        bridgeMessageDialog.hide();
                        bridgeMessageDialog = null;
                    }
                    bridgeMessageDialog = new WhiteDialog(HueApp.this);
                    bridgeMessageDialog.setMessageText("Connection to bridge lost.");
                    bridgeMessageDialog.setIsLoadingDialog(true);
                    bridgeMessageDialog.show();
                }
            });
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {

        }
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


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return new BridgeFragment();
            } else if(position == 1) {
                return new LightsFragment();
            } else if(position == 2) {
                return new ScenesFragment();
            } else if(position == 3) {
                return new LocationFragment();
            } else if(position == 4) {
                return new ScheduleFragment();
            } else {
                return new BridgeFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            String pageTitle = "Bridge";

            if(position == 0) {
                pageTitle = "Groups";
            } else if(position == 1) {
                pageTitle = "Lights";
            } else if(position == 2) {
                pageTitle = "Scenes";
            } else if(position == 3) {
                pageTitle = "Location";
            } else if(position == 4) {
                pageTitle = "Sensors";
            }

            return pageTitle;
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
