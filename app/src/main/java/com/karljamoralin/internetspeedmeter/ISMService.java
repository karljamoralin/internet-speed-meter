package com.karljamoralin.internetspeedmeter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Locale;

/**
 * Created by jamorali on 9/15/2016.
 */
public class ISMService extends IntentService {

    private final int mNotificationId = 1;
    private Handler mHandler;
    private Notification.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private String mDownloadSpeedOutput;
    private String mUnits;
    private boolean mDestroyed = false;

    public ISMService() {
        super("ISMService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        initializeNotification();

        while (!mDestroyed) {

            getDownloadSpeed();

            Message completeMessage = mHandler.obtainMessage(1);
            completeMessage.sendToTarget();

        }
    }

    private void initializeNotification() {

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {

                if (mDestroyed) {
                    return;
                }

                Bitmap bitmap = createBitmapFromString(mDownloadSpeedOutput, mUnits);

                Icon icon = Icon.createWithBitmap(bitmap);

                mBuilder.setSmallIcon(icon);

                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            }

        };

        mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(Icon.createWithBitmap(createBitmapFromString("0", "KB")));
        mBuilder.setContentTitle("");
        mBuilder.setVisibility(Notification.VISIBILITY_SECRET);
        mBuilder.setOngoing(true);

        /*Creates a special PendingIntent so that the app will open when the notification window
        is tapped*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent
                .FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);


        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private Bitmap createBitmapFromString(String speed, String units) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(55);
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
        canvas.drawText(speed, width / 2 + 5, 50, paint);
        canvas.drawText(units, width / 2, 90, unitsPaint);

        return bitmap;
    }

    private void getDownloadSpeed() {

        long mRxBytesPrevious = TrafficStats.getTotalRxBytes();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long mRxBytesCurrent = TrafficStats.getTotalRxBytes();

        long mDownloadSpeed = mRxBytesCurrent - mRxBytesPrevious;

        float mDownloadSpeedWithDecimals;

        if (mDownloadSpeed >= 1000000000) {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000000;
            mUnits = " GB";
        } else if (mDownloadSpeed >= 1000000) {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000;
            mUnits = " MB";

        } else {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000;
            mUnits = " KB";
        }


        if (!mUnits.equals(" KB") && mDownloadSpeedWithDecimals < 100) {
            mDownloadSpeedOutput = String.format(Locale.US, "%.1f", mDownloadSpeedWithDecimals);
        } else {
            mDownloadSpeedOutput = Integer.toString((int) mDownloadSpeedWithDecimals);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mNotifyMgr.cancelAll();
    }
}
