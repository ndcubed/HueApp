package com.ndcubed.hueapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Nathan on 12/15/2016.
 */

public class FixedAspectHueTapButton extends RelativeLayout {

    public FixedAspectHueTapButton(Context context) {
        super(context);
    }

    public FixedAspectHueTapButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedAspectHueTapButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int size = Math.min(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(size, size);
    }
}
