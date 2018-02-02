package com.ndcubed.hueapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Nathan on 12/15/2016.
 */

public class FixedAspectRelativeLayout extends RelativeLayout {

    public FixedAspectRelativeLayout(Context context) {
        super(context);
    }

    public FixedAspectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedAspectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
