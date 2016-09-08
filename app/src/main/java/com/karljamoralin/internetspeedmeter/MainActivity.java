package com.karljamoralin.internetspeedmeter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

public class MainActivity extends Activity implements SpeedMeter.TaskRunnableSpeedMeterMethods{

    private Thread mSpeedMeterThread;
    private Handler mHandler;
    private TextView downloadSpeedOutput;
    private Notification.Builder mBuilder;
    private Notification notification;
    private int mNotificationId = 001;
    NotificationManager mNotifyMgr;


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
                String downloadSpeed = Long.toString(speedMeter.getmDownloadSpeedKB());
                Bitmap bitmap = createBitmapFromString(downloadSpeed);
                Icon icon = Icon.createWithBitmap(bitmap);
                mBuilder.setSmallIcon(icon);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        };

//        SpeedMeter speedMeter = new SpeedMeter(this);
//        speedMeter.run();

        new Thread(new SpeedMeter(this)).start();

        ImageView image = (ImageView) findViewById(R.id.image);
        image.setBackgroundResource(R.drawable.animation_test);
//        AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
//        animationDrawable.start();

        Bitmap bitmap = createBitmapFromString("test");

        Icon icon = Icon.createWithBitmap(bitmap);

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle("My notification")
                .setContentText("Hello World!");

        notification = mBuilder.build();

        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

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

    public Bitmap createBitmapFromString(String string) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(50); // size is in pixels

        Rect textBounds = new Rect();
        paint.getTextBounds(string, 0, string.length(), textBounds);

        Bitmap bitmap = Bitmap.createBitmap(textBounds.width(), textBounds.height(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(string, -textBounds.left,
                textBounds.height() - textBounds.bottom, paint);

        return bitmap;
    }

}
