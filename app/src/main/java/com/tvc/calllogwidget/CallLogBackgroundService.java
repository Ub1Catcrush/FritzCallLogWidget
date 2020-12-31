package com.tvc.calllogwidget;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.CsvParser;
import net.dankito.fritzbox.services.DigestService;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.utils.web.OkHttpWebClient;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.tvc.calllogwidget.CallLogWidget.NOTIFICATION_ACTION;

public class CallLogBackgroundService extends Service
{
    public static String NOTIFICATION_CHANNEL_ID = "FCL";
    public static int NOTIFICATION_ID = 101;
    private static String LOG_NAME = "FritzCallLogBG";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private boolean inProgress;
    private boolean screenOff;

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if(serviceHandler != null)
            {
                serviceHandler.post(() -> {
                    Message msg = serviceHandler.obtainMessage();
                    serviceHandler.sendMessage(msg);
                });
            }
        }
    };

    private String dateFormatString = "dd.MM.yyyy HH:mm:ss.SSS";
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper)
        {
            super(looper);
            inProgress = false;
            timer.scheduleAtFixedRate(task, 0, 10000); // Executes the task every 15 seconds.
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_NAME, "handle message " + dateFormat.format(new Date(System.currentTimeMillis())) + " schedule would have been " + dateFormat.format(new Date(task.scheduledExecutionTime())));

            if(screenOff || inProgress)
                return;
            else if(!isNetworkAvailable())
                return;

            super.handleMessage(msg);

            try {
                inProgress = true;

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CallLogWidget.s_Context);
                String type = sharedPref.getString("connectiontype_preference", "");
                String address = sharedPref.getString("connectiondomain_preference", "");
                String port = sharedPref.getString("connectionport_preference", "");
                String user = sharedPref.getString("accountname_preference", "");
                String password = sharedPref.getString("accountpassword_preference", "");
                String refresh_period = sharedPref.getString("refreshperiod_preference", "15");

                Date now = new Date();

                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.MINUTE, -1 * Integer.parseInt(refresh_period));
                Date xMinutesBack = cal.getTime();
                if(!CallLogWidget.s_ForceUpdate && CallLogWidget.s_LastUpdate.after(xMinutesBack))
                {
                    Log.d(LOG_NAME, "NO update due (NO forced and NO timed update)");
                   return;
                }
                Log.d(LOG_NAME, "UPDATING DATA...");

                UserSettings userSettings = new UserSettings(type, address, port, user, password);

                FritzBoxClient fc = null;

                try
                {
                    fc = new FritzBoxClient(userSettings, new OkHttpWebClient(), new DigestService(), new CsvParser());
                }
                catch (Exception e)
                {
                    if(CallLogWidget.s_Context != null)
                        Toast.makeText(CallLogWidget.s_Context, "Connection to data source failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(LOG_NAME, "FritzBoxClient creation / connection failed", e);
                }
                final List<GetCallListResponse> responseList = new ArrayList<>();
                final CountDownLatch countDownLatch = new CountDownLatch(1);

                if(fc != null)
                {
                    try
                    {
                        fc.getCallListAsync(response -> {
                            responseList.add(response);
                            countDownLatch.countDown();
                        });
                    }
                    catch (Exception e)
                    {
                        if(CallLogWidget.s_Context != null)
                            Toast.makeText(CallLogWidget.s_Context, "getCallListAsync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(LOG_NAME, "getCallListAsync failed", e);
                    }

                    try {
                        countDownLatch.await(2, TimeUnit.MINUTES);
                    }
                    catch(Exception ignored) {
                        Log.d(LOG_NAME, "Hit countDownLatch 2 minutes");
                    }

                    GetCallListResponse response = responseList.get(0);

                    if(response != null && response.getCallList() != null && !response.getCallList().isEmpty())
                    {
                        CallLogWidget.s_CallLog.clear();
                        CallLogWidget.s_CallLog.addAll(response.getCallList());

                        AppWidgetManager mgr = AppWidgetManager.getInstance(CallLogWidget.s_Context);
                        int[] allWidgetIds = AppWidgetManager.getInstance(CallLogWidget.s_Context).getAppWidgetIds(new ComponentName(CallLogWidget.s_Context, CallLogWidget.class));

                        CallLogWidget.s_LastUpdate = new Date();
                        CallLogWidget.s_ForceUpdate = false;
                        CallLogWidget.s_FailedUpdates = 0;

                        mgr.notifyAppWidgetViewDataChanged(allWidgetIds, R.id.appwidget_listview);

                        Intent intent = new Intent(CallLogWidget.s_Context, CallLogWidget.class);
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                        sendBroadcast(intent);
                    }
                    else
                    {
                        Log.d(LOG_NAME, "No data retrieved");
                        CallLogWidget.s_FailedUpdates += 1;
                    }
                }
            } catch (Exception e){
                if(CallLogWidget.s_Context != null)
                    Toast.makeText(CallLogWidget.s_Context, "Data retrieval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(LOG_NAME, "Data retrieval failed", e);
            }
            finally {
                inProgress = false;
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
//            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            startForeground();
        }
        else {
            startForeground(NOTIFICATION_ID, new Notification());
        }

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new CallLogScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void startForeground(){
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                /*
                String NOTIFICATION_CHANNEL_ID = "com.tvc.calllogwidget";
                String channelName = "CallLogBackgroundService";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);
*/
                Intent intent = new Intent(this, CallLogScreenReceiver.class);
                intent.setAction(NOTIFICATION_ACTION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent,
                        0
                );

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.fcl)
//                        .setContentTitle("App is running in background")
                        .setContentTitle(getString(R.string.fcs_notification_title))
                        .setContentText(getString(R.string.fcs_notification))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();

                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Exception e){
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "startForeground failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(LOG_NAME, "startForeground failed", e);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.hasExtra("screen_off")) {
            boolean screenOff = intent.getBooleanExtra("screen_off", false);
            if (screenOff) {
                this.screenOff = screenOff;
            } else {
                this.screenOff = screenOff;

                if (serviceHandler != null) {
                    Message msg = serviceHandler.obtainMessage();
                    serviceHandler.sendMessage(msg);
                }
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private Boolean isNetworkAvailable() {
        if(CallLogWidget.s_Context == null) {
            return false;
        }

        try
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) CallLogWidget.s_Context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return true;
                        }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                            return true;
                        }
                    }
                }

                else {
                    try {
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                            return true;
                        }
                    } catch (Exception e) {
                        if(CallLogWidget.s_Context != null)
                            Toast.makeText(CallLogWidget.s_Context, "Connectivity check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(LOG_NAME, "Connectivity check failed", e);
                    }
                }
            }
        }
        catch (Exception e)
        {
            if(CallLogWidget.s_Context != null)
                Toast.makeText(CallLogWidget.s_Context, "Connectivity check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(LOG_NAME, "Connectivity check failed", e);
        }


        return false;
    }

}
