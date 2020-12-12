package foehnix.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Util {

    public static void scheduleUpdate(Context context) {
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // String interval = prefs.getString(SettingsActivity.INTERVAL_PREF, null);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 60 * 1000;

        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMillis, pi);
    }

    private static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(MyWidgetProvider.ACTION_TRSH);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public static void clearUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));
    }
}
