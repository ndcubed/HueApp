package com.ndcubed.hueapp;
import com.ndcubed.hueapp.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class StartupWizard extends Activity {

    ViewPager pager;
    ScreenSlidePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.startup_wizard_layout);

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
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

            System.out.println("POSITION: -------------    " + position);

            if(position == 0) {
                return new BridgeFragment();
            } else if(position == 1) {
                return new LightsFragment();
            } else if(position == 2) {
                return new ColorFragment();
            } else if(position == 3) {
                return new ScenesFragment();
            } else {
                return new BridgeFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            String pageTitle = "Bridge";

            if(position == 0) {
                pageTitle = "Bridge";
            } else if(position == 1) {
                pageTitle = "Lights";
            } else if(position == 2) {
                pageTitle = "Groups";
            } else if(position == 3) {
                pageTitle = "Scenes";
            }

            return pageTitle;
        }
    }
}
