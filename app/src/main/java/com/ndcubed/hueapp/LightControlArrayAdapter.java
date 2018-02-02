package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.ColorDot;
import com.ndcubed.nappsupport.views.RoundedSwitch;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;

public class LightControlArrayAdapter extends ArrayAdapter<LightItem> {

    Activity context;
    int textViewResourceId;

    private OnColorDotClickListener listener;
    private OnLightLabelClickListener labelClickListener;

    public LightControlArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = (Activity)context;
        this.textViewResourceId = textViewResourceId;
    }

    public void setColorDotListener(OnColorDotClickListener listener) {
        this.listener = listener;
    }

    private void fireColorDotClicked(PHLight light) {
        if(listener != null) {
            listener.onClick(light);
        }
    }

    public void setLabelClickListener(OnLightLabelClickListener labelClickListener) {
        this.labelClickListener = labelClickListener;
    }

    private void fireOnLightLabelEvent(PHLight light) {
        if(labelClickListener != null) {
            labelClickListener.onClick(light);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final LightItem lightItem = getItem(position);
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(textViewResourceId, null);

            Holder holder = new Holder();
            holder.lightLabel = (TextView)rowView.findViewById(R.id.lightLabel);
            holder.brightnessBar = (SeekBar)rowView.findViewById(R.id.brightnessBar);
            holder.lightToggleButton = (SimpleColorButton)rowView.findViewById(R.id.lightToggleButton);
            holder.colorDot = (ColorDot)rowView.findViewById(R.id.colorDot);
            holder.lightSwitch = (RoundedSwitch)rowView.findViewById(R.id.lightSwitch);

            rowView.setTag(holder);
        }

        final Holder holder = (Holder)rowView.getTag();
        holder.lightLabel.setText(lightItem.getLight().getName());
        holder.lightToggleButton.setText(lightItem.getLight().getLastKnownLightState().isOn() ? "On" : "Off");
        holder.lightSwitch.setSwitchStateWithoutAnimation(lightItem.getLight().getLastKnownLightState().isOn() ? RoundedSwitch.STATE_ON : RoundedSwitch.STATE_OFF);
        holder.brightnessBar.setOnSeekBarChangeListener(null);
        holder.brightnessBar.setProgress(lightItem.getLight().getLastKnownLightState().getBrightness());

        float[] xy = {lightItem.getLight().getLastKnownLightState().getX(), lightItem.getLight().getLastKnownLightState().getY()};

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

                    PHHueSDK sdk = PHHueSDK.getInstance();
                    sdk.getSelectedBridge().updateLightState(lightItem.getLight(), state);
                } else {
                    PHLightState state = new PHLightState();
                    state.setOn(true);

                    PHHueSDK sdk = PHHueSDK.getInstance();
                    sdk.getSelectedBridge().updateLightState(lightItem.getLight(), state);
                }
            }
        });

        holder.colorDot.setDotColor(PHUtilities.colorFromXY(xy, lightItem.getLight().getModelNumber()));
        holder.colorDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireColorDotClicked(lightItem.getLight());
            }
        });

        holder.lightLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireOnLightLabelEvent(lightItem.getLight());
            }
        });

        holder.brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            LightStateSetter setter = new LightStateSetter(lightItem.getLight(), 1);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setter.setBrightness(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setter = new LightStateSetter(lightItem.getLight(), seekBar.getProgress());
                setter.start();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setter.setRunning(false);
                setter = null;

                PHLightState state = new PHLightState();
                state.setBrightness(seekBar.getProgress());
                PHHueSDK sdk = PHHueSDK.getInstance();
                sdk.getSelectedBridge().updateLightState(lightItem.getLight(), state);
            }
        });


        return rowView;
    }

    private class LightStateSetter extends Thread {

        private PHHueSDK sdk = PHHueSDK.getInstance();
        private PHBridge bridge = sdk.getSelectedBridge();

        private boolean running = true;
        private int brightness = 1;
        private PHLight light;

        private LightStateSetter(PHLight light, int brightness) {
            this.light = light;
            this.brightness = brightness;
        }

        public boolean isRunning() {
            return running;
        }

        private void setRunning(boolean running) {
            this.running = running;
        }

        public int getBrightness() {
            return brightness;
        }

        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        public PHLight getLight() {
            return light;
        }

        public void setLight(PHLight light) {
            this.light = light;
        }

        @Override
        public void run() {

            while(running) {

                PHLightState state = new PHLightState();
                state.setBrightness(getBrightness());
                bridge.updateLightState(getLight(), state, null);

                try {
                    Thread.sleep(1000);
                } catch(Exception err){}
            }

            sdk = null;
            bridge = null;

            System.out.println("STOP SEEK");
        }
    }

    public static class Holder {
        TextView lightLabel;
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

    interface OnColorDotClickListener {
        public void onClick(PHLight light);
    }

    interface OnLightLabelClickListener {
        public void onClick(PHLight light);
    }
}
