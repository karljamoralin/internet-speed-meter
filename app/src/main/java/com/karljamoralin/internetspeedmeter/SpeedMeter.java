package com.karljamoralin.internetspeedmeter;

import android.net.TrafficStats;
import android.os.SystemClock;
import android.widget.TextView;

/**
 * Created by jamorali on 9/5/2016.
 */
public class SpeedMeter implements Runnable {

    final TaskRunnableSpeedMeterMethods mMainActivity;
    private long mDownloadSpeedKB;

    public SpeedMeter(TaskRunnableSpeedMeterMethods mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void run() {

        mMainActivity.setSpeedMeterThread(Thread.currentThread());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while(true) {

            long rxBytesPrevious = TrafficStats.getTotalRxBytes();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long rxBytesCurrent = TrafficStats.getTotalRxBytes();

            long downloadSpeed = rxBytesCurrent - rxBytesPrevious;

            setmDownloadSpeedKB(downloadSpeed/1000);

            mMainActivity.setInternetSpeed(this);

        }

    }

    public long getmDownloadSpeedKB() {
        return mDownloadSpeedKB;
    }

    public void setmDownloadSpeedKB(long mDownloadSpeedKB) {
        this.mDownloadSpeedKB = mDownloadSpeedKB;
    }

    interface TaskRunnableSpeedMeterMethods {
        void setSpeedMeterThread(Thread currentThread);
        void setInternetSpeed(SpeedMeter speedMeter);
    }

}
