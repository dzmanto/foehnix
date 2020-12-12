package foehnix.widget;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class GlobalConstants {
    static double[] pressures;
    static double[] wind_dir;
    static String[] wind_locations_show_marquee;
    static double[] wind_strength;
    static String[] wind_str;
    private final Context tcontext;

    public GlobalConstants(Context cntxt) {
        this.tcontext = cntxt;
    }

    static Date getSharedDate(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        Date val;
        try {
            val = new Date(prefs.getLong(desc, -1));
            return (val);
        } catch (Exception e) {
            return (null);
        }
    }

    static double getSharedDouble(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        return (Double.longBitsToDouble(prefs.getLong(desc, -100)));
    }

    static String getSharedString(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        return (prefs.getString(desc, cntxt.getResources().getString(R.string.stock_dsp)));
    }

    static String produceTexto() {
        String texto = "";
        for (int i = 0; i < wind_locations_show_marquee.length; i++) {
            if (wind_locations_show_marquee[i].equals("y")) {
                texto = texto + wind_str[i] + "\n";
            }
        }
        texto = trim(texto, '\n', '\n');
        return (texto);
    }

    static void setPressures(double[] tpressures) {
        pressures = tpressures.clone();
    }

    static void setWindDir(double[] twind_dir) {
        wind_dir = twind_dir.clone();
    }

    static void setWindStrength(double[] twind_strength) {
        wind_strength = twind_strength.clone();
    }

    static void setWindStr(String[] twind_str) {
        wind_str = twind_str.clone();
    }

    static void setWindLocationsShowMarquee(String[] twind_locations_show_marquee) {
        wind_locations_show_marquee = twind_locations_show_marquee.clone();
    }

    static void storeSharedDate(String desc, Date val, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(desc, val.getTime());
        editor.apply();
    }

    static void storeSharedDouble(String desc, double val, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(desc, Double.doubleToRawLongBits(val));
        editor.apply();
    }

    static void storeSharedString(String desc, String val, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(desc, val);
        editor.apply();
    }

    public static String trim(String string, char leadingChar, char trailingChar) {
        return string.replaceAll("^[" + leadingChar + "]+|[" + trailingChar + "]+$", "");
    }

    double getLastButOneDeltaPress() {
        return (getSharedDouble("lastbutonedeltapress", this.tcontext));
    }

    void setLastButOneDeltaPress(double lbodp) {
        storeSharedDouble("lastbutonedeltapress", lbodp, this.tcontext);
    }

    double getLastDeltaPress() {
        return (getSharedDouble("lastdeltapress", this.tcontext));
    }

    void setLastDeltaPress(double ldp) {
        storeSharedDouble("lastdeltapress", ldp, this.tcontext);
    }

    Date getLastDPOverride() {
        return (getSharedDate("lastdpoverride", this.tcontext));
    }

    void setLastDPOverride(Date ldpo) {
        storeSharedDate("lastdpoverride", ldpo, this.tcontext);
    }

    Date getLastNeuOverride() {
        return (getSharedDate("lastneuoverride", this.tcontext));
    }

    void setLastNeuOverride(Date lno) {
        storeSharedDate("lastneuoverride", lno, this.tcontext);
    }
}
