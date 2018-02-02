package com.ndcubed.hueapp;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.*;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import com.ndcubed.hueapp.R;

/**
 * Created by Nathan on 7/26/2015.
 */
public class LightsFragment extends Fragment {

    View rootView;
    LightControlArrayAdapter adapter;

    final int COLOR_CHOOSER_ACTIVITY = 0;
    final int CHANGE_LIGHT_NAME_ACTIVITY = 1;

    Spinner groupSpinner;
    ArrayAdapter<CharSequence> groupArrayAdapter;
    FragmentReceiver fragmentReceiver = new FragmentReceiver();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.lights_fragment_layout, container, false);

        getActivity().registerReceiver(fragmentReceiver, new IntentFilter("fragmentUpdater"));

        ListView lightListView = (ListView)rootView.findViewById(R.id.lightsListView);
        adapter = new LightControlArrayAdapter(getActivity(), R.layout.light_control_listview_item_layout);

        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();

        groupSpinner = (Spinner)rootView.findViewById(R.id.groupSpinner);
        groupArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item);
        groupArrayAdapter.setDropDownViewResource(R.layout.simple_dropdown_spinner_item);
       // groupArrayAdapter.add("All Lights");

        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
        for(PHGroup group : groups) {
            groupArrayAdapter.add(group.getName());
        }
        groupSpinner.setAdapter(groupArrayAdapter);
        groupArrayAdapter.notifyDataSetChanged();

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
                List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
                List<PHLight> lights = bridge.getResourceCache().getAllLights();

                adapter.clear();
                for(PHGroup group : groups) {
                    if(group.getName().equals(groupArrayAdapter.getItem(groupArrayAdapter.getCount() <= groupSpinner.getSelectedItemPosition() ? 0 : groupSpinner.getSelectedItemPosition()).toString())) {

                        List<String> ids = group.getLightIdentifiers();
                        for(String lightID : ids) {

                            for(PHLight light : lights) {
                                if(light.getIdentifier().equals(lightID)) {
                                    adapter.add(new LightItem(light));
                                }
                            }
                        }
                        break;
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<PHLight> lights = bridge.getResourceCache().getAllLights();
        for(PHLight light : lights) {
            adapter.add(new LightItem(light));
        }
        lightListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setColorDotListener(new LightControlArrayAdapter.OnColorDotClickListener() {
            @Override
            public void onClick(PHLight light) {

                Bundle extras = new Bundle();
                extras.putString("lightID", light.getIdentifier());

                Intent intent = new Intent(getActivity(), ColorChooseActivity.class);
                intent.putExtras(extras);

                startActivityForResult(intent, COLOR_CHOOSER_ACTIVITY);
            }
        });

        adapter.setLabelClickListener(new LightControlArrayAdapter.OnLightLabelClickListener() {
            @Override
            public void onClick(final PHLight light) {

                Common.flashLight(light);

                final WhiteDialog dialog = new WhiteDialog(getActivity());
                dialog.setAcceptsInput(true);
                dialog.setPositiveButtonText("Apply");
                dialog.setDismissButtonText("Cancel");
                dialog.setMessageText("Name Your Light");
                dialog.setHideOnClick(false);

                dialog.addDialogListener(new WhiteDialog.DialogListener() {
                    @Override
                    public void dismissButtonClicked() {
                        dialog.hide();
                    }

                    @Override
                    public void positiveButtonClicked() {

                        if(!dialog.getInput().equals("")) {
                            Common.hideSoftKeyboard(getActivity(), dialog.getInputField());
                            dialog.setIsLoadingDialog(true);
                            dialog.setMessageText("Talking with Bridge");

                            light.setName(dialog.getInput());
                            PHHueSDK.getInstance().getSelectedBridge().updateLight(light, new PHLightListener() {
                                @Override
                                public void onSuccess() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.hide();
                                            refresh();
                                        }
                                    });
                                }

                                @Override
                                public void onError(int i, String s) {

                                }

                                @Override
                                public void onReceivingLightDetails(PHLight phLight) {

                                }

                                @Override
                                public void onSearchComplete() {

                                }

                                @Override
                                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                                }

                                @Override
                                public void onReceivingLights(List<PHBridgeResource> list) {

                                }
                            });
                        }
                    }
                });

                dialog.show();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == COLOR_CHOOSER_ACTIVITY) {
            Intent intent = new Intent("fragmentUpdater");
            getActivity().sendBroadcast(intent);

            refresh();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refresh();
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(fragmentReceiver);
        super.onDestroyView();
    }

    public void refresh() {
        if(isAdded()) {
            System.out.println("RE ADD");

            adapter.clear();
            groupArrayAdapter.clear();

            PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
            List<PHGroup> groups = bridge.getResourceCache().getAllGroups();
            List<PHLight> lights = bridge.getResourceCache().getAllLights();

            for(PHGroup group : groups) {
                groupArrayAdapter.add(group.getName());
            }

            for(PHGroup group : groups) {
                if(group.getName().equals(groupArrayAdapter.getItem(groupArrayAdapter.getCount() <= groupSpinner.getSelectedItemPosition() ? 0 : groupSpinner.getSelectedItemPosition()).toString())) {

                    List<String> ids = group.getLightIdentifiers();
                    for(String lightID : ids) {

                        for(PHLight light : lights) {
                            if(light.getIdentifier().equals(lightID)) {
                                adapter.add(new LightItem(light));
                            }
                        }
                    }
                    break;
                }
            }

            adapter.notifyDataSetChanged();
            groupArrayAdapter.notifyDataSetChanged();
        }
    }

    public class FragmentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }
}
