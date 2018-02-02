package com.ndcubed.hueapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndcubed.hueapp.R;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHGroup;

import java.util.List;

/**
 * Created by neptu on 5/25/2016.
 */
public class GroupColorFragment extends Fragment {

    View rootView;
    View background;
    private int color = 0;
    final int COLOR_CHOOSE_INTENT = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.group_creator_colors_layout, container, false);

        background = rootView.findViewById(R.id.colorBackground);

        rootView.findViewById(R.id.pickButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PHHueSDK sdk = PHHueSDK.getInstance();
                List<PHGroup> groups = sdk.getSelectedBridge().getResourceCache().getAllGroups();

                Intent intent = new Intent(getActivity(), ColorChooseActivity.class);
                intent.putExtra("setGroup", true);

                for(PHGroup g : groups) {
                    if(g.getName().equals("HueAppTempGroup")) {
                        intent.putExtra("groupIdentifier", g.getIdentifier());
                        break;
                    }
                }
                startActivityForResult(intent, COLOR_CHOOSE_INTENT);
            }
        });
        return rootView;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == COLOR_CHOOSE_INTENT && resultCode == Activity.RESULT_OK) {
            color = data.getIntExtra("color", 0);

            SharedPreferences prefs = getActivity().getSharedPreferences("HueAppPreferences", Activity.MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();
            e.putInt("tempColor", color);
            e.commit();
        }
    }
}
