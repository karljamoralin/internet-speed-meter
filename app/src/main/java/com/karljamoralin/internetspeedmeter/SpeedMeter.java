package com.karljamoralin.internetspeedmeter;

import android.net.TrafficStats;

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
    private String mUnits;

    public String getmUnits() {
        return mUnits;
    }

    public void setmUnits(String mUnits) {
        this.mUnits = mUnits;
    }

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
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000000;
            mUnits = " GB";
        }
        else if (mDownloadSpeed >= 1000000) {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000;
            mUnits = " MB";

        }
        else {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000;
            mUnits = " KB";
        }

        if (!mUnits.equals(" KB") && mDownloadSpeedWithDecimals < 100) {
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
