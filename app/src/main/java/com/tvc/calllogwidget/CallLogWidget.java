package com.tvc.calllogwidget;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import net.dankito.fritzbox.model.Call;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CallLogWidget extends AppWidgetProvider {
    private static int JOB_ID = 4711;
    private static int DELAY = 1000;
    private static int BACKOFF = 5000;

    public static List<Call> s_CallLog = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static Context s_Context = null;
    public static RemoteViews s_LastViews = null;
    public static int[] s_AppWidgetIds = null;
    public static int s_DateWidth = 0;
    public static Date s_LastUpdate = new Date(0);
    public static boolean s_ForceUpdate = false;

    public static String REFRESH_ACTION = "com.tvc.calllogwidget.REFRESH";
    public static String DIALNUMBER_ACTION = "com.tvc.calllogwidget.DIALNUMBER";
    public static String NOTIFICATION_ACTION = "com.tvc.calllogwidget.NOTIFICATION";

    public static JobScheduler s_JobScheduler = null;

    public static void refreshData() {
        if(s_Context == null) {
            return;
        }

        try
        {
            ActivityManager manager = (ActivityManager) s_Context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (CallLogBackgroundService.class.getName().equals(service.service.getClassName())) {
                    return;
                }
            }

            Intent intent = new Intent(s_Context, CallLogBackgroundService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // only for newer versions
                s_Context.startForegroundService(intent);
            } else {
                s_Context.startService(intent);
            }
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "refreshData failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    static protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, CallLogWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        if(s_Context == null) {
            s_Context = context;
        }
        refreshData();

        try
        {
            // Construct the RemoteViews object
            s_LastViews = new RemoteViews(context.getPackageName(), R.layout.call_log_widget);
            CharSequence widgetText = context.getString(R.string.appwidget_text);
            s_LastViews.setTextViewText(R.id.appwidget_text, widgetText);
            s_LastViews.setEmptyView(R.id.appwidget_listview, android.R.id.empty);
            s_LastViews.setOnClickPendingIntent(R.id.refreshButton, getPendingSelfIntent(context, REFRESH_ACTION));

            Intent intent = new Intent(context, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            s_LastViews.setOnClickPendingIntent(R.id.settingsButton, pendingIntent);

            Intent clickIntent = new Intent(context, CallLogWidget.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            clickIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            clickIntent.setAction(DIALNUMBER_ACTION);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            s_LastViews.setPendingIntentTemplate(R.id.appwidget_listview, clickPendingIntent);

            Paint paint = new Paint();
            TextView tv = new TextView(context);
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            paint.setTypeface(tv.getTypeface());
            paint.setTextSize(tv.getTextSize());

            String text = "WW.WW. WW:WW";
            tv.setText(text);
            s_DateWidth = (int)paint.measureText(text);

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams")
            View v = layoutInflater.inflate(R.layout.activity_listview, null, false);
            View view = v.findViewById(R.id.callDate);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = s_DateWidth;
            view.setLayoutParams(layoutParams);

            s_AppWidgetIds = new int[]{appWidgetId};

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, s_LastViews);
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "startForeground failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if(s_Context == null) {
            s_Context = context;
        }
        refreshData();

        try
        {
            for (int appWidgetId : appWidgetIds) {
                onUpdateInternal(context, appWidgetManager, appWidgetId);
            }

            super.onUpdate(context, appWidgetManager, appWidgetIds);
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "startForeground failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        if(s_Context == null) {
            s_Context = context;
        }
        refreshData();

        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        if(s_Context == null) {
            s_Context = context;
        }
        refreshData();

        super.onDisabled(context);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        if(s_Context == null) {
            s_Context = context;
        }
        refreshData();

        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(s_Context == null) {
            s_Context = context;
        }

        try
        {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(context, CallLogWidget.class));

            Bundle extras = intent.getExtras();
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            // single appWidgetId as parameter
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                appWidgetIds = new int[] {appWidgetId};
            }

            if (REFRESH_ACTION.equals(intent.getAction())) {
//                Toast.makeText(context, "Refreshing data...", Toast.LENGTH_SHORT).show();
                s_ForceUpdate = true;
            }
            else if(DIALNUMBER_ACTION.equals(intent.getAction())) {
                String phoneNumber = intent.getStringExtra("phone_number");

                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
//                if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(dialIntent);
//                }
            }
            else
            {
                // multiple appWidgetIds as parameter
                if (appWidgetIds != null) {
                    for (int id : appWidgetIds) {
                        if ("REFRESH".equals(intent.getAction())){
                            Toast.makeText(context, "refreshing data ...", Toast.LENGTH_SHORT).show();
                            s_ForceUpdate = true;
                        }
                        else
                        {
/*
                            if(s_LastViews != null)
                            {
                                //RemoteViews remoteViews = new RemoteViews(context.PackageName, Resource.Layout.Widget);
                                String PACKAGE_NAME = context.getPackageName();
                                //set
                                Intent svcIntent = new Intent(context, CallLogWidgetService.class);
                                svcIntent.setPackage(PACKAGE_NAME);
                                svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                                svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

                                s_LastViews.setRemoteAdapter(R.id.appwidget_listview, svcIntent);
                                mgr.updateAppWidget(id, s_LastViews);
                                //refresh:
                            }
*/
                            // process a single appWidgetId
                            onReceiveInternal(context, intent, id);
                        }
                    }
                }
            }

            super.onReceive(context, intent);
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "startForeground failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void onReceiveInternal(Context context, Intent intent, int appWidgetId) {
        AppWidgetManager appWidgetMgr = AppWidgetManager.getInstance(context);
        String action = intent.getAction();

        onUpdateInternal(context, appWidgetMgr, appWidgetId);
    }

    private void onUpdateInternal(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try
        {
            s_LastViews = new RemoteViews(
                    context.getPackageName(),
                    R.layout.call_log_widget
            );

            Intent intent = new Intent(context, CallLogWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            DecimalFormat mFormat= new DecimalFormat("00");
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
            cal.setTime(s_LastUpdate);
            String date = s_Context.getString(R.string.last_update_text) + " " + mFormat.format(cal.get(Calendar.DAY_OF_MONTH)) + "." + mFormat.format(cal.get(Calendar.MONTH)+1) + "." + mFormat.format(cal.get(Calendar.YEAR)) + " " + mFormat.format(cal.get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format(cal.get(Calendar.MINUTE)) + ":" + mFormat.format(cal.get(Calendar.SECOND));
            s_LastViews.setTextViewText(R.id.lastUpdateText, date);
            s_LastViews.setRemoteAdapter(R.id.appwidget_listview, intent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // only for newer versions
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }

            int[] ids = {appWidgetId};
            intent = new Intent(context, CallLogWidgetService.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);

//            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, CallLogWidgetListProvider.class));
//            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.appwidget_listview);

            // Trigger widget layout update
            AppWidgetManager.getInstance(context).updateAppWidget( new ComponentName(context, CallLogWidget.class), s_LastViews);

            updateAppWidget(context, appWidgetManager, appWidgetId);
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "startForeground failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}