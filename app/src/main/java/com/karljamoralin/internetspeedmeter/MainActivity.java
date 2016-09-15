package com.karljamoralin.internetspeedmeter;

import android.app.Activity;
import android.content.Intent;
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
        meterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(ismServiceIntent);
                } else {
                    stopService(ismServiceIntent);
                }
            }
        });

    }

}
