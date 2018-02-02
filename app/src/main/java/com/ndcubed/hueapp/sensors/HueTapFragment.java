package com.ndcubed.hueapp.sensors;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ndcubed.hueapp.R;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nathan on 12/22/2016.
 */

public class HueTapFragment extends Fragment {

    View rootView;

    View button1, button2, button3, button4;

    RadioButton groupRadioButton, sceneRadioButton;
    Spinner groupSpinner;
    ArrayAdapter<String> groupArrayAdapter;

    ArrayList<View> hueButtonGroup = new ArrayList<>();
    HueButtonListener hueButtonListener = new HueButtonListener();

    TextView groupLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.hue_tap_fragment_layout, container, false);

        button1 = rootView.findViewById(R.id.hueButton1);
        button2 = rootView.findViewById(R.id.hueButton2);
        button3 = rootView.findViewById(R.id.hueButton3);
        button4 = rootView.findViewById(R.id.hueButton4);

        hueButtonGroup.add(button1);
        hueButtonGroup.add(button2);
        hueButtonGroup.add(button3);
        hueButtonGroup.add(button4);

        button1.setOnClickListener(hueButtonListener);
        button2.setOnClickListener(hueButtonListener);
        button3.setOnClickListener(hueButtonListener);
        button4.setOnClickListener(hueButtonListener);

        groupLabel = (TextView)rootView.findViewById(R.id.lightLabel);
        groupLabel.setText("All Lights");

        groupRadioButton = (RadioButton)rootView.findViewById(R.id.groupRadioButton);
        sceneRadioButton = (RadioButton)rootView.findViewById(R.id.sceneRadioButton);
        groupRadioButton.setChecked(true);

        groupSpinner = (Spinner)rootView.findViewById(R.id.groupSpinner);
        groupArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dark_spinner_item);
        groupArrayAdapter.setDropDownViewResource(R.layout.simple_dropdown_spinner_item);

        PHHueSDK sdk = PHHueSDK.getInstance();
        PHBridge bridge = sdk.getSelectedBridge();
        List<PHGroup> groups = bridge.getResourceCache().getAllGroups();

        for(PHGroup group : groups) {
            groupArrayAdapter.add(group.getName());
        }
        groupArrayAdapter.notifyDataSetChanged();
        groupSpinner.setAdapter(groupArrayAdapter);

        setHueButtonSelected(R.id.hueButton1);

        return rootView;
    }

    private void setBackgroundDrawable(View view, int drawableID) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackground(getResources().getDrawable(drawableID, getContext().getTheme()));
        } else {
            view.setBackgroundDrawable( getResources().getDrawable(drawableID));
        }
    }

    private void setHueButtonSelected(int hueButtonID) {

        int i = 0;

        for(View button : hueButtonGroup) {
            if(button.getId() == hueButtonID) {

                switch(i) {
                    case 0:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_one_sel);
                        break;
                    case 1:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_two_sel);
                        break;
                    case 2:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_three_sel);
                        break;
                    case 3:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_four_sel);
                        break;
                }
            } else {
                switch(i) {
                    case 0:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_one);
                        break;
                    case 1:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_two);
                        break;
                    case 2:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_three);
                        break;
                    case 3:
                        setBackgroundDrawable(button, R.drawable.hue_tap_button_four);
                        break;
                }
            }

            i++;
        }
    }

    class HueButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            setHueButtonSelected(view.getId());
        }
    }
}
