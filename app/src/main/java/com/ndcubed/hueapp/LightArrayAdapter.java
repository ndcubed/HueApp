package com.ndcubed.hueapp;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.ndcubed.hueapp.R;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;

public class LightArrayAdapter extends ArrayAdapter<LightItem> {

    Activity context;
    int textViewResourceId;

    private LightSelectionListener listener;

    public LightArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = (Activity)context;
        this.textViewResourceId = textViewResourceId;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final LightItem lightItem = getItem(position);
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(textViewResourceId, null);

            Holder holder = new Holder();
            holder.checkBox = (CheckBox)rowView.findViewById(R.id.checkBox);

            rowView.setTag(holder);
        }

        final Holder holder = (Holder)rowView.getTag();
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(lightItem.isSelected());
        holder.checkBox.setText(lightItem.getLight().getName());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lightItem.setSelected(isChecked);
                if(listener != null) {
                    listener.lightSelected(lightItem.getLight());
                }
            }
        });

        return rowView;
    }

    public static class Holder {
        CheckBox checkBox;
    }

    public float dpToPx(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public void setLightSelectionListener(LightSelectionListener listener) {
        this.listener = listener;
    }

    interface LightSelectionListener {
        void lightSelected(PHLight light);
    }
}
