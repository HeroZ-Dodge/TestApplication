package com.dodge.testapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by linzheng on 2019/6/18.
 */

public class RefreshHeadView extends FrameLayout {





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
        View view = inflater.inflate(R.layout.layout_head_view, this, false);
        addView(view);
    }


    public void pullScroll(int y) {




    }





}
