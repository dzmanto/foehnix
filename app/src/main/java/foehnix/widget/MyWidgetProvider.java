package foehnix.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_TRSH = "TRSH";
    static String formattedDate;
    static int disablenite = 0;
    private GlobalConstants tconstants;

    public double rnd1dig(double kritz) {
        kritz = Math.round(10 * kritz);
        return kritz / 10;
    }

    private void updateWidgetIds(Context context) {
        ComponentName componentName = new ComponentName(context, getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.w(this.getClass().getName(), "commenced onDeleted");
        Intent intent = new Intent("ContactWidgetUpdate");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        Util.cancelAlarm(context, sender);
        intent = new Intent("TRSH");
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        Util.cancelAlarm(context, sender);
        // ensure the widget will restart even after a delete event at night
        disablenite = 0;

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        Log.w(this.getClass().getName(), "commenced onDisabled");
        Intent intent = new Intent("ContactWidgetUpdate");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        Util.cancelAlarm(context, sender);
        // ensure the widget will restart even after a disable event at night
        disablenite = 0;

        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.w("AlarmManager", "run enablement proc");
        PendingIntent anIntent = PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
        this.tconstants = new GlobalConstants(context);
        super.onEnabled(context);
    }

    public void onReceive(Context context, Intent intent) {
        Log.w(this.getClass().getName(), "onReceive: intent=" + intent);

        if ("TRSH".equalsIgnoreCase(intent.getAction())) {
            Log.w("TRSH", "trash");
            if (formattedDate == null) {
                updateWidgetIds(context);
            }
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || "android.appwidget.action.BOOT_COMPLETED".equalsIgnoreCase(intent.getAction())) {
            //ensure the widget will start even after a reboot at nite
            disablenite = 0;

            PendingIntent anIntent = PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(anIntent);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
            super.onEnabled(context);
        }

        // act on update button pressed
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equalsIgnoreCase(intent.getAction())) {
            Log.w(this.getClass().getName(), "commenced APPWIDGET_UPDATE");
            Log.w(this.getClass().getName(), intent.getAction());
            updateWidgetIds(context);
        }

        // act on alarm manager
        if ("ContactWidgetUpdate".equalsIgnoreCase(intent.getAction())) {
            Log.w(this.getClass().getName(), "commenced ContactWidgetUpdate");
            Log.w(this.getClass().getName(), intent.getAction());
            if (!isnite() || formattedDate == null) {
                updateWidgetIds(context);
            }
        }

        if ("firststockviewclicked".equalsIgnoreCase(intent.getAction())) {
            Log.w("firststockview", "clicked");

            String url = "http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        }

        if ("secondstockviewclicked".equalsIgnoreCase(intent.getAction())) {
            Log.w("secondstockview", "clicked");

            String url = "http://windundwetter.ch/Stations/filter/abo,altd,chu,cim,loc,meir,neu/show/time,wind,windarrow,qff";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        }

        if ("shareactionclicked".equalsIgnoreCase(intent.getAction())) {
            Log.w("share action", "clicked");

            // register an intent for sharing
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            double currentdeltapress = GlobalConstants.getSharedDouble("currentdeltapress", context);
            if (currentdeltapress != -100) {
                // Add data to the intent, the receiving app will decide what to do with it.
                String txtmsg = "Δp Lugano-Kloten " + rnd1dig(currentdeltapress) + " hPa\nwind gusts [km/h]:";
                txtmsg = txtmsg + "\n" + GlobalConstants.produceTexto();
                i.putExtra(Intent.EXTRA_SUBJECT, "foehnix brief");
                i.putExtra(Intent.EXTRA_TEXT, txtmsg);
                context.startActivity(i);
            }
        }

        if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equalsIgnoreCase(intent.getAction())) {
            onDisabled(context);
        }

        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equalsIgnoreCase(intent.getAction())) {
            ComponentName componentName = new ComponentName(context, getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            onDeleted(context, appWidgetIds);
        }

        if (intent.getAction().toLowerCase().indexOf("enabled") > 0) {
            onEnabled(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews;
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_gray);
            remoteViews.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh_gray);
            remoteViews.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
            remoteViews.setInt(R.id.secondstockview, "setTextColor", Color.GRAY);
            remoteViews.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
            remoteViews.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
            String surl = context.getString(R.string.meteo_url);
            if (this.tconstants == null) {
                Log.w("onUpdate", "enabling tconstants");
                this.tconstants = new GlobalConstants(context);
            }
            new DSPTask(context, remoteViews, this.tconstants, widgetId, appWidgetManager, R.id.firststockview).execute(surl);

            // Register an onClickListener
            Intent intent = new Intent(context, MyWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.updatebutton, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            // Register another onClickListener for pressure diagrams via internet
            remoteViews.setOnClickPendingIntent(R.id.firststockview, getPendingSelfIntent(context, "firststockviewclicked"));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            // Register yet another onClickListener for wind speeds via internet
            remoteViews.setOnClickPendingIntent(R.id.secondstockview, getPendingSelfIntent(context, "secondstockviewclicked"));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            // Register still another onClickListener for sharing via text message
            remoteViews.setOnClickPendingIntent(R.id.sharebutton, getPendingSelfIntent(context, "shareactionclicked"));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }


    public boolean isnite() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("mm");
        if (disablenite < 5) {
            disablenite++;
            return false;
        }
        String mmdate = df.format(c.getTime());
        int mm = Integer.parseInt(mmdate);
        if (mm > -1 && mm < 17) {
            return false;
        }
        df = new SimpleDateFormat("HH");
        String hhdate = df.format(c.getTime());
        int hh = Integer.parseInt(hhdate);
        return hh > 21 || hh < 7;
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
