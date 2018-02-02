package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;

public class FindBridgeActivity extends Activity {

    View loadingWheel, loadingContainer;
    String username = "";
    boolean didShowAuthentication, didShowLinkError;

    PHSDKListener listener = new BridgeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_intro_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Common.getAttributeColor(this, R.attr.actionBarColor));
        }

        loadingWheel = findViewById(R.id.loadingWheel);
        loadingContainer = findViewById(R.id.loadingContainer);


        ((SimpleColorButton)findViewById(R.id.connectButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {

                SharedPreferences prefs = Common.getPreferences(FindBridgeActivity.this);
                setLoading(true);

                if(!prefs.getBoolean("usernameSet", false)) {

                    SharedPreferences.Editor e = prefs.edit();
                    e.putString("username", username);
                    e.putBoolean("usernameSet", true);
                    e.commit();
                } else {
                    username = prefs.getString("username", "default");
                }

                final PHHueSDK hueSDK = PHHueSDK.getInstance();
                hueSDK.setDeviceName(Build.MODEL);
                hueSDK.getNotificationManager().registerSDKListener(listener);
                PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                sm.search(true, true);
            }
        });
    }

    public void setLoading(boolean b) {

        if(b) {
            findViewById(R.id.connectButton).setVisibility(View.GONE);
            loadingWheel.startAnimation(AnimationUtils.loadAnimation(FindBridgeActivity.this, R.anim.loading_wheel_animation));
            loadingContainer.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.connectButton).setVisibility(View.VISIBLE);
            loadingWheel.clearAnimation();
            loadingContainer.setVisibility(View.GONE);
        }
    }


    class BridgeListener implements PHSDKListener {

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

        }

        @Override
        public void onBridgeConnected(final PHBridge phBridge, String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                    dialog.setMessageText("Connected");
                    //dialog.show();

                    PHHueSDK hueSDK = PHHueSDK.getInstance();
                    hueSDK.setSelectedBridge(phBridge);
                    hueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);

                    SharedPreferences prefs = Common.getPreferences(FindBridgeActivity.this);
                    SharedPreferences.Editor e = prefs.edit();

                    e.putBoolean("bridgeConnected", true);
                    e.commit();

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
                                    username = null;
                                    loadingWheel = null;

                                    startActivity(new Intent(FindBridgeActivity.this, HueApp.class));
                                    finish();
                                }
                            });
                        }
                    }).start();
                }
            });
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {

        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint phAccessPoint) {

            if(!didShowAuthentication) {
                didShowAuthentication = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                        dialog.setMessageText("Press the link button on your bridge.");
                        dialog.setDismissButtonText("Cancel");
                        dialog.setPositiveButtonText("Continue");
                        dialog.addDialogListener(new WhiteDialog.DialogListener() {
                            @Override
                            public void dismissButtonClicked() {

                            }

                            @Override
                            public void positiveButtonClicked() {

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
                        WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                        dialog.setMessageText("Access Point: " + points.get(0));
                        //dialog.show();

                        PHHueSDK hueSDK = PHHueSDK.getInstance();
                        PHAccessPoint accessPoint = new PHAccessPoint();
                        accessPoint.setIpAddress(points.get(0).getIpAddress());
                        accessPoint.setUsername(username);

                        //save recent bridge
                        SharedPreferences prefs = getSharedPreferences("HueAppPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor e = prefs.edit();
                        e.putBoolean("bridgeConnected", false);
                        e.putString("ip", accessPoint.getIpAddress());
                        e.commit();

                        hueSDK.connect(accessPoint);
                    } else {
                        WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
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
                        didShowLinkError = true;
                        WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                        dialog.setMessageText("Error: " + error);
                        dialog.show();
                    } else if (errorCode == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                        WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                        dialog.setMessageText("Failed to link bridge.");
                        dialog.setDismissButtonVisible(false);
                        dialog.setPositiveButtonText("Try Again");
                        dialog.addDialogListener(new WhiteDialog.DialogListener() {
                            @Override
                            public void dismissButtonClicked() {

                            }

                            @Override
                            public void positiveButtonClicked() {

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
                    WhiteDialog dialog = new WhiteDialog(FindBridgeActivity.this);
                    dialog.setMessageText("Connection Lost");
                    dialog.show();
                }
            });
        }
    }
}
