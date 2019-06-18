package com.dodge.testapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by linzheng on 2019/6/18.
 */

public class RefreshHeadView extends FrameLayout implements IRefreshView {


    private int mStatus = STATUS_NULL;
    private TextView mTvStatus;


    public RefreshHeadView(@NonNull Context context) {
        super(context);
        initView(context);
    }


    public RefreshHeadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshHeadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_head_view, this, true);
        mTvStatus = view.findViewById(R.id.tv_status);
//        addView(view);
    }


    @Override
    public void setStatus(int status) {
        if (mStatus == status) {
            return;
        }
        mStatus = status;
        switch (status) {
            case STATUS_NULL:
                onReset();
                break;
            case STATUS_PULL:
                onPull();
                break;
            case STATUS_PRE_REFRESH:
                onPreRefresh();
                break;
            case STATUS_REFRESHING:
                onRefreshing();
                break;
            case STATUS_RESET:
                onReset();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPull() {

        mTvStatus.setText("下拉刷新");
    }

    @Override
    public void onPreRefresh() {

        mTvStatus.setText("释放刷新");
    }

    @Override
    public void onRefreshing() {
        mTvStatus.setText("正在刷新");
    }

    @Override
    public void onReset() {
        mTvStatus.setText("");
    }
}
