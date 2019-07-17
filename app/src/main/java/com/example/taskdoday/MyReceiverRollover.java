package com.example.taskdoday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiverRollover extends BroadcastReceiver {
    public MyReceiverRollover() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, MyNewIntentServiceRollover.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent1);
        }
        context.startService(intent1);
    }
}