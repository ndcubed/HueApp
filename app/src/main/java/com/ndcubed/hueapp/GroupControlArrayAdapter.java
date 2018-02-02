package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.ColorDot;
import com.ndcubed.nappsupport.views.RoundedSwitch;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.List;

public class GroupControlArrayAdapter extends ArrayAdapter<GroupItem> {

    Activity context;
    int textViewResourceId;
    PHHueSDK sdk = PHHueSDK.getInstance();

    private OnLightLabelClickListener labelClickListener;
    private OnColorDotClickListener colorDotClickListener;
    private OnLightSwitchClickedListener lightSwitchClickedListener;

    public GroupControlArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = (Activity)context;
        this.textViewResourceId = textViewResourceId;
    }

    public void setLabelClickListener(OnLightLabelClickListener labelClickListener) {
        this.labelClickListener = labelClickListener;
    }

    public void setLightSwitchClickedListener(OnLightSwitchClickedListener lightSwitchClickedListener) {
        this.lightSwitchClickedListener = lightSwitchClickedListener;
    }

    public void setColorDotClickListener(OnColorDotClickListener listener) {
        this.colorDotClickListener = listener;
    }

    private void fireColorDotClickedEvent(PHGroup group, ColorDot dot) {
        if(colorDotClickListener != null) {
            colorDotClickListener.onClick(group, dot.getDotColor());
        }
    }

    private void fireOnLightLabelEvent(PHLight light) {
        if(labelClickListener != null) {
            labelClickListener.onClick(light);
        }
    }

    private void fireLightSwitchedClickedEvent(int lightSwitchState) {
        if(lightSwitchClickedListener != null) {
            lightSwitchClickedListener.onClick(lightSwitchState);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final GroupItem groupItem = getItem(position);
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(textViewResourceId, null);

            Holder holder = new Holder();
            holder.groupLabel = (TextView)rowView.findViewById(R.id.lightLabel);
            holder.brightnessBar = (SeekBar)rowView.findViewById(R.id.brightnessBar);
            holder.lightSwitch = (RoundedSwitch)rowView.findViewById(R.id.lightSwitch);
            holder.colorDot = (ColorDot)rowView.findViewById(R.id.colorDot);

            rowView.setTag(holder);
        }

        final Holder holder = (Holder)rowView.getTag();
        holder.groupLabel.setText(groupItem.getGroup().getName());
        holder.lightSwitch.setRoundedSwitchListener(null);
        holder.lightSwitch.setSwitchState(isGroupOn(groupItem.getGroup(), holder.colorDot.getDotColor()) ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        holder.brightnessBar.setOnSeekBarChangeListener(null);
        holder.brightnessBar.setProgress(getGroupBrightness(groupItem.getGroup()));
        holder.colorDot.setDotColors(groupItem.getColors());

        holder.colorDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireColorDotClickedEvent(groupItem.getGroup(), holder.colorDot);
            }
        });

        holder.lightSwitch.setRoundedSwitchListener(new RoundedSwitch.RoundedSwitchListener() {
            @Override
            public void onPress() {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onStateChange(int switchState) {
                if (switchState == RoundedSwitch.STATE_OFF) {

                    PHLightState state = new PHLightState();
                    state.setOn(false);

                    sdk.getSelectedBridge().setLightStateForGroup(groupItem.getGroup().getIdentifier(), state);
                } else {
                    PHLightState state = new PHLightState();
                    state.setOn(true);

                    LightUtils.updateLightState(sdk, groupItem.getGroup(), holder.brightnessBar.getProgress());
                }

                fireLightSwitchedClickedEvent(switchState);
            }
        });

        holder.brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                PHLightState lightState = new PHLightState();
                lightState.setBrightness(seekBar.getProgress());
                lightState.setOn(seekBar.getProgress() > 1);

                sdk.getSelectedBridge().setLightStateForGroup(groupItem.getGroup().getIdentifier(), lightState);
            }
        });

        return rowView;
    }

    public static class Holder {
        TextView groupLabel;
        SeekBar brightnessBar;
        SimpleColorButton lightToggleButton;
        ColorDot colorDot;
        RoundedSwitch lightSwitch;
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public boolean isGroupOn(PHGroup group, int color) {


        List<String> ids = group.getLightIdentifiers();
        PHBridge bridge = sdk.getSelectedBridge();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        for(PHLight light : lights) {
            for(String groupLight : ids) {
                if(groupLight.equals(light.getIdentifier()) && light.getLastKnownLightState().isOn()) {
                    return true;
                    /*
                    PHLightState state = light.getLastKnownLightState();
                    float[] xy = PHUtilities.calculateXY(color, light.getModelNumber());
                    color = PHUtilities.colorFromXY(xy, light.getModelNumber());

                    int lightColor = PHUtilities.colorFromXY(new float[]{state.getX(), state.getY()}, light.getModelNumber());
                    System.out.println("COLORS: " + Color.red(color) + " " + Color.green(color) + " " + Color.blue(color) + " " + Color.red(lightColor) + " " + Color.green(lightColor) + " " + Color.blue(lightColor) + areColorsSimilar(color, lightColor));
                    return areColorsSimilar(color, lightColor);
                     */
                }
            }
        }

        return false;
    }

    public boolean areColorsSimilar(int colorA, int colorB) {
        //red 50
        // red > 45 but red < 55
        boolean r = Color.red(colorA) > Color.red(colorB) - 5 && Color.red(colorA) < Color.red(colorB) + 5;
        boolean g = Color.green(colorA) > Color.green(colorB) - 5 && Color.green(colorA) < Color.green(colorB) + 5;
        boolean b = Color.blue(colorA) > Color.blue(colorB) - 5 && Color.blue(colorA) < Color.blue(colorB) + 5;

        return r && g && b;
    }

    public int getGroupBrightness(PHGroup group) {

        List<String> ids = group.getLightIdentifiers();
        PHBridge bridge = sdk.getSelectedBridge();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();

        for(PHLight light : lights) {
            for(String groupLight : ids) {
                if(groupLight.equals(light.getIdentifier())) {
                    return light.getLastKnownLightState().getBrightness();
                }
            }
        }

        return 0;
    }

    interface OnColorDotClickListener {
        void onClick(PHGroup group, int color);
    }

    interface OnLightLabelClickListener {
        void onClick(PHLight light);
    }

    interface OnLightSwitchClickedListener {
        void onClick(int lightSwitchState);
    }
}
