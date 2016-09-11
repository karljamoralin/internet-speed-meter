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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SpeedMeter.SpeedMeterListener {

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

                //instantiate a SpeedMeter class using the current running SpeedMeter thread
                SpeedMeter speedMeter = (SpeedMeter) inputMessage.obj;

                downloadSpeedOutput.setText(speedMeter.getmDownloadSpeedOutput());

                Bitmap bitmap = createBitmapFromString(speedMeter.getmDownloadSpeedOutput(),
                        speedMeter.getUnits());

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

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_thumb_up_black_24dp)
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

    public Bitmap createBitmapFromString(String speed, String units) {

        TextPaint speedPaint = new TextPaint();
        speedPaint.setAntiAlias(true); //anti-aliasing
        speedPaint.setTextSize(50); // size is in pixels

        //create rect to bind both items
        Rect textBounds = new Rect();

        if (speed.length() > units.length()) {
            speedPaint.getTextBounds(speed, 0, speed.length(), textBounds);
        }
        else {
            speedPaint.getTextBounds(units, 0, units.length(), textBounds);
        }

        Bitmap bitmap = Bitmap.createBitmap(textBounds.width() + 10, textBounds.height() * 2,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

//        canvas.drawText(speed, -textBounds.left,
//                textBounds.height() - textBounds.bottom, speedPaint);


        //Try
        StaticLayout staticLayout = new StaticLayout(speed + " " + units, speedPaint, textBounds.width(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);

        canvas.save();

        //calculate X and Y coordinates - In this case we want to draw the text in the
        //center of canvas so we calculate
        //text height and number of lines to move Y coordinate to center.
        float textHeight = getTextHeight(speed, speedPaint);
        int numberOfTextLines = staticLayout.getLineCount();
        float textYCoordinate = textBounds.exactCenterY() -
                ((numberOfTextLines * textHeight) / 2);

        //text will be drawn from left
        float textXCoordinate = textBounds.left;

        canvas.translate(textXCoordinate, textYCoordinate);

        staticLayout.draw(canvas);

        canvas.restore();

        /*Paint unitsPaint = new Paint();
        unitsPaint.setAntiAlias(true);
        unitsPaint.setTextSize(50);

        Rect unitsTextBounds = new Rect();
        unitsPaint.getTextBounds(units, 0, units.length(), unitsTextBounds);

        canvas.drawText(units, -unitsTextBounds.left,
                textBounds.height() - textBounds.bottom - unitsTextBounds.height() - unitsTextBounds.bottom, unitsPaint );*/

        return bitmap;
    }

    private float getTextHeight(String text, Paint paint) {

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
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
