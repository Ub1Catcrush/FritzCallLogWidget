package com.tvc.calllogwidget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class CallLogWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new CallLogWidgetListProvider(this.getApplicationContext(), intent);
    }
}