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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends Activity implements SpeedMeter.SpeedMeterListener {

    private Thread mSpeedMeterThread;
    private Handler mHandler;
    private TextView downloadSpeedOutput;
    private Notification.Builder mBuilder;
    private Notification mNotification;
    private int mNotificationId = 001;
    private Intent mResultIntent;
    private NotificationManager mNotifyMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultIntent = new Intent(this, MainActivity.class);

        new Thread(new SpeedMeter(this)).start();

        initializeNotification();

    }

    private void initializeNotification() {

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {

                //instantiate a SpeedMeter class using the current running SpeedMeter thread
                SpeedMeter speedMeter = (SpeedMeter) inputMessage.obj;

                Bitmap bitmap = createBitmapFromString(speedMeter.getmDownloadSpeedOutput(),
                        speedMeter.getmUnits());

                Icon icon = Icon.createWithBitmap(bitmap);

                mBuilder.setSmallIcon(icon);

                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            }

        };

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_thumb_up_black_24dp)
                .setContentTitle("");

        mNotification = mBuilder.build();

        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        /*The stack builder object will contain an artificial back stack for the started Activity.
          This ensures that navigating backward from the Activity leads out of your application to
          the Home screen.*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(mResultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on. mId = 1
        mNotificationManager.notify(1, mBuilder.build());
    }

    public Bitmap createBitmapFromString(String speed, String units) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(50); // size is in pixels
        paint.setTextAlign(Paint.Align.CENTER);

        Paint unitsPaint = new Paint();
        unitsPaint.setAntiAlias(true);
        unitsPaint.setTextSize(40); // size is in pixels
        unitsPaint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(speed, 0, speed.length(), textBounds);

        Rect unitsTextBounds = new Rect();
        unitsPaint.getTextBounds(units, 0, units.length(), unitsTextBounds);

        int width = (textBounds.width() > unitsTextBounds.width()) ? textBounds.width() : unitsTextBounds.width();

        Bitmap bitmap = Bitmap.createBitmap(width + 10, 90,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(speed, width/2 + 5, 50, paint);
        canvas.drawText(units, width/2, 90, unitsPaint);

        return bitmap;
    }

    @Override
    public void speedMeterThreadCreated(Thread currentThread) {
        mSpeedMeterThread = currentThread;
    }

    @Override
    public void downloadSpeedUpdated(SpeedMeter speedMeter) {

        Message completeMessage = mHandler.obtainMessage(1, speedMeter);
        completeMessage.sendToTarget();


    }

}
