package com.hikvision.open.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hikvision.open.hikvideoplayer.HikVideoPlayer;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerCallback;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;

import java.text.MessageFormat;

/**
 * 错误码开头：17是mgc或媒体取流SDK的错误，18是vod，19是dac
 */
public class VoiceTalkActivity extends AppCompatActivity implements View.OnClickListener, HikVideoPlayerCallback.VoiceTalkCallback {
    private static final String TAG = "VoiceTalkActivity";
    protected ProgressBar progressBar;
    protected TextView playHintText;
    protected EditText reviewUriEdit;
    private String mUri = "";
    private HikVideoPlayer mPlayer;
    private PlayerStatus mPlayerStatus = PlayerStatus.IDLE;//默认闲置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//防止键盘弹出
        super.setContentView(R.layout.activity_voice_talk);
        initView();
        mPlayer = HikVideoPlayerFactory.provideHikVideoPlayer();
        reviewUriEdit.setText(mUri);
        requestPermissions();
    }

    private void initView() {
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        progressBar = findViewById(R.id.progress_bar);
        playHintText = findViewById(R.id.result_hint_text);
        reviewUriEdit = findViewById(R.id.review_uri_edit);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start) {
            if (mPlayerStatus != PlayerStatus.SUCCESS && getPreviewUri()) {
                startVoiceTalk();
            }
        } else if (view.getId() == R.id.stop) {
            if (mPlayerStatus == PlayerStatus.SUCCESS) {
                mPlayerStatus = PlayerStatus.IDLE;//释放这个窗口
                progressBar.setVisibility(View.GONE);
                playHintText.setVisibility(View.VISIBLE);
                playHintText.setText("对讲关闭了");
                mPlayer.stopVoiceTalk();
            }
        }
    }

    /**
     * 开始播放
     */
    private void startVoiceTalk() {
        mPlayerStatus = PlayerStatus.LOADING;
        progressBar.setVisibility(View.VISIBLE);
        playHintText.setVisibility(View.GONE);
        //TODO 注意: startVoiceTalk() 方法会阻塞当前线程，需要在子线程中执行,建议使用RxJava
        new Thread(() -> {
            if (mPlayer.startVoiceTalk(mUri, VoiceTalkActivity.this)) {
                onTalkStatus(HikVideoPlayerCallback.Status.SUCCESS, -1);
            } else {
                onTalkStatus(HikVideoPlayerCallback.Status.FAILED, mPlayer.getLastError());
            }
        }).start();
    }

    /**
     * 播放结果回调
     *
     * @param status    共四种状态：SUCCESS（开启成功）、FAILED（开启失败）、EXCEPTION（取流异常）
     * @param errorCode 错误码，只有 FAILED 和 EXCEPTION 才有值
     */
    @Override
    public void onTalkStatus(@NonNull HikVideoPlayerCallback.Status status, int errorCode) {
        //TODO 注意: 由于 VoiceTalkCallback 是在子线程中进行回调的，所以一定要切换到主线程处理UI
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            switch (status) {
                case SUCCESS:
                    //播放成功
                    mPlayerStatus = PlayerStatus.SUCCESS;
                    playHintText.setVisibility(View.VISIBLE);
                    playHintText.setText("正在对讲中。。。。");
                    break;
                case FAILED:
                    //播放失败
                    mPlayerStatus = PlayerStatus.FAILED;
                    playHintText.setVisibility(View.VISIBLE);
                    playHintText.setText(MessageFormat.format("开启对讲失败，错误码：{0}", Integer.toHexString(errorCode)));
                    break;
                case EXCEPTION:
                    //取流异常
                    mPlayerStatus = PlayerStatus.EXCEPTION;
                    mPlayer.stopVoiceTalk();//TODO 注意:异常时关闭对讲
                    playHintText.setVisibility(View.VISIBLE);
                    playHintText.setText(MessageFormat.format("对讲发生异常，错误码：{0}", Integer.toHexString(errorCode)));
                    break;
            }
        });
    }


    private boolean getPreviewUri() {
        mUri = reviewUriEdit.getText().toString();
        if (TextUtils.isEmpty(mUri)) {
            reviewUriEdit.setError("URI不能为空");
            return false;
        }

        if (!mUri.contains("rtsp")) {
            reviewUriEdit.setError("非法URI");
            return false;
        }

        return true;
    }


    /**
     * TODO：语音对讲功能需要手机麦克风权限
     */
    private void requestPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, (new String[]{Manifest.permission.RECORD_AUDIO}), 10);
        }
    }
}
