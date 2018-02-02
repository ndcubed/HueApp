package com.ndcubed.hueapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ndcubed.hueapp.R;

/**
 * Created by neptu on 6/1/2016.
 */
public class EffectsFragment extends Fragment {

    View rootView;

    Spinner presetsSpinner;
    ArrayAdapter<CharSequence> presetsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.effects_fragment_layout, container, false);

        presetsSpinner = (Spinner)rootView.findViewById(R.id.presetsSpinner);
        presetsAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_dropdown_item);

        presetsAdapter.add("Flow");
        presetsAdapter.add("Candle");
        presetsAdapter.add("Strobe");
        presetsAdapter.add("Color Transition");

        presetsSpinner.setAdapter(presetsAdapter);

        return rootView;
    }
}
