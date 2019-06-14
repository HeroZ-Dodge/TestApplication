package com.dodge.testapplication;

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

    private View mHeadView;
    private RecyclerView mRecyclerView0;
    private RecyclerView recyclerView;
    private TextView mTvStatus;


    private int mMaxHeadHeight;
    private int mMinHeadHeight;


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
        View view = inflater.inflate(R.layout.my_scroll_layout, this, false);
        mHeadView = view.findViewById(R.id.head_view);
        mRecyclerView0 = view.findViewById(R.id.recycler_view_0);
        recyclerView = view.findViewById(R.id.recycler_view_1);
        mTvStatus = view.findViewById(R.id.status_view);

        mMaxHeadHeight = ScreenUtil.dip2px(100);
        mMinHeadHeight = mMaxHeadHeight / 2;

        addView(view);
    }

    public RecyclerView getRecyclerView0() {
        return mRecyclerView0;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public TextView getmTvStatus() {
        return mTvStatus;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes, @ViewCompat.ScrollAxis int type) {
        Log.d(TAG, "onStartNestedScroll: axes = " + axes);
        if (axes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            return true;
        }
        return false;
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
        handleScrollDown(dyUnconsumed);
        Log.d(TAG, "onNestedPreScroll: dyConsumed = " + dyConsumed);
        Log.d(TAG, "onNestedPreScroll: dyUnconsumed = " + dyUnconsumed);

    }

    private void handleScrollDown(int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            int oldY = getScrollY();
            if (oldY > 0) {                       // 向上滚动
                int offset = Math.max(dyUnconsumed, -oldY);
                scrollBy(0, offset);
            }
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, @ViewCompat.NestedScrollType int type) {
        if (dy > 0) {
            int headHeight = mHeadView.getHeight();
            if (headHeight > mMinHeadHeight) {
                ViewGroup.LayoutParams layoutParams = mHeadView.getLayoutParams();
                int offset = Math.min(dy, headHeight - mMinHeadHeight);
                layoutParams.height = headHeight - offset;
                mHeadView.setLayoutParams(layoutParams);
                mHeadView.requestLayout();
                consumed[1] = offset;
            } else {
                handleScrollUp(dy, consumed);
            }
        }

        Log.d(TAG, "onNestedPreScroll: dy = " + dy);
        Log.d(TAG, "onNestedPreScroll: consumed = " + Arrays.toString(consumed));
    }

    private void handleScrollUp(int dy, @NonNull int[] consumed) {
        int oldY = getScrollY();
        int height = mRecyclerView0.getHeight();
        if (oldY < height) {
            int offset = Math.min(dy, height - oldY);
            scrollBy(0, offset);
            consumed[1] = getScrollY() - oldY;
        }
    }


}
