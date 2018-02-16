package com.ndcubed.hueapp;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by neptu on 9/2/2016.
 */
public class SideView extends LinearLayout {

    private float pressX, newX, oldX, dX;
    private boolean isDragging = false;
    private View triggerView, animatedChildContainer;
    private Paint paint = new Paint();

    public SideView(Context context) {
        super(context);
        init();
    }

    public SideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //test
    private void init() {
        setWillNotDraw(false);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setTranslationX(-getWidth());
                setVisibility(View.GONE);
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()) {

                    case MotionEvent.ACTION_DOWN: {
                        System.out.println("DOWN");
                        isDragging = false;
                        pressX = event.getRawX();
                        oldX = event.getRawX();
                        newX = oldX;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        System.out.println("UP");
                        if(isDragging) {
                            show(!((pressX - event.getRawX()) > Common.dpToPx(getContext(), 80f)));
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {

                        newX = event.getRawX();
                        dX = newX - oldX;

                        if(Math.abs(dX) > Common.dpToPx(getContext(), 30f) && !isDragging) {
                            isDragging = true;
                            oldX = newX;
                            dX = 0;
                        } else if(isDragging) {
                            oldX = newX;
                            setTranslationX(Math.min(0, getTranslationX() + dX));
                        }
                        break;
                    }
                }
                invalidate();
                return true;
            }
        });
    }

    private float getPercent() {

        return Math.max(0f, ((getPaddedWidth() - Math.abs(getTranslationX())) / getPaddedWidth()));
    }

    private int getPaddedWidth() {
        return getWidth() - getPaddingRight();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("EVENT: " + event.getAction());
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                System.out.println("DOWN");
                isDragging = false;
                pressX = event.getRawX();
                oldX = event.getRawX();
                newX = oldX;
                break;
            }
            case MotionEvent.ACTION_UP: {
                System.out.println("UP");
                if(isDragging) {
                    show(dX > 0);
                }
                invalidate();
                return isDragging;
            }
            case MotionEvent.ACTION_MOVE: {

                newX = event.getRawX();
                dX = newX - oldX;

                if(Math.abs(dX) > Common.dpToPx(getContext(), 30f) && !isDragging) {
                    isDragging = true;
                    oldX = newX;
                    dX = 0;
                } else if(isDragging) {
                    oldX = newX;
                    setTranslationX(Math.min(0, getTranslationX() + dX));
                }
                break;
            }
        }

        invalidate();
        return false;
    }

    boolean isSideViewVisible() {
        return getVisibility() == View.VISIBLE;
    }

    void setSideViewVisible(boolean b) {
        if(b) {
            if(animatedChildContainer != null) animatedChildContainer.setTranslationX(-Common.dpToPx(getContext(), 115f));
            setTranslationX(-getPaddedWidth());
            setVisibility(View.VISIBLE);
        }
        show(b);
    }

    private void show() {
        show(getTranslationX() > -(((float)getWidth()) / 1.5f));
    }

    private void show(boolean b) {

        if(!b) {
            ValueAnimator animator = ValueAnimator.ofFloat(getTranslationX(), -getPaddedWidth());
            animator.setInterpolator(new DecelerateInterpolator(2f));
            animator.setDuration(350);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTranslationX((Float)animation.getAnimatedValue());
                    invalidate();
                }
            });

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(getTranslationX(), 0f);
            animator.setInterpolator(new DecelerateInterpolator(2.2f));
            animator.setDuration(550);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTranslationX((Float)animation.getAnimatedValue());
                    invalidate();
                }
            });
            animator.start();

            if(animatedChildContainer != null) {
                ValueAnimator childAnimator = ValueAnimator.ofFloat(animatedChildContainer.getTranslationX(), 0f);
                childAnimator.setInterpolator(new DecelerateInterpolator(2.2f));
                childAnimator.setDuration(550);
                childAnimator.setStartDelay(100);
                childAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animatedChildContainer.setTranslationX((Float)animation.getAnimatedValue());
                    }
                });
                childAnimator.start();
            }

            /*
            int colorFrom = Common.getAttributeColor(getContext(), R.attr.actionBarColor);
            int colorTo = Color.rgb(222, 222, 222);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = ((Activity)getContext()).getWindow();
                        window.setStatusBarColor((Integer)animator.getAnimatedValue());
                    }
                }

            });
            colorAnimation.start();
            */
        }
    }

    void setTriggerView(View triggerView) {
        if(this.triggerView != null) {
            this.triggerView.setOnTouchListener(null);
        }

        this.triggerView = triggerView;
        triggerView.setOnTouchListener(new SideViewTriggerListener());
    }

    void setAnimatedChildContainer(View container) {
        animatedChildContainer = container;
    }

    private class SideViewTriggerListener implements OnTouchListener {

        float pressX, newX, oldX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if(animatedChildContainer != null) animatedChildContainer.setTranslationX(-Common.dpToPx(getContext(), 115f));
                setTranslationX(-getPaddedWidth());
                setVisibility(View.VISIBLE);

                pressX = event.getX();
                oldX = event.getX();
                newX = oldX;

            } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
                float translateX = event.getX() - pressX;

                newX = event.getX();

                setTranslationX(Math.min(0, (-getPaddedWidth()) + translateX));
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                show();
            }

            if(animatedChildContainer != null) {
                animatedChildContainer.setTranslationX(Common.dpToPx(getContext(), (115f * getPercent()) - 115f));
            }

            invalidate();
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(Color.rgb(0, 151, 174));
        paint.setAlpha((int)(200f * getPercent()));

        canvas.translate(-(getTranslationX()), 0);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        canvas.translate(getTranslationX(), 0);

        if(animatedChildContainer != null) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, animatedChildContainer.getWidth(), animatedChildContainer.getHeight(), paint);
        }

        super.onDraw(canvas);
    }
}
