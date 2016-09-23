package com.karljamoralin.internetspeedmeter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.karljamoralin.internetspeedmeter.util.Decoder;
import com.karljamoralin.internetspeedmeter.util.IabBroadcastReceiver;
import com.karljamoralin.internetspeedmeter.util.IabHelper;
import com.karljamoralin.internetspeedmeter.util.IabResult;
import com.karljamoralin.internetspeedmeter.util.Inventory;
import com.karljamoralin.internetspeedmeter.util.Purchase;

public class MainActivity extends Activity {

    IabHelper mHelper;
/*    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String base64EncodedPublicKey = Decoder.decode
                ("miibiJanbGKQHKIg9W0baqefaaocaq8amiibcGkcaqeaPgHbZkGf6iZccjp1ofY6/NdZgyYYuDstGz13f95pmgiPIma22eXhNKtEKTftchaNV2S+JVtIKWgfxwcopMFqke+suocd724kKdipI7Es8WnLo1Vh3jiBZFYFV11p4Uvu1p75yHvs5L30a+RwmKd68v57mrQ4VahGmg7PlrDYX4L3AG2QqeIPJGyI3Ah1+YxK5OWkKUkwsrqMIPu3xj9KmeojXPra1GSEPsA2FTqEs71EflIbHreeEpO6yopRro6ruSExeiIBSsyKmGwr7blEl1zLHZKTmt0FFNA4x0OOA3sMIf0Opm0j8mgmerSMelWS3frufN+Caj5jDdHg+ESHCqidaqab");

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    try {
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /*Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);*/

        final Intent ismServiceIntent = new Intent(this, ISMService.class);
        ismServiceIntent.setPackage(this.getPackageName());

        Switch meterSwitch = (Switch) findViewById(R.id.meter_switch);

        /*Get meter state so that we'll know if it's enabled/disabled by user upon start*/
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref),
                Context.MODE_PRIVATE);
        int state = sharedPref.getInt(getString(R.string.meter_state), 0);
        if (state == 1) {
            meterSwitch.setChecked(true);
        }
        else {
            meterSwitch.setChecked(false);
        }

        final SharedPreferences.Editor editor = sharedPref.edit();

        meterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putInt(getString(R.string.meter_state), 1);
                    editor.commit();
                    startService(ismServiceIntent);
                } else {
                    editor.putInt(getString(R.string.meter_state), 0);
                    editor.commit();
                    stopService(ismServiceIntent);
                }
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }


    public void onTipButtonClicked(View view) {

        try {
            mHelper.launchPurchaseFlow(this, "1", 10001,
                    mPurchaseFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }


    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isSuccess()) {
                Toast.makeText(getApplicationContext(), "Thanks! 1 coffee = +2 hours coding!", Toast
                        .LENGTH_LONG);
                try {
                    mHelper.consumeAsync(purchase,mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }

        }
    };


    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

        }
    };


    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;



            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */



            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase purchase = inventory.getPurchase("1");
            if (purchase != null) {

                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                }
                return;
            }

        }
    };

}
