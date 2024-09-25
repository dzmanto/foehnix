package foehnix.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Date;

public class GlobalConstants {
    static double[] pressures;
    static double[] wind_dir;
    static String[] wind_locations_show_marquee;
    static String wind_locations_show_marquee_b64;
    static double[] wind_strength;
    static String[] wind_str;
    static String wind_str_b64;
    private final Context tcontext;

    public GlobalConstants(Context cntxt) {
        this.tcontext = cntxt;
    }

    static Date getSharedDate(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        Date val;
        try {
            val = new Date(prefs.getLong(desc, -1));
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    static double getSharedDouble(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        return Double.longBitsToDouble(prefs.getLong(desc, -100));
    }

    static String getSharedString(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name), Context.MODE_PRIVATE);
        return prefs.getString(desc, cntxt.getResources().getString(R.string.stock_dsp));
    }

    static String produceTexto() {
        String texto = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                byte [] data = Base64.getDecoder().decode(wind_locations_show_marquee_b64);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                String[] wsm;
                wsm = (String[]) ois.readObject();
                ois.close();
                data = Base64.getDecoder().decode(wind_str_b64);
                ois = new ObjectInputStream(new ByteArrayInputStream(data));
                String[] ws;
                ws = (String[]) ois.readObject();
                ois.close();

                for (int i = 0; i < wsm.length; i++) {
                    if (wsm[i].equals("y")) texto = texto + ws[i] + "\n";
                }
                texto = trim(texto, '\n', '\n');
            } catch(java.io.IOException e) {
                Log.w("produceTexto...",e.toString());
            } catch(java.lang.ClassNotFoundException f) {
                Log.w("produceTexto...",f.toString());
            }
        } else {
            for (int i = 0; i < wind_locations_show_marquee.length; i++) {
                if (wind_locations_show_marquee[i].equals("y")) {
                    texto = texto + wind_str[i] + "\n";
                }
            }
            texto = trim(texto, '\n', '\n');
        }
        return texto;
    }

    static String produceTexto(Context cntxt) {
        String texto = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                String wind_locations_show_marquee_shared = getSharedString("wind_locations_show_marquee", cntxt);
                byte [] data = Base64.getDecoder().decode(wind_locations_show_marquee_shared);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                String[] wsm;
                wsm = (String[]) ois.readObject();
                ois.close();
                String wind_str_shared= getSharedString("wind_str", cntxt);
                data = Base64.getDecoder().decode(wind_str_shared);
                ois = new ObjectInputStream(new ByteArrayInputStream(data));
                String[] ws;
                ws = (String[]) ois.readObject();
                ois.close();

                for (int i = 0; i < wsm.length; i++) {
                    if (wsm[i].equals("y")) texto = texto + ws[i] + "\n";
                }
                texto = trim(texto, '\n', '\n');
            } catch(java.io.IOException e) {
                Log.w("produceTexto...",e.toString());
            } catch(java.lang.ClassNotFoundException f) {
                Log.w("produceTexto...",f.toString());
            }
        } else {
            for (int i = 0; i < wind_locations_show_marquee.length; i++) {
                if (wind_locations_show_marquee[i].equals("y")) {
                    texto = texto + wind_str[i] + "\n";
                }
            }
            texto = trim(texto, '\n', '\n');
        }
        return texto;
    }

    static void setWindStr(String[] twind_str, Context cntxt) {
        wind_str = twind_str.clone();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(twind_str);
                oos.close();
                wind_str_b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                storeSharedString("wind_str", wind_str_b64, cntxt);
            } catch(IOException e) {
                Log.w("setWindStr...",e.toString());
            }
        }
    }

    static void setWindLocationsShowMarquee(String[] twind_locations_show_marquee) {
        wind_locations_show_marquee = twind_locations_show_marquee.clone();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(twind_locations_show_marquee);
                oos.close();
                wind_locations_show_marquee_b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch(IOException e) {
                Log.w("setWindLocations...",e.toString());
            }
        }
    }

    static void setWindLocationsShowMarquee(String[] twind_locations_show_marquee, Context cntxt) {
        wind_locations_show_marquee = twind_locations_show_marquee.clone();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(twind_locations_show_marquee);
                oos.close();
                wind_locations_show_marquee_b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                storeSharedString("wind_locations_show_marquee", wind_locations_show_marquee_b64, cntxt);
            } catch(IOException e) {
                Log.w("setWindLocations...",e.toString());
            }
        }
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
        return getSharedDouble("lastbutonedeltapress", this.tcontext);
    }

    void setLastButOneDeltaPress(double lbodp) {
        storeSharedDouble("lastbutonedeltapress", lbodp, this.tcontext);
    }

    double getLastDeltaPress() {
        return getSharedDouble("lastdeltapress", this.tcontext);
    }

    void setLastDeltaPress(double ldp) {
        storeSharedDouble("lastdeltapress", ldp, this.tcontext);
    }

    Date getLastDPOverride() {
        return getSharedDate("lastdpoverride", this.tcontext);
    }

    void setLastDPOverride(Date ldpo) {
        storeSharedDate("lastdpoverride", ldpo, this.tcontext);
    }

    Date getLastNeuOverride() {
        return getSharedDate("lastneuoverride", this.tcontext);
    }

    void setLastNeuOverride(Date lno) {
        storeSharedDate("lastneuoverride", lno, this.tcontext);
    }
}
