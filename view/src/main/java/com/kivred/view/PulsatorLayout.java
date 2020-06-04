package com.kived.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.kived.view.R;

import java.util.ArrayList;
import java.util.List;


public class PulsatorLayout extends RelativeLayout {

    public static final int INFINITE = 0;
    public static final int INTERP_ACCELERATE = 1;
    public static final int INTERP_DECELERATE = 2;
    public static final int INTERP_ACCELERATE_DECELERATE = 3;

    private final List<View> mViews = new ArrayList<>();
    private int mCircleCount;
    private int mAnimDuration;
    private int mAnimEndless;
    private int mStockWidth;
    private boolean mStartFromScratch;
    private int mCircleColor;
    private int mInterpolator;
    private AnimatorSet mAnimatorSet;
    private Paint mPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private Paint.Style paintStyle = Paint.Style.FILL;
    private boolean mIsStarted;


    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            mIsStarted = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mIsStarted = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mIsStarted = false;
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }

    };


    public PulsatorLayout(Context context) {
        this(context, null, 0);
    }

    public PulsatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PulsatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PulsatorStyleable, 0, 0);

        mCircleCount = 4;
        mAnimDuration = 7000;
        mAnimEndless = 0;
        mStockWidth = 15;
        mStartFromScratch = true;
        mCircleColor = Color.parseColor("#0074C1");
        mInterpolator = 0;

        try {
            mCircleCount = attr.getInteger(R.styleable.PulsatorStyleable_ps_count, mCircleCount);
            mAnimDuration = attr.getInteger(R.styleable.PulsatorStyleable_ps_duration, mAnimDuration);
            mAnimEndless = attr.getInteger(R.styleable.PulsatorStyleable_ps_repeat, mAnimEndless);
            mStartFromScratch = attr.getBoolean(R.styleable.PulsatorStyleable_ps_startFromScratch, mStartFromScratch);
            mCircleColor = attr.getColor(R.styleable.PulsatorStyleable_ps_color, mCircleColor);
            mInterpolator = attr.getInteger(R.styleable.PulsatorStyleable_ps_interpolator, mInterpolator);
            mStockWidth = attr.getInteger(R.styleable.PulsatorStyleable_ps_stroke_width, mStockWidth);
            int pS = attr.getInteger(R.styleable.PulsatorStyleable_ps_style, 0);
            if (pS == 0) {
                paintStyle = Paint.Style.FILL;
            } else if (pS == 1) {
                paintStyle = Paint.Style.STROKE;
            } else if (pS == 2) {
                paintStyle = Paint.Style.FILL_AND_STROKE;
            }
        } finally {
            attr.recycle();
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(paintStyle);
        mPaint.setStrokeWidth(mStockWidth);
        mPaint.setColor(mCircleColor);
        build();
    }

    private static Interpolator createInterpolator(int type) {
        switch (type) {
            case INTERP_ACCELERATE:
                return new AccelerateInterpolator();
            case INTERP_DECELERATE:
                return new DecelerateInterpolator();
            case INTERP_ACCELERATE_DECELERATE:
                return new AccelerateDecelerateInterpolator();
            default:
                return new LinearInterpolator();
        }
    }

    public synchronized void start() {
        if (mAnimatorSet == null || mIsStarted) {
            return;
        }

        mAnimatorSet.start();

        if (!mStartFromScratch) {
            ArrayList<Animator> animators = mAnimatorSet.getChildAnimations();
            for (Animator animator : animators) {
                ObjectAnimator objectAnimator = (ObjectAnimator) animator;

                long delay = objectAnimator.getStartDelay();
                objectAnimator.setStartDelay(0);
                objectAnimator.setCurrentPlayTime(mAnimDuration - delay);
            }
        }
    }

    public synchronized void stop() {
        if (mAnimatorSet == null || !mIsStarted) {
            return;
        }

        mAnimatorSet.end();
    }

    public synchronized boolean isStarted() {
        return (mAnimatorSet != null && mIsStarted);
    }

    public int getDuration() {
        return mAnimDuration;
    }

    public void setDuration(int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        if (millis != mAnimDuration) {
            mAnimDuration = millis;
            reset();
            invalidate();
        }
    }

    public int getColor() {
        return mCircleColor;
    }

    public void setColor(int color) {
        if (color != mCircleColor) {
            this.mCircleColor = color;

            if (mPaint != null) {
                mPaint.setColor(color);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        mCenterX = width * 0.5f;
        mCenterY = height * 0.5f;
        mRadius = Math.min(width, height) * 0.5f;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void clear() {
        stop();
        for (View view : mViews) {
            removeView(view);
        }
        mViews.clear();
    }

    private void build() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        int repeatCount = (mAnimEndless == INFINITE) ? ObjectAnimator.INFINITE : mAnimEndless;
        List<Animator> animators = new ArrayList<>();
        for (int index = 0; index < mCircleCount; index++) {
            PulseView pulseView = new PulseView(getContext());
            pulseView.setScaleX(0);
            pulseView.setScaleY(0);
            pulseView.setAlpha(1);

            addView(pulseView, index, layoutParams);
            mViews.add(pulseView);

            long delay = index * mAnimDuration / mCircleCount;

            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(pulseView, "ScaleX", 0f, 1f);
            scaleXAnimator.setRepeatCount(repeatCount);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(delay);
            animators.add(scaleXAnimator);

            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(pulseView, "ScaleY", 0f, 1f);
            scaleYAnimator.setRepeatCount(repeatCount);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(delay);
            animators.add(scaleYAnimator);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(pulseView, "Alpha", 1f, 0f);
            alphaAnimator.setRepeatCount(repeatCount);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(delay);
            animators.add(alphaAnimator);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.setInterpolator(createInterpolator(mInterpolator));
        mAnimatorSet.setDuration(mAnimDuration);
        mAnimatorSet.addListener(mAnimatorListener);
    }

    private void reset() {
        boolean isStarted = isStarted();
        clear();
        build();

        if (isStarted) {
            start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
    }

    private class PulseView extends View {
        public PulseView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        }
    }
}
