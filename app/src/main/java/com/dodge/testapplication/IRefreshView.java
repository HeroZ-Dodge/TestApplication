package com.dodge.testapplication;

/**
 * Created by linzheng on 2019/6/18.
 */

public interface IRefreshView {

    int STATUS_NULL = 0;
    int STATUS_PULL = 1;
    int STATUS_PRE_REFRESH = 2;
    int STATUS_REFRESHING = 3;
    int STATUS_RESET = 4;


    void setStatus(int status);

    void onPull();

    void onPreRefresh();

    void onRefreshing();


    void onReset();


}
