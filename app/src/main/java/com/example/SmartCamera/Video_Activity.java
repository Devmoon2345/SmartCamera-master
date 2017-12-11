package com.example.SmartCamera;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Video_Activity extends AppCompatActivity {
    private SurfaceView sv_video;
    private ImageButton ib_play;
    private SeekBar sb_playbar;
    private TextView tv_playtime;
    private MediaPlayer player;
    private ProgressDialog dialog;
    private String toteTime;
    private int position;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void dispatchMessage(Message msg) {
            tv_playtime.setText(msg.obj.toString());
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);

        sv_video = findViewById(R.id.sv_video);
        ib_play = findViewById(R.id.ib_play);
        sb_playbar = findViewById(R.id.sb_playbar);
        tv_playtime = findViewById(R.id.tv_playtime);

        sb_playbar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        sv_video.getHolder().addCallback(new MyCallback());

        sv_video.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        listenerPhoneState();
    }

    public void listenerPhoneState() {
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);// 获取电话管理器
        assert manager != null;
        manager.listen(new PhoneStateListener() {
            boolean isPlay = false;// 默认为没有播放视频

            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:// 空闲状态
                        if (player != null && isPlay) {
                            player.start();
                            isPlay = false;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:// 来电状态
                        if (player != null && player.isPlaying()) {
                            player.pause();
                            isPlay = true;
                        }
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);// 监听手机电话状态
    }


    public void onClick(View view) throws Exception {
        if (player == null) {

            playVideo();
        } else {
            if (player.isPlaying()) {

                player.pause();
            } else {

                player.start();
            }

            ib_play.setBackgroundResource(player.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        }
    }


    private void playVideo() throws Exception {
        String path = "http://980.so/2RTL7";
        player = new MediaPlayer(); // 创建媒体播放对象
        player.setDataSource(path);// 设置视频源地址
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置播放的视频类型
        player.setDisplay(sv_video.getHolder());// 设置视频显示的位置
        player.prepareAsync();// 异步加载视频
        player.setOnPreparedListener(new MyPreparedListener());// 注册一个回调函数，当视频加载完成时调用
        showDialog();
    }

    /*
     * 显示对话框
     */
    private void showDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("loading");
        dialog.show();
    }


    public void releaseVideo() {
        if (player != null) {
            player.stop();// 停止播放的视频
            player.release();// 释放视频资源
            player = null;// 把播放的视频置为空
        }
    }

    /*
     * 当退出当前Activity时，释放视频资源
     */
    @Override
    protected void onDestroy() {
        releaseVideo();
        super.onDestroy();
    }

    /*
     * 控制进度条的显示
     */
    private void handleSeekBar() {
        toteTime = updateTime(player.getDuration());
        sb_playbar.setMax(player.getDuration()); // 设置进度条的长度为视频的总长度
        new Thread() { // 启动一个新线程用于更新进度条
            public void run() {
                while (player != null) { // 如果有视频播放才更新进度条
                    // 如果视频正在播放而且进度条没有被拖动
                    if (!sb_playbar.isPressed() && player.isPlaying()) {
                        // 设置进度条的当前进度为视频已经播放的长度
                        sb_playbar.setProgress(player.getCurrentPosition());
                        Message msg = new Message();
                        msg.obj = updateTime(player.getCurrentPosition()) + "/" + toteTime;
                        handler.sendMessage(msg);
                    }
                    try {
                        Thread.sleep(500);// 休眠500毫秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /*
     * 获取视频当前播放时间格式化后的字符串
     */
    public String updateTime(int t) {
        int s = t / 1000 % 60;
        int m = t / 1000 / 60;
        return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
    }

    /*
     * 对视频在后台时做处理
     */
    private class MyCallback implements SurfaceHolder.Callback {

        // 切换回界面时
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (position != 0) {
                try {
                    playVideo();// 继续之前的位置播放
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        // 切换到后台时调用
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (player != null) {
                position = player.getCurrentPosition();// 记录当前播放到的位置
                releaseVideo();// 释放资源
            }
        }

    }

    /*
     * 监听视频加载状态
     */
    private class MyPreparedListener implements OnPreparedListener {

        // 当视频加载完成时调用
        @Override
        public void onPrepared(MediaPlayer mp) {
            dialog.dismiss();
            player.start();// 播放视频
            player.seekTo(position);// 从指定位置开始播放
            ib_play.setBackgroundResource(android.R.drawable.ic_media_pause);// 改变按钮的背景图片
            player.setOnCompletionListener(new MyCompletionListener()); // 添加一个媒体播放监听器，监听视频是否播放结束
            handleSeekBar();// 控制进度条的显示
        }

    }

    /*
     * 监听拖动条变化
     */
    private class MySeekBarChangeListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        // 当拖动条拖动完成后调用
        @SuppressLint("SetTextI18n")
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (player != null) {
                player.seekTo(seekBar.getProgress());// 设置拖动条当前进度
                tv_playtime.setText(updateTime(player.getCurrentPosition()) + "/" + toteTime);// 更新视频播放到的时间
            }
        }

    }

    /*
     * 监听视频播放状态
     */
    private class MyCompletionListener implements OnCompletionListener {

        // 当媒体播放结束后调用
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseVideo();// 释放视频资源
            ib_play.setBackgroundResource(android.R.drawable.ic_media_play);// 改变按钮的背景图片
            sb_playbar.setProgress(0);// 设置当前进度条的进度为零
            tv_playtime.setText(R.string.playtime);// 设置视频播放到的时间为00:00/00:00
        }

    }
}
