package com.ndcubed.hueapp;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ndcubed.hueapp.R;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neptu on 5/25/2016.
 */
public class GroupLightsFragment extends Fragment {

    View rootView;
    ListView lightListView;
    LightArrayAdapter lightArrayAdapter;
    String groupID;
    private List<String> lightIDs = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.group_creater_lights_layout, container, false);

        lightListView = (ListView)rootView.findViewById(R.id.lightList);
        lightArrayAdapter = new LightArrayAdapter(getActivity(), R.layout.light_white_listview_item_layout);

        PHHueSDK sdk = PHHueSDK.getInstance();
        List<PHLight> lights = sdk.getSelectedBridge().getResourceCache().getAllLights();

        for(PHLight light : lights) {
            lightArrayAdapter.add(new LightItem(light));
        }

        lightListView.setAdapter(lightArrayAdapter);
        lightArrayAdapter.notifyDataSetChanged();

        lightArrayAdapter.setLightSelectionListener(new LightArrayAdapter.LightSelectionListener() {
            @Override
            public void lightSelected(PHLight light) {

                lightIDs.clear();

                for (int i = 0; i < lightArrayAdapter.getCount(); i++) {
                    if (lightArrayAdapter.getItem(i).isSelected()) {
                        lightIDs.add(lightArrayAdapter.getItem(i).getLight().getIdentifier());
                    }
                }

                try {
                    if(isAdded()) {
                        ((OnLightSelectionListener)getActivity()).lightSelected(lightIDs);
                    }
                } catch(ClassCastException err) {
                    err.printStackTrace();
                }
            }
        });

        return rootView;
    }

    public List<String> getLightIDs() {
        return lightIDs;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isAdded()) {
            PHHueSDK sdk = PHHueSDK.getInstance();
            PHBridge bridge = sdk.getSelectedBridge();

            List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
            for(PHGroup g : groups) {
                if(g.getName().equals("HueAppTempGroup")) {
                    PHGroup tempGroup = new PHGroup();
                    tempGroup.setName(g.getName());
                    tempGroup.setIdentifier(g.getIdentifier());
                    tempGroup.setLightIdentifiers(lightIDs);
                    bridge.updateGroup(tempGroup, null);

                    break;
                }
            }
        }
    }

    interface OnLightSelectionListener {
        void lightSelected(List<String> selectedLights);
    }
}
