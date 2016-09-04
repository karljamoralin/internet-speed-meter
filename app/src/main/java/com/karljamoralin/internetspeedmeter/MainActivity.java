package com.karljamoralin.internetspeedmeter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends Activity implements SpeedMeter.TaskRunnableSpeedMeterMethods{

    private Thread mSpeedMeterThread;
    private Handler mHandler;
    private TextView downloadSpeedOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadSpeedOutput = (TextView) findViewById(R.id.speed);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                SpeedMeter speedMeter = (SpeedMeter) inputMessage.obj;
                downloadSpeedOutput.setText(Long.toString(speedMeter.getmDownloadSpeedKB()));
            }
        };

        SpeedMeter speedMeter = new SpeedMeter(this);
        speedMeter.run();

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_thumb_up_black_24dp)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());

    }

    @Override
    public void setSpeedMeterThread(Thread currentThread) {
        mSpeedMeterThread = currentThread;
    }

    @Override
    public void setInternetSpeed(SpeedMeter speedMeter) {
//        downloadSpeedOutput.setText(Long.toString(downloadSpeed));
        Message completeMessage = mHandler.obtainMessage(1, speedMeter);
        completeMessage.sendToTarget();
    }
}
