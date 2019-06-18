package com.dodge.testapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by linzheng on 2019/6/13.
 */

public class MyScrollLayout extends FrameLayout implements NestedScrollingParent2 {

    public static final String TAG = "MyScrollLayout";

    public static final int STATUS_NULL = 0;
    public static final int STATUS_PULL = 1;
    public static final int STATUS_RESET = 2;
    public static final int STATUS_REFRESH = 3;
    public static final int STATUS_BREAK = 4;

    private int mStatus = STATUS_NULL;

    private View mRootView;
    private ViewGroup mListParent;
    private View mHeadView;
    private RecyclerView mHeadRv;
    private RecyclerView mRecyclerView;

    private ViewGroup mRefreshLayout;
    private IRefreshView mRefreshView;


    private int mParentHeight;
    private int mMaxHeadHeight;
    private int mMinHeadHeight;

    private int mMaxPullHeight;
    private int mRefreshHeight;
    private int mBreakHeight;

    private OnRefreshListener mRefreshListener;

    private final NestedScrollingParentHelper mNsParentHelper = new NestedScrollingParentHelper(this);


    public MyScrollLayout(@NonNull Context context) {
        super(context);
        initView();
    }


    public MyScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MyScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mRootView = inflater.inflate(R.layout.my_scroll_layout, this, false);
        mListParent = mRootView.findViewById(R.id.recycler_layout);

        mHeadView = mRootView.findViewById(R.id.head_view);
        mHeadRv = mRootView.findViewById(R.id.recycler_view_head);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view_content);

        mRefreshLayout = mRootView.findViewById(R.id.refresh_layout);
        mRefreshView = mRootView.findViewById(R.id.refresh_head);

        mMaxHeadHeight = ScreenUtil.dip2px(100);
        mMinHeadHeight = mMaxHeadHeight / 2;

        mMaxPullHeight = ScreenUtil.dip2px(120);
        mRefreshHeight = ScreenUtil.dip2px(64);
        mBreakHeight = ScreenUtil.dip2px(96);

        addView(mRootView);
    }

    public RecyclerView getRecyclerView0() {
        return mHeadRv;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "onMeasure: ");

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return;
        }

        if (mParentHeight == 0) {
            mParentHeight = getMeasuredHeight();
        }

        if (mParentHeight > 0) {
            initHeight();
            if (getChildCount() > 0) {
                final View child = getChildAt(0);
                $measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
    }

    protected void $measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    public void initHeight() {
        Log.d(TAG, "initHeight: ");
        int height = getMeasuredHeight();
        if (height == 0) {
            return;
        }
        mRecyclerView.getLayoutParams().height = height - mMinHeadHeight;
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
        Log.d(TAG, "measureChildWithMargins: ");
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes, @ViewCompat.ScrollAxis int type) {
        Log.d(TAG, "onStartNestedScroll: axes = " + axes);
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes, @ViewCompat.ScrollAxis int type) {
        mNsParentHelper.onNestedScrollAccepted(child, target, axes, type);
        Log.d(TAG, "onNestedScrollAccepted: axes = " + axes);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, @ViewCompat.ScrollAxis int type) {
        mNsParentHelper.onStopNestedScroll(target, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        if (dyUnconsumed > 0) {
            handleScrollUp(dyUnconsumed, null, type);
        } else if (dyUnconsumed < 0) {
            handleScrollDown(target, dyUnconsumed, type);
        }
        Log.d(TAG, "onNestedScroll: dyConsumed = " + dyConsumed);
        Log.d(TAG, "onNestedScroll: dyUnconsumed = " + dyUnconsumed);
    }


    private boolean mHeadRvClosed = false;

    private void handleScrollDown(View target, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        if (mHeadRvClosed) {
            if (type == ViewCompat.TYPE_NON_TOUCH) {
                if (target instanceof RecyclerView) {
                    ((RecyclerView) target).stopScroll();
                }
            } else {
                if (mStatus != STATUS_NULL && mStatus != STATUS_PULL) {
                    return;
                }
                float y = mRefreshLayout.getTranslationY();
                if (y < mMaxPullHeight) {
                    mStatus = STATUS_PULL;
                    float offset = 0.5f * dyUnconsumed;
                    offset = Math.min(-offset, mMaxPullHeight - y);
                    final float translationY = offset + y;
                    mListParent.setTranslationY(translationY);
                    mRefreshLayout.setTranslationY(translationY);
                    if (translationY >= mBreakHeight) {
                        onRefreshViewBreak();
                    } else if (translationY >= mRefreshHeight) {
                        mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
                    } else {
                        mRefreshView.setStatus(IRefreshView.STATUS_PULL);
                    }
                }
            }
        } else {
            int oldY = mListParent.getScrollY();
            if (oldY > 0) {
                int offset = Math.max(dyUnconsumed, -oldY);
                mListParent.scrollBy(0, offset);
                if (type == ViewCompat.TYPE_NON_TOUCH && mListParent.getScrollY() == 0) {
                    if (target instanceof RecyclerView) {
                        ((RecyclerView) target).stopScroll();
                        Log.d(TAG, "handleScrollDown: stop Scroll-0");
                    }
                }
            } else if (type == ViewCompat.TYPE_TOUCH) {
                if (mStatus != STATUS_NULL && mStatus != STATUS_PULL) {
                    return;
                }
                float y = mRefreshLayout.getTranslationY();
                if (y < mMaxPullHeight) {
                    mStatus = STATUS_PULL;
                    float offset = 0.5f * dyUnconsumed;
                    offset = Math.min(-offset, mMaxPullHeight - y);
                    final float translationY = offset + y;
                    mListParent.setTranslationY(translationY);
                    mRefreshLayout.setTranslationY(translationY);
                    if (translationY >= mRefreshHeight) {
                        mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
                    } else {
                        mRefreshView.setStatus(IRefreshView.STATUS_PULL);
                    }
                }
            } else if (type == ViewCompat.TYPE_NON_TOUCH) {
                if (target instanceof RecyclerView) {
                    ((RecyclerView) target).stopScroll();
                    Log.d(TAG, "handleScrollDown: stop Scroll-1");
                }
            }
        }

    }

    private void onRefreshViewBreak() {
        if (mStatus == STATUS_PULL) {
            mStatus = STATUS_BREAK;
            mHeadRvClosed = false;
            mListParent.setTranslationY(0);
            mListParent.scrollBy(0, -mBreakHeight);
            mRefreshLayout.animate().translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mStatus = STATUS_NULL;
                        }
                    })
                    .start();
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, @ViewCompat.NestedScrollType int type) {
        if (dy > 0) {
            int headHeight = mHeadView.getHeight();
            if (headHeight > mMinHeadHeight) { // 缩小headView
                ViewGroup.LayoutParams layoutParams = mHeadView.getLayoutParams();
                int offset = Math.min(dy, headHeight - mMinHeadHeight);
                layoutParams.height = headHeight - offset;
                mHeadView.setLayoutParams(layoutParams);
                mHeadView.requestLayout();
                consumed[1] = offset;
            } else if (mStatus == STATUS_PULL) {
                float oldY = mRefreshLayout.getTranslationY();
                if (oldY > 0) {
                    float offset = Math.min(dy, oldY);
                    float translationY = oldY - offset;
                    mListParent.setTranslationY(translationY);
                    mRefreshLayout.setTranslationY(translationY);
                    float newY = mRefreshLayout.getTranslationY();
                    consumed[1] = (int) (oldY - newY);
                    if (newY == 0) {
                        mStatus = STATUS_NULL;
                    }
                    if (translationY >= mRefreshHeight) {
                        mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
                    } else if (translationY > 0) {
                        mRefreshView.setStatus(IRefreshView.STATUS_PULL);
                    } else {
                        mRefreshView.setStatus(IRefreshView.STATUS_NULL);
                    }
                } else {
                    mStatus = STATUS_NULL;
                }
            } else if (target == mRecyclerView) {
                handleScrollUp(dy, consumed, type);
            }
        }
        Log.d(TAG, "onNestedPreScroll: dy = " + dy);
        Log.d(TAG, "onNestedPreScroll: consumed = " + Arrays.toString(consumed));
    }

    private void handleScrollUp(int dy, int[] consumed, int type) {
        int oldY = mListParent.getScrollY();
        int height = mHeadRv.getHeight();
        if (oldY < height) {
            int offset = Math.min(dy, height - oldY);
            mListParent.scrollBy(0, offset);
            int newY = mListParent.getScrollY();
            if (consumed != null) {
                consumed[1] = newY - oldY;
            }
            if (newY == height) {
                mHeadRvClosed = true;
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "dispatchTouchEvent: is Touching" + true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d(TAG, "dispatchTouchEvent: is Touching" + false);
                    resetHeadView();
                    break;
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void resetHeadView() {
        float y = mRefreshLayout.getTranslationY();
        if (y != 0 && mStatus == STATUS_PULL) {
            if (y >= mRefreshHeight) {
                onRefreshing();
            } else {
                onResetRefresh();
            }
        }
    }

    private void onRefreshing() {
        mStatus = STATUS_REFRESH;
        mRefreshView.setStatus(IRefreshView.STATUS_REFRESHING);
        ValueAnimator animator1 = ObjectAnimator.ofFloat(mRefreshLayout, "translationY", mRefreshHeight);
        ValueAnimator animator2 = ObjectAnimator.ofFloat(mListParent, "translationY", mRefreshHeight);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onResetRefresh();
                    }
                }, 1500);
            }
        });
        animatorSet.playTogether(animator1, animator2);
        animatorSet.start();
    }


    private void onResetRefresh() {
        mStatus = STATUS_RESET;
        mRefreshView.setStatus(IRefreshView.STATUS_RESET);
        ValueAnimator animator1 = ObjectAnimator.ofFloat(mRefreshLayout, "translationY", 0);
        ValueAnimator animator2 = ObjectAnimator.ofFloat(mListParent, "translationY", 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mStatus = STATUS_NULL;
                mRefreshView.setStatus(IRefreshView.STATUS_NULL);
            }
        });
        animatorSet.playTogether(animator1, animator2);
        animatorSet.start();
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh(mHeadRvClosed);
        }
    }


    public void startRefresh() {
        if (mStatus == STATUS_NULL) {
            onRefreshing();
        }
    }

    public void stopRefresh() {
        if (mStatus == STATUS_REFRESH) {
            onResetRefresh();
        }
    }

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void resetAllView() {
        if (mStatus == STATUS_NULL) {
            onResetRefresh();


            mListParent.setScrollY(0);


        }
    }


    public interface OnRefreshListener {

        void onRefresh(boolean headRvClosed);

    }


}
