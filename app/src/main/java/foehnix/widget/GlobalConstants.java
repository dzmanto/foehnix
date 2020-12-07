package foehnix.widget;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class GlobalConstants {
	private Context tcontext;
    static double[] pressures;
	static double[] wind_dir;
	static String[] wind_locations_show_marquee;
	static double[] wind_strength;
	static String[] wind_str;

	double getLastButOneDeltaPress() {
		return(this.getSharedDouble("lastbutonedeltapress",this.tcontext));
	}

	double getLastDeltaPress() {
		return(this.getSharedDouble("lastdeltapress",this.tcontext));
	}

    Date getLastDPOverride() {
        return(this.getSharedDate("lastdpoverride",this.tcontext));
    }

	Date getLastNeuOverride() {
		return(this.getSharedDate("lastneuoverride",this.tcontext));
	}

	static Date getSharedDate(String desc, Context cntxt) {
		SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
		Date val;
		try {
			val=new Date(prefs.getLong(desc,-1));
			return(val);
		} catch(Exception e) {
			return(null);
		}
	}

	static double getSharedDouble(String desc, Context cntxt) {
		SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
		double val=Double.longBitsToDouble(prefs.getLong(desc,-100));
		return(val);
	}

    public GlobalConstants(Context cntxt) {
        this.tcontext = cntxt;
    }

    static String getSharedString(String desc, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
        return(prefs.getString(desc,cntxt.getResources().getString(R.string.stock_dsp)));
    }

	static String produceTexto() {
		String texto="";
		for(int i=0; i<wind_locations_show_marquee.length;i++) {
			if(wind_locations_show_marquee[i].equals("y")) {
				texto = texto + wind_str[i] + "\n";
			}
		}
		texto=trim(texto,'\n','\n');
		return(texto);
	}

    void setContext(Context cntxt) {
        this.tcontext = cntxt;
    }

	void setLastButOneDeltaPress(double lbodp) {
		this.storeSharedDouble("lastbutonedeltapress",lbodp,this.tcontext);
	}

	void setLastDeltaPress(double ldp) {
		this.storeSharedDouble("lastdeltapress",ldp,this.tcontext);
	}

    void setLastDPOverride(Date ldpo) {
        this.storeSharedDate("lastdpoverride",ldpo,this.tcontext);
    }

    void setLastNeuOverride(Date lno) {
        this.storeSharedDate("lastneuoverride",lno,this.tcontext);
    }

	static void setPressures(double[] tpressures) {
		pressures=tpressures.clone();
	}

	static void setWindDir(double[] twind_dir) {
		wind_dir=twind_dir.clone();
	}

	static void setWindStrength(double[] twind_strength) {
		wind_strength=twind_strength.clone();
	}

	static void setWindStr(String[] twind_str) {
		wind_str=twind_str.clone();
	}

	static void setWindLocationsShowMarquee(String[] twind_locations_show_marquee) {
		wind_locations_show_marquee=twind_locations_show_marquee.clone();
	}

	static void storeSharedDate(String desc, Date val, Context cntxt) {
		SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(desc, val.getTime());
		editor.apply();
	}

	static void storeSharedDouble(String desc, double val, Context cntxt) {
		SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(desc, Double.doubleToRawLongBits(val));
		editor.apply();
	}

    static void storeSharedString(String desc, String val, Context cntxt) {
        SharedPreferences prefs = cntxt.getSharedPreferences(cntxt.getResources().getString(R.string.preferences_name),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(desc, val);
        editor.apply();
    }

	public static String trim(String string, char leadingChar, char trailingChar){
		return string.replaceAll("^["+leadingChar+"]+|["+trailingChar+"]+$", "");
	}
}
