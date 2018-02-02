package com.ndcubed.hueapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.ndcubed.hueapp.R;
import com.ndcubed.nappsupport.views.DotPageIndicator;
import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GroupActivity extends Activity {

    ViewPager pager;
    ScreenSlidePagerAdapter pagerAdapter;
    DotPageIndicator dotPageIndicator;

    GroupColorFragment colorFragment;
    GroupLightsFragment lightsFragment;
    GroupNameFragment nameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.argb(10, 0, 0, 0));

            int softButtonHeight = com.ndcubed.nappsupport.utils.Common.getSoftButtonHeight(this);

            View indicator = findViewById(R.id.dotIndicator);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)indicator.getLayoutParams();

            params.bottomMargin = (int)(Common.dpToPx(this, 20f) + softButtonHeight);

            indicator.setLayoutParams(params);
        }

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setOffscreenPageLimit(4);
        pagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();

        dotPageIndicator = (DotPageIndicator)findViewById(R.id.dotIndicator);
        dotPageIndicator.setViewPager(pager);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                if (i + 1 == pager.getAdapter().getCount()) {
                    findViewById(R.id.doneButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.arrowNextButton).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.doneButton).setVisibility(View.GONE);
                    findViewById(R.id.arrowNextButton).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        findViewById(R.id.arrowNextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager.getCurrentItem() + 1 < pager.getAdapter().getCount()) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
            }
        });

        findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PHGroup group = new PHGroup();
                group.setName(nameFragment.getGroupName());
                group.setLightIdentifiers(lightsFragment.getLightIDs());

                final PHHueSDK sdk = PHHueSDK.getInstance();
                sdk.getSelectedBridge().createGroup(group, new PHGroupListener() {
                    @Override
                    public void onCreated(PHGroup phGroup) {
                        SharedPreferences prefs = getSharedPreferences("HueAppPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor e = prefs.edit();

                        e.putInt("group" + phGroup.getIdentifier() + "Color", colorFragment.getColor());
                        e.commit();

                        finish();
                    }

                    @Override
                    public void onReceivingGroupDetails(PHGroup phGroup) {

                    }

                    @Override
                    public void onReceivingAllGroups(List<PHBridgeResource> list) {

                    }

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
            }
        });

        //create temp group
        PHHueSDK sdk = PHHueSDK.getInstance();
        List<PHLight> lights = sdk.getSelectedBridge().getResourceCache().getAllLights();
        List<String> ids = new ArrayList<String>();
        ids.add(lights.get(0).getIdentifier());

        PHGroup group = new PHGroup();
        group.setName("HueAppTempGroup");
        group.setLightIdentifiers(ids);

        sdk.getSelectedBridge().createGroup(group, new PHGroupListener() {
            @Override
            public void onCreated(PHGroup phGroup) {
                SharedPreferences prefs = getSharedPreferences("HueAppPreferences", MODE_PRIVATE);
                SharedPreferences.Editor e = prefs.edit();
                e.putString("tempGroupID", phGroup.getIdentifier());
                e.apply();
            }

            @Override
            public void onReceivingGroupDetails(PHGroup phGroup) {

            }

            @Override
            public void onReceivingAllGroups(List<PHBridgeResource> list) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        SharedPreferences prefs = getSharedPreferences("HueAppPreferences", MODE_PRIVATE);
        String tempGroupID = prefs.getString("tempGroupID", "null");

        PHHueSDK sdk = PHHueSDK.getInstance();
        sdk.getSelectedBridge().deleteGroup(tempGroupID, null);

        sdk = null;

        super.onDestroy();
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 0) {
                lightsFragment = new GroupLightsFragment();
                return lightsFragment;
            } else if(position == 1) {
                colorFragment = new GroupColorFragment();
                return colorFragment;
            } else if(position == 2) {
                nameFragment = new GroupNameFragment();
                return nameFragment;
            } else {
                return new GroupNameFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            String pageTitle = "Lights";

            if(position == 0) {
                pageTitle = "Lights";
            } else if(position == 1) {
                pageTitle = "Color";
            } else if(position == 2) {
                pageTitle = "Groups";
            } else if(position == 3) {
                pageTitle = "Scenes";
            }

            return pageTitle;
        }
    }
}
