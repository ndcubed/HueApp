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
import com.ndcubed.nappsupport.views.ColorDot;
import com.ndcubed.nappsupport.views.RadialColorIndicator;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHScene;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ndcubed.hueapp.R;

public class SceneControlArrayAdapter extends ArrayAdapter<SceneItem> {

    Activity context;
    int textViewResourceId;

    private OnSceneClickListener listener;

    public SceneControlArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = (Activity)context;
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        final SceneItem sceneItem = getItem(position);

        if(rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(textViewResourceId, null);

            Holder holder = new Holder();
            holder.sceneLabel = (TextView)rowView.findViewById(R.id.sceneLabel);
            holder.rootView = rowView.findViewById(R.id.rootView);
            holder.radialColorIndicator = (RadialColorIndicator)rowView.findViewById(R.id.radialColorIndicator);

            rowView.setTag(holder);
        }

        Holder holder = (Holder)rowView.getTag();

        holder.sceneLabel.setText(sceneItem.getScene().getName());
        holder.radialColorIndicator.update();

        PHScene scene = sceneItem.getScene();
        Map<String, PHLightState> lightStateMap = scene.getLightStates();

        if(lightStateMap != null) {
            System.out.println("NOT NULL");
            for (Map.Entry<String, PHLightState> entry : lightStateMap.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }
        } else {
            System.out.println("NULL: " + scene.getName());
        }

        return rowView;
    }

    public void setOnSceneClickListener(OnSceneClickListener listener) {
        this.listener = listener;
    }

    private void fireOnSceneClick(PHScene scene) {
        if(listener != null) {
            listener.onClick(scene);
        }
    }

    public static class Holder {

        TextView sceneLabel;
        View rootView;
        RadialColorIndicator radialColorIndicator;
    }

    interface OnSceneClickListener {
        public void onClick(PHScene scene);
    }
}
