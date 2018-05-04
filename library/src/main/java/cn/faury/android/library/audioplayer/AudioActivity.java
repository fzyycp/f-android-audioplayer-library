package cn.faury.android.library.audioplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

import cn.faury.android.library.image.loader.FImageLoader;

public class AudioActivity extends Activity implements OnClickListener,
        OnSeekBarChangeListener, OnDrawerCloseListener, OnDrawerOpenListener,
        OnPreparedListener, OnCompletionListener, Runnable, OnErrorListener {

    private String TAG = AudioActivity.class.getName();

    // 标题
    public static final String EXTS_TITLE = "title";
    public static final String EXTS_URL = "url";
    public static final String EXTS_COVER_IMG = "coverImg";
    public static final String EXTS_TEXT = "text";
    public static final String EXTS_ZM_IMG = "zmImg";

    private ImageView ivBack;

    private ImageView ivClose;

    private CircleImageView civLightDisk;

    private ImageView ivStop;

    private ImageView ivPlay;

    private ImageView ivLoop;

    private SeekBar sbMusic;

    private TextView tvCurrent;

    private TextView tvTotal;

    private TextView tvTitle;

    private TextView tvSubtitle; // 字幕

    private ImageView ivSubImage;

//	private TextView tvMusicName;

    private ImageView ivSlidLeft;

    private SlidingDrawer sDrawer;

    private AudioInfo audioInfo;

    private MediaPlayer mpMusic;

    private Handler handler = new Handler();

    private boolean error = false;

    // A-B循环的状态
    private int IDLE = 0;

    private int LOOPING = 1;

    private int WAIT_LOOPING = 2;

    private int status = 0;

    private int current_time;

    private boolean isShouldPlay = false;

    private boolean isPareComplete = false;// 是否缓存完成

    private String audioCoverImg;

    private String audioUrl;

    private String audioText;

    private String audioTitle;

    private String audioZmImg;

    public static DisplayImageOptions options;

    public static ImageLoader imageLoader;

    class OnePhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (mpMusic != null && mpMusic.isPlaying() && isPareComplete) {
                        pauseMusic();
                        isShouldPlay = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mpMusic != null && !mpMusic.isPlaying() && isShouldPlay
                            && isPareComplete) {
                        startMusic();
                        isShouldPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (mpMusic != null && mpMusic.isPlaying() && isPareComplete) {
                        pauseMusic();
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f_library_audio_activity_audio);
        setImageLoad();
        init();
        setListener();
        initValue();
        initMusic();
    }

    private void init() {
        ivBack = findViewById(R.id.iv_roll_top_left);
        ivClose = findViewById(R.id.iv_roll_top_right);
        civLightDisk = findViewById(R.id.civ_audio_circel_play);
        ivStop = findViewById(R.id.iv_audio_stop);
        ivPlay = findViewById(R.id.iv_audio_play);
        ivLoop = findViewById(R.id.iv_audio_loop);
        sbMusic = findViewById(R.id.sb_audio_progress);
        tvCurrent = findViewById(R.id.tv_audio_current_time);
        tvTotal = findViewById(R.id.tv_audio_end_time);
        tvTitle = findViewById(R.id.tv_roll_top_title);
        ivSubImage = findViewById(R.id.iv_audio_sub_image);
        tvSubtitle = findViewById(R.id.tv_audio_subtitle);
        ivSlidLeft = findViewById(R.id.iv_audio_slid_handle);
        sDrawer = findViewById(R.id.sd_audio_slidingdrawer);
    }

    private void setListener() {
        ivPlay.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivStop.setOnClickListener(this);
        ivLoop.setOnClickListener(this);
        sbMusic.setOnSeekBarChangeListener(this);
        sDrawer.setOnDrawerOpenListener(this);
        sDrawer.setOnDrawerCloseListener(this);
    }

    private void initValue() {

        ivClose.setVisibility(View.INVISIBLE);
        tvTitle.setText(R.string.f_library_audio_activity_audio_title);
        mpMusic = new MediaPlayer();

        audioTitle = getIntent().getStringExtra(EXTS_TITLE);
        audioCoverImg = getIntent().getStringExtra(EXTS_COVER_IMG);
        audioUrl = getIntent().getStringExtra(EXTS_URL);
        audioText = getIntent().getStringExtra(EXTS_TEXT);
        audioZmImg = getIntent().getStringExtra(EXTS_ZM_IMG);

        Log.v(TAG, "audioUrl: " + audioUrl);

        if (audioTitle != null && audioTitle.trim().length() > 0) {
            tvTitle.setText(audioTitle.trim());
        }
        if (audioText != null && audioText.trim().length() > 0) {
            tvSubtitle.setText(Html.fromHtml(audioText.trim()));
        }
        ivLoop.setImageResource(R.drawable.f_library_audio_btn_music_loop);
        if (audioZmImg != null && (!audioZmImg.equals(""))) {
            int screen_width = (int) (((WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()
                    - getResources().getDimension(
                    R.dimen.f_library_audio_activity_audio_arrow_width) - getResources()
                    .getDimension(R.dimen.f_library_audio_activity_audio_subimage_margin));
            int subImage_height = screen_width * 50 / 50;
            LinearLayout.LayoutParams lpSubPic = (LinearLayout.LayoutParams) ivSubImage
                    .getLayoutParams();
            lpSubPic.height = subImage_height;
            imageLoader.displayImage(audioZmImg,
                    ivSubImage);
        }

        if (audioCoverImg != null && !audioCoverImg.equals("")) {
            imageLoader.displayImage(audioCoverImg,
                    civLightDisk);
        }
        handler.post(this);

    }

    public void setImageLoad() {
        imageLoader = FImageLoader.createInstance(this);
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    private void initMusic() {

        try {
            mpMusic.reset();
            mpMusic.setDataSource(audioUrl);
            mpMusic.prepareAsync();
            mpMusic.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mpMusic.setOnPreparedListener(this);
            mpMusic.setOnCompletionListener(this);
            mpMusic.setOnErrorListener(this);
        } catch (IOException e) {
            Toast.makeText(this, "文件读取失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_roll_top_left) {
            finish();

        } else if (i == R.id.iv_audio_stop) {
            if (!isPareComplete)
                return;
            status = IDLE;
            ivLoop.setImageResource(R.drawable.f_library_audio_btn_music_loop);
            stopMusic();

        } else if (i == R.id.iv_audio_loop) {
            if (!isPareComplete)
                return;
            if ((!error) && mpMusic.isPlaying()) {
                if (status == IDLE) {
                    status = WAIT_LOOPING;
                    current_time = mpMusic.getCurrentPosition();
                    ivLoop.setImageResource(R.drawable.f_library_audio_btn_music_loop_sel);
                } else if (status == WAIT_LOOPING) {
                    status = LOOPING;
                    ivLoop.setImageResource(R.drawable.f_library_audio_btn_cycle_sel);
                    repeatAToB(current_time, mpMusic.getCurrentPosition());
                } else if (status == LOOPING) {
                    mpMusic.start();
                    status = IDLE;
                    ivLoop.setImageResource(R.drawable.f_library_audio_btn_music_loop);
                }
            }

        } else if (i == R.id.iv_audio_play) {
            if (!isPareComplete)
                return;
            status = IDLE;
            ivLoop.setImageResource(R.drawable.f_library_audio_btn_music_loop);
            if ((!error) && mpMusic.isPlaying()) {
                pauseMusic();
            } else {
                startMusic();
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        if (mpMusic != null && mpMusic.isPlaying()) {
            mpMusic.stop();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mpMusic != null) {
            stopMusic();
            mpMusic.release();
            mpMusic = null;
            handler.removeCallbacks(this);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
//		pauseMusic();
    }

    private void startMusic() {

        if (!error) {
            mpMusic.start();
            ivPlay.setImageResource(R.drawable.f_library_audio_btn_music_pause_selector);
            RotateAnimation animation = LocalAnimationUtils
                    .getAudioDiskAnimation(this);
            LinearInterpolator lir = new LinearInterpolator();
            lir.getInterpolation((float) 0.5);
            animation.setInterpolator(lir);

            civLightDisk.startAnimation(animation);
        }

    }

    private void stopMusic() {

        if (!error) {
            pauseMusic();
            mpMusic.seekTo(0);
        }
    }

    private void pauseMusic() {

        if (!error) {
            mpMusic.pause();
            ivPlay.setImageResource(R.drawable.f_library_audio_btn_play_music_selector);
            civLightDisk.clearAnimation();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        // TODO Auto-generated method stub
        if (fromUser && (!error) && isPareComplete) {
            mpMusic.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerOpened() {

        // TODO Auto-generated method stub
        ivSlidLeft.setBackgroundResource(R.drawable.f_library_audio_btn_close_text);
        findViewById(R.id.sv_audio_subtitle).scrollTo(0, 0);

    }

    @Override
    public void onDrawerClosed() {
        ivSlidLeft.setBackgroundResource(R.drawable.f_library_audio_btn_open);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // TODO Auto-generated method stub
        if (!error) {
            isPareComplete = true;
            startMusic();
            tvTotal.setText(getMusicTime());
            sbMusic.setMax(mp.getDuration());

            // 监听电话事件
            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(new OnePhoneStateListener(),
                    PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        // TODO Auto-generated method stub
        stopMusic();
    }

    /**
     * 获取字符串的音乐时长
     *
     * @return
     */
    private String getMusicTime() {

        int musicTime = mpMusic.getDuration() / 1000;

        return musicTime / 60 + ":" + musicTime % 60;

    }

    @Override
    public void run() {

        // TODO Auto-generated method stub
        int current = mpMusic.getCurrentPosition();
        sbMusic.setProgress(current);
        // 每次延迟100毫秒再启动线程
        String now_text = current / 1000 / 60 + ":" + current / 1000 % 60;
        if (isPareComplete) {
            tvCurrent.setText(now_text);
        } else {
            tvCurrent.setText(R.string.f_library_audio_loading);
        }
        handler.postDelayed(this, 1000);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        // TODO Auto-generated method stub
        Toast.makeText(this, R.string.f_library_audio_connect_internel_fail,
                Toast.LENGTH_SHORT).show();
        mp.stop();
        mpMusic.release();
        mpMusic = null;
        error = true;
        handler.removeCallbacks(this);
        return error;
    }

    private void repeatAToB(final int startPos, final int endPos) {

        if (status == LOOPING) {
            mpMusic.seekTo(startPos);
            // mpMusic.start();
            CountDownTimer cntr_aCounter = new CountDownTimer(
                    /* millisInFuture= */endPos - startPos, /*
             * countDownInterval=
             */1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    // DO SOMETHING
                }

                @Override
                public void onFinish() {

                    // Code fire after finish
                    if (status == LOOPING) {
                        try {
                            ivLoop.setImageResource(R.drawable.f_library_audio_btn_cycle_sel);
                            repeatAToB(startPos, endPos);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        mpMusic.seekTo(endPos);
                    }
                }
            };
            cntr_aCounter.start();
        } else {
            mpMusic.seekTo(endPos);
        }

    }
}
