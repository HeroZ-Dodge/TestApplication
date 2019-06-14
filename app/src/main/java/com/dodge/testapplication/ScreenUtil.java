package com.dodge.testapplication;

import android.content.res.Resources;

/**
 * Created by linzheng on 2019/6/14.
 */

public class ScreenUtil {


    public static int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


}
