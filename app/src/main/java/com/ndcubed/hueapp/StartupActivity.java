package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ndcubed.hueapp.sensors.SensorsActivity;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHSensorListener;
import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.sensor.PHSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan on 7/23/2015.
 */
public class StartupActivity extends Activity implements PHSensorListener {

    String username = "";
    String ip = "";
    TextView searchLabel;
    View loadingWheel;

    PHSDKListener listener = new BridgeListener();

    boolean didShowLinkError = false;
    boolean didShowAuthentication = false;
    boolean didCreateAllGroup = false;

    ConnectTimer connectTimer = new ConnectTimer();

    int scenesCount = 0;
    int totalScenes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Common.getAttributeColor(this, R.attr.actionBarColor));

            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //getWindow().setStatusBarColor(Common.getAttributeColor(this, R.attr.actionBarColor));
            System.out.println("DRAW BACK");
        }

        loadingWheel = findViewById(R.id.loadingWheel);
        searchLabel = (TextView)findViewById(R.id.searchLabel);

        loadingWheel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.loading_wheel_animation));
        findViewById(R.id.bulbIcon).startAnimation(AnimationUtils.loadAnimation(this, R.anim.float_animation));

        SharedPreferences prefs = Common.getPreferences(this);

        searchLabel.setText("Looking for your Bridge");
        final PHHueSDK hueSDK = PHHueSDK.getInstance();
        hueSDK.setDeviceName(Build.MODEL);
        hueSDK.getNotificationManager().registerSDKListener(listener);

        if(prefs.getBoolean("usernameSet", false)) {
            username = prefs.getString("username", "default");
            ip = prefs.getString("ip", "0");

            System.out.println("CONNECT: " + ip + "  " + username);
            //connect
            PHAccessPoint accessPoint = new PHAccessPoint();
            accessPoint.setIpAddress(ip);
            accessPoint.setUsername(username);

            hueSDK.connect(accessPoint);
            System.out.println("CONNECT: " + username + "  " + ip);

            connectTimer.start();
        } else {
            PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(true, true);

            System.out.println("SEARCHHHH");
        }

        /*
        if(!prefs.getBoolean("bridgeConnected", false)) {
            searchLabel.setText("Connecting to Bridge");

            PHHueSDK sdk = PHHueSDK.getInstance();
            sdk.setDeviceName(Build.MODEL);
            sdk.getNotificationManager().registerSDKListener(listener);

            PHAccessPoint accessPoint = new PHAccessPoint();
            accessPoint.setIpAddress(prefs.getString("ip", "0"));
            accessPoint.setUsername(username);

            sdk.connect(accessPoint);

        } else {
            searchLabel.setText("Looking for your Bridge");

            final PHHueSDK hueSDK = PHHueSDK.getInstance();
            hueSDK.setDeviceName(Build.MODEL);
            hueSDK.getNotificationManager().registerSDKListener(listener);
            PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(true, true);
        }
         */
    }

    private class ConnectTimer extends Thread {

        private boolean connected = false;
        private int i = 0;

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        @Override
        public void run() {

            while(!connected) {

                if(i > 5) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchLabel.setText("Reauthorizing...");
                            SharedPreferences prefs = Common.getPreferences(StartupActivity.this);
                            SharedPreferences.Editor e = prefs.edit();
                            e.putBoolean("usernameSet", false);
                            e.apply();

                            PHBridgeSearchManager sm = (PHBridgeSearchManager) PHHueSDK.getInstance().getSDKService(PHHueSDK.SEARCH_BRIDGE);
                            sm.search(true, true);
                        }
                    });
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                i++;
            }
        }
    }

    class BridgeListener implements PHSDKListener {

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

        }

        @Override
        public void onBridgeConnected(final PHBridge bridge, final String username) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                    dialog.setMessageText("Connected");
                    //dialog.show();

                    PHHueSDK hueSDK = PHHueSDK.getInstance();
                    hueSDK.setSelectedBridge(bridge);
                    hueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

                    SharedPreferences prefs = Common.getPreferences(StartupActivity.this);
                    SharedPreferences.Editor e = prefs.edit();

                    e.putBoolean("bridgeConnected", true);
                    e.putBoolean("usernameSet", true);
                    e.putString("username", username);
                    e.putString("ip", bridge.getResourceCache().getBridgeConfiguration().getIpAddress());
                    e.commit();

                    System.out.println("BRIDGE IP: " + bridge.getResourceCache().getBridgeConfiguration().getIpAddress());

                    searchLabel.setText("Connected");
                    connectTimer.setConnected(true);

                    if(bridge.getResourceCache().getAllLights().isEmpty()) {
                        searchLabel.setText("Finding your lights...");

                        bridge.findNewLights(new PHLightListener() {
                            @Override
                            public void onReceivingLightDetails(PHLight phLight) {

                            }

                            @Override
                            public void onReceivingLights(List<PHBridgeResource> list) {

                            }

                            @Override
                            public void onSearchComplete() {
                                finishStart();

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
                    } else {

                        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
                        boolean hasAllLightsGroup = false;
                        for(PHGroup group : groups) {
                            if(group.getName().equals("All Lights")) {
                                hasAllLightsGroup = true;
                                break;
                            }
                        }

                        if(!hasAllLightsGroup) {
                            List<PHLight> lights = bridge.getResourceCache().getAllLights();
                            ArrayList<String> lightIDs = new ArrayList<>();

                            for(PHLight light : lights) {
                                lightIDs.add(light.getIdentifier());
                            }

                            PHGroup allLightsGroup = new PHGroup();
                            allLightsGroup.setLightIdentifiers(lightIDs);
                            allLightsGroup.setName("All Lights");
                            bridge.createGroup(allLightsGroup, new PHGroupListener() {
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
                        }


                        /*

                        for(PHSensor s : bridge.getResourceCache().getAllSensors()) {
                            bridge.deleteSensor(s.getIdentifier(), new PHSensorListener() {
                                @Override
                                public void onSensorsReceived(List<PHBridgeResource> list) {

                                }

                                @Override
                                public void onReceivingSensorDetails(PHSensor phSensor) {

                                }

                                @Override
                                public void onSensorSearchFinished() {

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
                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bridge.findNewSensors(StartupActivity.this);
                            }
                        });
                        */

                        finishStart();
                    }
                }
            });
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
            System.out.println("ERRRRRRR");
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint phAccessPoint) {

            if(!didShowAuthentication) {
                didShowAuthentication = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                        dialog.setMessageText("Push the link button on your bridge.");
                        dialog.setDismissButtonText("Cancel");
                        dialog.setPositiveButtonText("Continue");
                        dialog.addDialogListener(new WhiteDialog.DialogListener() {
                            @Override
                            public void dismissButtonClicked() {

                            }

                            @Override
                            public void positiveButtonClicked() {
                                searchLabel.setText("Connecting");

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(5000);
                                        } catch(Exception err){}
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                PHHueSDK hueSDK = PHHueSDK.getInstance();
                                                hueSDK.startPushlinkAuthentication(phAccessPoint);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        });
                        dialog.show();
                    }
                });
            }
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {

            final List<PHAccessPoint> points = list;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!points.isEmpty()) {
                        WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                        dialog.setMessageText("Access Point: " + points.get(0));
                        dialog.show();

                        PHHueSDK hueSDK = PHHueSDK.getInstance();
                        PHAccessPoint accessPoint = new PHAccessPoint();
                        accessPoint.setIpAddress(points.get(0).getIpAddress());
                        accessPoint.setUsername(points.get(0).getUsername());

                        hueSDK.connect(accessPoint);
                    } else {
                        WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                        dialog.setMessageText("No Points");
                        dialog.show();
                    }
                }
            });
        }

        @Override
        public void onError(int i, String s) {

            final String error = s;
            final int errorCode = i;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED && !didShowLinkError) {
                        searchLabel.setText("Press the bridge link button");
                        didShowLinkError = true;
                        WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                        dialog.setMessageText("Error: " + error);
                        dialog.show();
                    } else if (errorCode == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                        WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                        dialog.setMessageText("Failed to link bridge.");
                        dialog.setDismissButtonVisible(false);
                        dialog.setPositiveButtonText("Try Again");
                        dialog.addDialogListener(new WhiteDialog.DialogListener() {
                            @Override
                            public void dismissButtonClicked() {

                            }

                            @Override
                            public void positiveButtonClicked() {
                                searchLabel.setText("Looking for your Bridge");

                                final PHHueSDK hueSDK = PHHueSDK.getInstance();
                                hueSDK.setDeviceName(Build.MODEL);
                                hueSDK.getNotificationManager().registerSDKListener(listener);
                                PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                                sm.search(true, true);

                                didShowAuthentication = false;
                            }
                        });
                        dialog.show();
                    }
                }
            });
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WhiteDialog dialog = new WhiteDialog(StartupActivity.this);
                    dialog.setMessageText("Connection Lost");
                    dialog.show();
                }
            });
        }
    }

    public void finishStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(500);
                } catch(Exception err) {}

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingWheel.clearAnimation();
                        PHHueSDK sdk = PHHueSDK.getInstance();
                        sdk.setDeviceName(Build.MODEL);
                        sdk.getNotificationManager().unregisterSDKListener(listener);

                        listener = null;
                        searchLabel = null;
                        loadingWheel = null;

                        startActivity(new Intent(StartupActivity.this, HueApp.class));
                        finish();
                    }
                });
            }
        }).start();
    }


    /** PHSensorListener **/
    @Override
    public void onSensorsReceived(List<PHBridgeResource> list) {

        if(list != null) {
            for(PHBridgeResource b : list) {
                System.out.println("----> " + b.getName());
            }
        }
    }

    @Override
    public void onReceivingSensorDetails(PHSensor phSensor) {
        System.out.println("DETAILS");
    }

    @Override
    public void onSensorSearchFinished() {
        System.out.println("SENSOR FINISHED");
    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(StartupActivity.this, SensorsActivity.class));
        //finishStart();
    }

    @Override
    public void onError(int i, String s) {

    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
        System.out.println("STATE UPDATE");
    }
}
