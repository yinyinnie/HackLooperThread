package io.github.yinyinnie.hackloopthread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler mUIHandler;
    private TextView mtvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // case 1: communicate with the UI thread directly.
        mtvMessage = findViewById(R.id.tvMessage);
        mUIHandler = new UIHandler(this);

        // case 2: create the loop thread
        final LooperThread looperThread = new LooperThread();
        looperThread.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    // send message to LooperThread
                    Message message0 = new Message();
                    message0.what = i++;
                    looperThread.mHandler.sendMessageDelayed(message0, 1000);

                    // send message to UI Thread
                    Message message1 = new Message();
                    message1.what = i;
                    mUIHandler.sendMessageDelayed(message1, 1000);
                }

            }
        }).start();

    }
    // --------------------------------------- hack ui thread
    static class UIHandler extends Handler {

        WeakReference<MainActivity> mWeakReference;

        public UIHandler(MainActivity mainActivity) {
            mWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference != null) {
                mWeakReference.get().mtvMessage.setText(msg != null ? String.valueOf(msg.what) : "null");
            }
        }
    }

    // ---------------------------------------  hack loop thread.

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "receive msg: " + (msg != null ? msg.what : 0));
        }
    }


    class LooperThread extends Thread {
        public Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();

            mHandler = new MyHandler();

            Looper.loop();
        }
    }
}
