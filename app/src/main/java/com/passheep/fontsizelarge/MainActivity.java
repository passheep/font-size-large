package com.passheep.fontsizelarge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.passheep.fontsizelarge.service.WidgetService;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        TextView butteryShow = (TextView) findViewById(R.id.tv_battery_show);
        Map<String, String> mm = getBattery();
        butteryShow.setText(mm.get("level") + "--" + mm.get("charging"));

        Intent service = new Intent(this, WidgetService.class);
        startService(service);
    }

    private Map<String, String> getBattery(){
        Map<String, String> mm = new HashMap<>();
        // charging=2  level
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (null == intent) {
            mm.put("level", "暂无");
            mm.put("charging", "1");
        } else {
            mm.put("level", String.valueOf(intent.getIntExtra("level", 0)));
            mm.put("charging", String.valueOf(intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)));
        }
        return mm;
    }
}
