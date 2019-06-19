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
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * Created by linzheng on 2019/6/13.
 */

public class MyScrollLayout extends FrameLayout implements NestedScrollingParent2 {

    public static final String TAG = "MyScrollLayout";

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_PULL = 1;
    public static final int STATUS_RESET = 2;
    public static final int STATUS_REFRESH = 3;
    public static final int STATUS_BREAK = 4;

    private int mStatus = STATUS_DEFAULT;
    private boolean mHeadRvClosed = false;


    private View mRootView;
    private ViewGroup mHeadLayout;
    private View mHeadView;

    private ViewGroup mListParent;
    private RecyclerView mHeadRv;
    private RecyclerView mRecyclerView;

    private ViewGroup mRefreshLayout;
    private IRefreshView mRefreshView;


    private int mParentHeight;   // 父控件 高度
    private int mMaxHeadHeight;  // 头部控件最大高度
    private int mMinHeadHeight;  // 头部控件最小高度

    private int mMaxPullHeight;  // 最大下拉高度
    private int mRefreshHeight;  // 刷新触发高度
    private int mRipHeight;      // 撕裂高度
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
        mRootView = inflater.inflate(R.layout.my_scroll_layout, this, true);
        mListParent = mRootView.findViewById(R.id.recycler_layout);
        mHeadLayout = mRootView.findViewById(R.id.head_layout);
        mHeadView = mRootView.findViewById(R.id.head_view);

        mHeadRv = mRootView.findViewById(R.id.recycler_view_head);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view_content);

        mRefreshLayout = mRootView.findViewById(R.id.refresh_layout);
        mRefreshView = mRootView.findViewById(R.id.refresh_head);

        mMaxHeadHeight = ScreenUtil.dip2px(100);
        mMinHeadHeight = mMaxHeadHeight / 2;

        mMaxPullHeight = ScreenUtil.dip2px(120);
        mRefreshHeight = ScreenUtil.dip2px(64);
        mRipHeight = ScreenUtil.dip2px(96);
    }

    public RecyclerView getRecyclerView0() {
        return mHeadRv;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public ViewGroup getHeadLayout() {
        return mHeadLayout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return;
        }
        mParentHeight = getMeasuredHeight();
        if (mParentHeight > 0 && mRecyclerView != null) {
            mRecyclerView.getLayoutParams().height = mParentHeight - mMinHeadHeight;
            if (getChildCount() > 0) {
                final View child = getChildAt(0);
                $measureChildWithMargins(child, widthMeasureSpec);
            }
        }
    }

    private void $measureChildWithMargins(View child, int parentWidthMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes, @ViewCompat.ScrollAxis int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes, @ViewCompat.ScrollAxis int type) {
        mNsParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, @ViewCompat.ScrollAxis int type) {
        mNsParentHelper.onStopNestedScroll(target, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        if (dyUnconsumed > 0) {
            hideTopRecyclerView(dyUnconsumed, null);
        } else if (dyUnconsumed < 0) {
            handleScrollDown(target, dyUnconsumed, type);
        }
    }

    private void handleScrollDown(View target, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        if (mHeadRvClosed) {
            scrollDownWhenTopHide(target, dyUnconsumed, type);
        } else {
            scrollDownWhenTopExpand(target, dyUnconsumed, type);
        }

    }

    /**
     * 向下滚动，当头部区域可见状态时，优先将头部列表显示出来
     *
     * @param target       targetView
     * @param dyUnconsumed y轴距离 dyUnconsumed < 0
     * @param type         滑动类型
     */
    private void scrollDownWhenTopExpand(View target, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        int scrollY = mListParent.getScrollY();
        if (scrollY > 0) {
            int offset = Math.max(dyUnconsumed, -scrollY);
            mListParent.scrollBy(0, offset);
            if (type == ViewCompat.TYPE_NON_TOUCH && mListParent.getScrollY() == 0) {
                tryToStopScroll(target);
            }
            return;
        }

        switch (type) {
            case ViewCompat.TYPE_TOUCH:         // 触摸滑动
                if (mStatus != STATUS_DEFAULT && mStatus != STATUS_PULL) { // 在不是默认状态 或 下拉状态时，直接跳过
                    return;
                }
                float oldY = mRefreshLayout.getTranslationY();
                if (oldY >= mMaxPullHeight) { // 下拉到了最大距离
                    return;
                }
                mStatus = STATUS_PULL;
                final float offset = Math.min(-0.5f * dyUnconsumed, mMaxPullHeight - oldY); // 下拉阻尼 = 0.5
                final float translationY = offset + oldY;
                mListParent.setTranslationY(translationY);
                mRefreshLayout.setTranslationY(translationY);
                // 根据偏移量，更新状态
                if (translationY >= mRefreshHeight) {
                    mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
                } else {
                    mRefreshView.setStatus(IRefreshView.STATUS_PULL);
                }
                break;

            case ViewCompat.TYPE_NON_TOUCH:     // 惯性滑动
                tryToStopScroll(target);
                break;

            default:
                break;
        }
    }

    /**
     * 向下滚动，当头部区域隐藏状态时，需要撕裂过程过渡到可见状态
     *
     * @param target       targetView
     * @param dyUnconsumed y轴距离 dyUnconsumed < 0
     * @param type         滑动类型
     */
    private void scrollDownWhenTopHide(View target, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        switch (type) {
            case ViewCompat.TYPE_TOUCH:     // 触摸滑动
                if (mStatus != STATUS_DEFAULT && mStatus != STATUS_PULL) { // 在不是默认状态 或 下拉状态时，直接跳过
                    return;
                }
                float oldY = mRefreshLayout.getTranslationY();
                if (oldY >= mMaxPullHeight) { // 下拉到了最大距离
                    return;
                }
                mStatus = STATUS_PULL;
                final float offset = Math.min(-0.5f * dyUnconsumed, mMaxPullHeight - oldY); // 下拉阻尼 = 0.5
                final float translationY = offset + oldY;
                mListParent.setTranslationY(translationY);
                mRefreshLayout.setTranslationY(translationY);
                // 根据偏移量，更新状态
                if (translationY >= mRipHeight) {                           // 撕裂状态
                    onRipRefreshView();
                } else if (translationY >= mRefreshHeight) {                // 可刷新状态
                    mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
                } else {                                                    // 默认状态
                    mRefreshView.setStatus(IRefreshView.STATUS_PULL);
                }
                break;

            case ViewCompat.TYPE_NON_TOUCH:     // 惯性滑动
                tryToStopScroll(target);
                break;

            default:
                break;
        }
    }

    /**
     * 处理撕裂状态
     */
    private void onRipRefreshView() {
        if (mStatus != STATUS_PULL) {
            return;
        }
        mStatus = STATUS_BREAK;
        mHeadRvClosed = false;
        mListParent.setTranslationY(0);
        mListParent.scrollBy(0, -mRipHeight);
        ValueAnimator animator = ObjectAnimator.ofFloat(mRefreshLayout, "translationY", 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mStatus = STATUS_DEFAULT;
            }
        });
        animator.start();
    }

    /**
     * 尝试停止滚动
     *
     * @param target targetView
     */
    private void tryToStopScroll(View target) {
        if (target instanceof RecyclerView) {
            ((RecyclerView) target).stopScroll();
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, @ViewCompat.NestedScrollType int type) {
        if (dy <= 0) {
            // 向下滚动的行为，交给子View处理
            return;
        }
        // 处理向上滚动
        int headHeight = mHeadView.getHeight();
        if (headHeight > mMinHeadHeight) {      // 优先缩小置顶View
            int offset = Math.min(dy, headHeight - mMinHeadHeight);
            ViewGroup.LayoutParams layoutParams = mHeadView.getLayoutParams();
            layoutParams.height = headHeight - offset;
            mHeadView.setLayoutParams(layoutParams);
            consumed[1] = offset;
        } else if (mStatus == STATUS_PULL) {    // 当前正在下拉刷新状态
            float oldY = mRefreshLayout.getTranslationY();
            if (oldY <= 0) {
                mStatus = STATUS_DEFAULT;
                return;
            }
            float offset = Math.min(dy, oldY);
            float translationY = oldY - offset;
            mListParent.setTranslationY(translationY);
            mRefreshLayout.setTranslationY(translationY);
            consumed[1] = (int) offset;
            // 根据偏移量，更新状态
            if (translationY >= mRefreshHeight) {
                mRefreshView.setStatus(IRefreshView.STATUS_PRE_REFRESH);
            } else if (translationY > 0) {
                mRefreshView.setStatus(IRefreshView.STATUS_PULL);
            } else {
                mRefreshView.setStatus(IRefreshView.STATUS_NULL);
                mStatus = STATUS_DEFAULT;
            }
        } else if (target == mRecyclerView) {
            hideTopRecyclerView(dy, consumed);
        }
    }

    private void hideTopRecyclerView(int dy, int[] consumed) {
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
                mStatus = STATUS_DEFAULT;
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
        if (mStatus == STATUS_DEFAULT) {
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
        if (mStatus != STATUS_DEFAULT) {
            return;
        }
        int height = mHeadView.getHeight();
        if (height == mMaxHeadHeight) { // 当前已处于初始状态
            return;
        }
        tryToStopScroll(mRecyclerView);
        tryToStopScroll(mHeadRv);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.scrollToPosition(0);
        mListParent.setScrollY(0);
        final ViewGroup.LayoutParams layoutParams = mHeadView.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(height, mMaxHeadHeight);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.height = (int) animation.getAnimatedValue();
                mHeadView.setLayoutParams(layoutParams);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mStatus = STATUS_DEFAULT;
            }
        });
        animator.start();
    }


    public interface OnRefreshListener {

        void onRefresh(boolean headRvClosed);

    }


}
