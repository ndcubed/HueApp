package com.ndcubed.hueapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ndcubed.hueapp.R;

/**
 * Created by neptu on 5/25/2016.
 */
public class GroupNameFragment extends Fragment {

    View rootView;
    EditText groupNameField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.group_creator_name_layout, container, false);

        groupNameField = (EditText)rootView.findViewById(R.id.groupNameField);

        return rootView;
    }

    public String getGroupName() {
        return groupNameField.getText().toString();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            groupNameField.requestFocusFromTouch();
            Common.showSoftKeyboard(getActivity(), groupNameField);
        }
    }
}
