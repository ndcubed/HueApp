package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;

import java.util.List;

/**
 * Created by Nathan on 12/16/2016.
 */

public class CreateSceneActivity extends Activity {

    ArrayAdapter<CharSequence> groupArrayAdapter;
    Spinner groupSpinner;
    boolean hasPressedNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_scene_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.argb(80, 0, 0, 0));
        }

        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

        groupSpinner = (Spinner)findViewById(R.id.groupSpinner);
        groupArrayAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item);
        groupArrayAdapter.setDropDownViewResource(R.layout.simple_dropdown_spinner_item);

        groupArrayAdapter.add("All Lights");
        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
        for(PHGroup group : groups) {
            groupArrayAdapter.add(group.getName());
        }
        groupSpinner.setAdapter(groupArrayAdapter);
        groupArrayAdapter.notifyDataSetChanged();

        ((SimpleColorButton)findViewById(R.id.nextButton)).addSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {

                if(!hasPressedNext && !((EditText)findViewById(R.id.sceneNameField)).getText().toString().equals("")) {
                    ((TextView)findViewById(R.id.titleLabel)).setText("Choose a Group");
                    findViewById(R.id.sceneNameControls).setVisibility(View.GONE);
                    findViewById(R.id.groupControls).setVisibility(View.VISIBLE);
                    ((SimpleColorButton)findViewById(R.id.nextButton)).setButtonText("Done");

                    hasPressedNext = true;
                } else {
                    /** CREATE SCENE **/

                    String selectedGroup = (String)groupArrayAdapter.getItem(groupSpinner.getSelectedItemPosition());
                    String groupID = "";

                    if(selectedGroup.equals("All Lights")) {
                        Intent intent = getIntent();
                        intent.putExtra("groupID", "0");
                        intent.putExtra("sceneName", ((EditText)findViewById(R.id.sceneNameField)).getText().toString());
                    } else {
                        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
                        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();

                        for(PHGroup group : groups) {
                            if(group.getName().equals(selectedGroup)) {
                                groupID = group.getIdentifier();
                                break;
                            }
                        }

                        Intent intent = getIntent();
                        intent.putExtra("groupID", groupID);
                        intent.putExtra("sceneName", ((EditText)findViewById(R.id.sceneNameField)).getText().toString());
                    }

                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
                }
            }
        });
    }
}
