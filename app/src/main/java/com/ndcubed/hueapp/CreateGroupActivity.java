package com.ndcubed.hueapp;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CreateGroupActivity extends Activity {

    ListView lightListView;
    LightArrayAdapter lightArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_creator_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Common.getAttributeColor(this, R.attr.transparentWindowColor));
        }

        lightListView = (ListView)findViewById(R.id.lightList);
        lightArrayAdapter = new LightArrayAdapter(this, R.layout.light_listview_item_layout);
        lightListView.setAdapter(lightArrayAdapter);

        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        for(PHLight light : lights) {

            LightItem item = new LightItem();
            item.setLight(light);
            lightArrayAdapter.add(item);
        }

        ((SimpleColorButton)findViewById(R.id.cancelGroupButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {
                finish();
            }
        });

        ((SimpleColorButton)findViewById(R.id.createGroupButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {

                final List<String> lightIDs = new ArrayList<String>();

                for (int i = 0; i < lightArrayAdapter.getCount(); i++) {

                    LightItem item = lightArrayAdapter.getItem(i);

                    if (item.isSelected()) {
                        lightIDs.add(item.getLight().getIdentifier());
                    }
                }

                final WhiteDialog dialog = new WhiteDialog(CreateGroupActivity.this);
                dialog.setAcceptsInput(true);
                dialog.setMessageText("Name your new group");
                dialog.setPositiveButtonText("Create");
                dialog.setDismissButtonText("Cancel");
                dialog.getInputField().setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                dialog.addDialogListener(new WhiteDialog.DialogListener() {
                    @Override
                    public void dismissButtonClicked() {
                        finish();
                    }

                    @Override
                    public void positiveButtonClicked() {
                        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

                        ArrayList<String> ids = new ArrayList<String>();
                        for (String id : lightIDs) {
                            ids.add(id);
                        }

                        PHGroup group = new PHGroup();
                        group.setLightIdentifiers(ids);
                        group.setName(dialog.getInput());
                        bridge.createGroup(group, new PHGroupListener() {
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
                                WhiteDialog dialog = new WhiteDialog(CreateGroupActivity.this);
                                dialog.setMessageText("Group Created");
                                dialog.setLayout(WhiteDialog.OK_OPTION);
                                dialog.show();
                            }

                            @Override
                            public void onError(int i, String s) {

                            }

                            @Override
                            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                            }
                        });
                        finish();
                    }
                });

                dialog.show();
            }
        });
    }
}
