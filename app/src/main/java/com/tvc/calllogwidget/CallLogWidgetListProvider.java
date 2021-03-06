package com.tvc.calllogwidget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import net.dankito.fritzbox.model.Call;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static com.tvc.calllogwidget.CallLogWidget.DIALNUMBER_ACTION;

public class CallLogWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context _context;
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM. HH:mm");
    private int _appWidgetId;
    private int[] _appWidgetIds;

    public CallLogWidgetListProvider(Context context, Intent intent) {

        this._context = context;
        this._appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        this._appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Call call = CallLogWidget.s_CallLog.get(position);
        final RemoteViews rowRemoteViews = new RemoteViews(_context.getPackageName(), R.layout.activity_listview);

        try
        {
            switch(call.getType())
            {
                case INCOMING_CALL:
                case ONGOING_INCOMING_CALL:
                    rowRemoteViews.setImageViewResource(R.id.callType, R.drawable.incoming_call);
                    break;
                case MISSED_CALL:
                    rowRemoteViews.setImageViewResource(R.id.callType, R.drawable.missed_call);
                    break;
                case OUTGOING_CALL:
                case ONGOING_OUTGOING_CALL:
                    rowRemoteViews.setImageViewResource(R.id.callType, R.drawable.outgoing_call);
                    break;
                case REJECTED_CALL:
                    rowRemoteViews.setImageViewResource(R.id.callType, R.drawable.rejected_call);
                    break;
                default:
                    //leave it
                    break;
            }

            String date = DATE_FORMATTER.format(call.getDate());
            rowRemoteViews.setTextViewText(R.id.callDate, date);

            String callername = call.getCallerName();
            if(callername == null || callername.isEmpty())
            {
                callername = call.getCallerNumber();
            }
            rowRemoteViews.setTextViewText(R.id.callerName, callername);
            if(call.getCallerNumber() != null && !call.getCallerNumber().isEmpty())
            {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CallLogWidget.s_Context);
                boolean addPrefix = sharedPref.getBoolean("addprefixtotelephonenumber_preference", true);
                String prefix = sharedPref.getString("prefixfortelephonenumber_preference", "+49");

                Intent fillInIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                String phoneNumber = call.getCallerNumber();
                if(addPrefix && phoneNumber.startsWith("0") && !phoneNumber.startsWith("00"))
                {
                    phoneNumber = phoneNumber.replaceFirst("0", prefix);
                }
                fillInIntent.setData(Uri.parse("tel:" + phoneNumber));
                fillInIntent.putExtra("phone_number", phoneNumber);
                /*
                fillInIntent.putExtra(Settings.EXTRA_APP_PACKAGE, CallLogWidget.s_Context.getPackageName());
                fillInIntent.putExtra(Settings.EXTRA_CHANNEL_ID, CallLogBackgroundService.NOTIFICATION_CHANNEL_ID);
*/
                // Make it possible to distinguish the individual on-click
                // action of a given item
                rowRemoteViews.setOnClickFillInIntent(R.id.rowLayout, fillInIntent);
            }
        }
        catch(Exception e)
        {
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "getViewAt " + position + " failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return rowRemoteViews;
    }

    @Override
    public int getCount() {

        return CallLogWidget.s_CallLog != null ? CallLogWidget.s_CallLog.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() { }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {

        return 2;
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }
}