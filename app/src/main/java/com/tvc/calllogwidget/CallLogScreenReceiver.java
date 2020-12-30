package com.tvc.calllogwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import static com.tvc.calllogwidget.CallLogWidget.NOTIFICATION_ACTION;

public class CallLogScreenReceiver extends BroadcastReceiver {
    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            screenOff = false;
        }
        else if (intent.getAction().equals(NOTIFICATION_ACTION)) {
            Intent i = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                    .putExtra(Settings.EXTRA_CHANNEL_ID, CallLogBackgroundService.NOTIFICATION_CHANNEL_ID)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return;
        }

        Intent i = new Intent(context, CallLogBackgroundService.class);
        i.putExtra("screen_off", screenOff);
        context.startService(i);
    }
}
