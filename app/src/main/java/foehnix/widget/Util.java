package foehnix.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

public class Util {

    public static void scheduleUpdate(Context context) {
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // String interval = prefs.getString(SettingsActivity.INTERVAL_PREF, null);
        Log.w("Util", "run scheduleUpdate");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 60 * 1000;

        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMillis, pi);
    }

    public static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(MyWidgetProvider.ACTION_TRSH);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    public static void cancelAlarm(Context context, PendingIntent pi) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null) {
            // return an empty spannable if the html is null
            return new SpannableString("");
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }
}
