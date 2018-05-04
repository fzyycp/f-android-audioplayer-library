package cn.faury.library.demo.audio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.faury.android.library.audioplayer.AudioActivity;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getName();
    private static ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.play_audio_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioActivity.class);
                intent.putExtra(AudioActivity.EXTS_TITLE, "标题");
                intent.putExtra(AudioActivity.EXTS_TEXT, "字幕");
                intent.putExtra(AudioActivity.EXTS_URL, "http://dev.wassx.cn:8888/Myftp/jlsj/file/ssqr/fckUplodFiles/2018/04/02/C201804021621067283270/2018040216215814144.mp3");
                intent.putExtra(AudioActivity.EXTS_COVER_IMG, "http://e.hiphotos.baidu.com/image/pic/item/aa18972bd40735fa324a79d792510fb30f240821.jpg");
                intent.putExtra(AudioActivity.EXTS_ZM_IMG, "http://h.hiphotos.baidu.com/image/pic/item/21a4462309f790525fe7185100f3d7ca7acbd5e1.jpg");
                startActivity(intent);
            }
        });

        findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                es.execute(new Task(1));
                Log.e(TAG, "Exec Task 1");
                es.execute(new Task(2));
                Log.e(TAG, "Exec Task 2");
                es.execute(new Task(3));
                Log.e(TAG, "Exec Task 3");
                es.execute(new Task(4));
                Log.e(TAG, "Exec Task 4");
                es.execute(new Task(5));
                Log.e(TAG, "Exec Task 5");
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.INTERNET"
        }, 10000);
    }

    class Task implements Runnable {

        private final String TAG = Task.class.getName();

        private int idx;

        public Task(int i) {
            this.idx = i;
        }

        @Override
        public void run() {
            Log.e(TAG, "run: task start " + idx);
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "run: task stop " + idx);
        }
    }
}
