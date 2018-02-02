package com.ndcubed.hueapp.sensors;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ndcubed.hueapp.Common;
import com.ndcubed.hueapp.R;

/**
 * Created by Nathan on 12/20/2016.
 */

public class SensorsActivity extends FragmentActivity {

    FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensors_activity_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Common.getAttributeColor(this, R.attr.actionBarColor));
        }


        if(findViewById(R.id.fragmentContainer) != null) {
            fragmentContainer = (FrameLayout)findViewById(R.id.fragmentContainer);

            if(savedInstanceState != null) {
                return;
            }

            HueTapFragment hueTapFragment = new HueTapFragment();

            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, hueTapFragment).commit();
        }
    }
}
