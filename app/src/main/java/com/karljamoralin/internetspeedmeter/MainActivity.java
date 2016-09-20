package com.karljamoralin.internetspeedmeter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

}
