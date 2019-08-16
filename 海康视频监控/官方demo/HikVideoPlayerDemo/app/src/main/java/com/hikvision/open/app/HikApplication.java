package com.hikvision.open.app;

import android.app.Application;
import android.graphics.Color;
import android.view.Gravity;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;


public class HikApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO: enableLog：在debug模式下打开日志，release关闭日志
        //TODO: 现阶段 appKey 不需要，直接传 null
        HikVideoPlayerFactory.initLib(null, true);


        Utils.init(this);
        ToastUtils.setBgColor(Color.parseColor("#99000000"));
        ToastUtils.setMsgColor(Color.parseColor("#FFFFFFFF"));
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
    }
}
