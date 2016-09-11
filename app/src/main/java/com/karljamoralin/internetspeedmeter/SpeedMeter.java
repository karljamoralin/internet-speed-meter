package com.karljamoralin.internetspeedmeter;

import android.net.TrafficStats;
import android.text.StaticLayout;

/**
 * Created by jamorali on 9/5/2016.
 */
public class SpeedMeter implements Runnable {

    final private SpeedMeterListener mSpeedMeterListener;
    private String mDownloadSpeedOutput;
    private float mDownloadSpeedWithDecimals;
    private long mRxBytesPrevious;
    private long mRxBytesCurrent;
    private long mDownloadSpeed;
    private StaticLayout mOutput;

    public StaticLayout getmOutput() {
        return mOutput;
    }

    public void setmOutput(StaticLayout mOutput) {
        this.mOutput = mOutput;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    private String units;


    public SpeedMeter(SpeedMeterListener mainActivity) {
        mSpeedMeterListener = mainActivity;
    }


    @Override
    public void run() {

        mSpeedMeterListener.speedMeterThreadCreated(Thread.currentThread());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while(true) {

            getDownloadSpeed();

            mSpeedMeterListener.downloadSpeedUpdated(this);

        }

    }


    private void getDownloadSpeed() {

        mRxBytesPrevious = TrafficStats.getTotalRxBytes();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mRxBytesCurrent = TrafficStats.getTotalRxBytes();

        mDownloadSpeed = mRxBytesCurrent - mRxBytesPrevious;

        if (mDownloadSpeed >= 1000000000) {
            mDownloadSpeedWithDecimals = mDownloadSpeed / 1000000000;
            units = " GB";
        }
        else if (mDownloadSpeed >= 1000000) {
            mDownloadSpeedWithDecimals = mDownloadSpeed / 1000000;
            units = " MB";
        }
        else {
            mDownloadSpeedWithDecimals = mDownloadSpeed / 1000;
            units = " KB";
        }

        if (!units.equals(" KB") && mDownloadSpeed < 100) {
            mDownloadSpeedOutput = String.format("%.1f", mDownloadSpeedWithDecimals);
        }
        else {
            mDownloadSpeedOutput = Integer.toString((int) mDownloadSpeedWithDecimals);
        }

    }


    public String getmDownloadSpeedOutput() {
        return mDownloadSpeedOutput;
    }


    public void setmDownloadSpeedOutput(String mDownloadSpeedOutput) {
        this.mDownloadSpeedOutput = mDownloadSpeedOutput;
    }


    interface SpeedMeterListener {
        void speedMeterThreadCreated(Thread currentThread);
        void downloadSpeedUpdated(SpeedMeter speedMeter);
    }

}
