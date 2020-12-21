package foehnix.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DSPTask extends AsyncTask<String, Void, Void> {
    private final RemoteViews views;
    int WidgetID;
    private String firsttextResult = "";
    private String secondtextResult = "";
    private String thirdtextResult = "";
    private String cpy = "";
    private GlobalConstants tconstants;
    private Context cntxt;
    private AppWidgetManager WidgetManager;
    private int TextViewID;

    private double deltapress;

    private double wind_max = -1;
    private int wind_max_idx = -1;

    private String[] wind_locations;
    private String[] wind_locations_ns;
    private String[] wind_locations_short;
    private String[] wind_locations_show_marquee;

    public DSPTask(Context cntxt, RemoteViews views, GlobalConstants cnstnts, int appWidgetID,
                   AppWidgetManager appWidgetManager, int textViewID) {
        this.cntxt = cntxt;
        this.views = views;
        this.WidgetID = appWidgetID;
        this.WidgetManager = appWidgetManager;
        this.TextViewID = textViewID;
        this.tconstants = cnstnts;

        wind_locations = this.cntxt.getResources().getStringArray(R.array.wind_locations);
        wind_locations_ns = this.cntxt.getResources().getStringArray(R.array.wind_locations_ns);
        wind_locations_short = this.cntxt.getResources().getStringArray(R.array.wind_locations_short);
        wind_locations_show_marquee = this.cntxt.getResources().getStringArray(R.array.wind_locations_show_marquee);
    }

    public DSPTask(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID) {
        this.views = views;
        this.WidgetID = appWidgetID;
        this.WidgetManager = appWidgetManager;
        this.TextViewID = textViewID;

        wind_locations = this.cntxt.getResources().getStringArray(R.array.wind_locations);
        wind_locations_ns = this.cntxt.getResources().getStringArray(R.array.wind_locations_ns);
        wind_locations_short = this.cntxt.getResources().getStringArray(R.array.wind_locations_short);
        wind_locations_show_marquee = this.cntxt.getResources().getStringArray(R.array.wind_locations_show_marquee);
    }

    public DSPTask(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager) {
        this.views = views;
        this.WidgetID = appWidgetID;
        this.WidgetManager = appWidgetManager;
    }

    public DSPTask(RemoteViews views) {
        this.views = views;
    }

    @Override
    protected Void doInBackground(String... murls) {
        URL textUrl;
        double klopress = -1;
        double locpress = -1;
        double lugpress = -1;
        double neudir = -1;
        double neuwnd = -1;
        double smapress = -1;
        int PRESS_IDX = this.cntxt.getResources().getInteger(R.integer.PRESS_IDX);
        int WINDDIR_IDX = this.cntxt.getResources().getInteger(R.integer.WINDDIR_IDX);
        int WINDSPD_IDX = this.cntxt.getResources().getInteger(R.integer.WINDSPD_IDX);

        double[] pressures = new double[wind_locations.length];
        double[] wind_dir = new double[wind_locations.length];
        double[] wind_strength = new double[wind_locations.length];
        String[] wind_str = new String[wind_locations.length];
        for (int i = 0; i < wind_locations.length; i++) {
            wind_dir[i] = -1;
            wind_strength[i] = -1;
            pressures[i] = -1;
        }

        try {
            cpy = murls[0];
            textUrl = new URL(murls[0]);
            URLConnection urlConnection = textUrl.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.connect();
            InputStream resultingInputStream;
            String encoding = urlConnection.getContentEncoding();
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                resultingInputStream = new GZIPInputStream(urlConnection.getInputStream());
            } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
                resultingInputStream = new InflaterInputStream(urlConnection.getInputStream(), new Inflater(true));
            } else {
                resultingInputStream = urlConnection.getInputStream();
            }
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(resultingInputStream));
            String StringBuffer;
            while ((StringBuffer = bufferReader.readLine()) != null) {
                for (int i = 0; i < wind_locations.length; i++) {
                    if (StringBuffer.length() > 3 && StringBuffer.substring(0, 3).equals(wind_locations_short[i])) {
                        String[] separated = StringBuffer.split(";");
                        if (separated.length < WINDDIR_IDX)
                            separated = StringBuffer.split(","); // just in case they switch from ; to ,
                        String str2proc = separated[PRESS_IDX].trim();
                        if (str2proc.length() > 0 && !str2proc.equals("-"))
                            pressures[i] = Double.parseDouble(str2proc);
                        if (StringBuffer.startsWith("KLO")) klopress = pressures[i];
                        if (StringBuffer.startsWith("LUG")) lugpress = pressures[i];
                        if (StringBuffer.startsWith("OTL")) locpress = pressures[i];
                        if (StringBuffer.startsWith("SMA")) smapress = pressures[i];

                        if (keeponseparated(separated, WINDDIR_IDX, WINDSPD_IDX)) {
                            wind_dir[i] = Double.parseDouble(separated[WINDDIR_IDX]);
                            wind_strength[i] = Double.parseDouble(separated[WINDSPD_IDX]);
                            wind_str[i] = wind_locations[i] + " " + deg2abc(wind_dir[i]).trim() + wind_strength[i];
                            if (StringBuffer.startsWith("NEU"))
                                neuwnd = wind_strength[i];
                        }
                    }
                }
            }
            bufferReader.close();

            GlobalConstants.setPressures(pressures);
            GlobalConstants.setWindDir(wind_dir);
            GlobalConstants.setWindLocationsShowMarquee(wind_locations_show_marquee);
            GlobalConstants.setWindStrength(wind_strength);
            GlobalConstants.setWindStr(wind_str);

            firsttextResult = "-";
            deltapress = -100;
            if (klopress != -1 && lugpress != -1 && TextViewID == R.id.firststockview) {
                deltapress = lugpress - klopress;
                firsttextResult = "" + rnd1dig(deltapress);
            } else if (smapress != -1 && lugpress != -1 && TextViewID == R.id.firststockview) {
                deltapress = lugpress - smapress;
                firsttextResult = "" + rnd1dig(deltapress);
            } else if (klopress != -1 && locpress != -1 && TextViewID == R.id.firststockview) {
                deltapress = locpress - klopress;
                firsttextResult = "" + rnd1dig(deltapress);
            } else if (smapress != -1 && locpress != -1 && TextViewID == R.id.firststockview) {
                deltapress = locpress - smapress;
                firsttextResult = "" + rnd1dig(deltapress);
            }

            secondtextResult = "-";
            if (deltapress <= 3 && deltapress >= -3 && neuwnd >= 40) {
                secondtextResult = deg2abc(neudir) + rnd1dig(neuwnd);
                tconstants.setLastNeuOverride(new Date());
            } else if (ninetyminutestoolate(tconstants.getLastNeuOverride()) && deltapress <= 3 && deltapress >= -3) {
                secondtextResult = deg2abc(neudir) + rnd1dig(neuwnd);
            } else if (deltapress <= 0) {
                wind_max = -1;
                wind_max_idx = -1;
                for (int i = 0; i < wind_locations.length; i++) {
                    if (wind_locations_ns[i].equals("s") && wind_locations_show_marquee[i].equals("y") && (wind_dir[i] < 90 || wind_dir[i] > 270) && wind_strength[i] > wind_max) {
                        wind_max = wind_strength[i];
                        wind_max_idx = i;
                    }
                }
                if (wind_max_idx == -1) {
                    for (int i = 0; i < wind_locations.length; i++) {
                        if (wind_locations_ns[i].equals("s") && wind_locations_show_marquee[i].equals("y") && wind_strength[i] > wind_max) {
                            wind_max = wind_strength[i];
                            wind_max_idx = i;
                        }
                    }
                }
                if (wind_max_idx != -1) {
                    secondtextResult = deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
                } else {
                    secondtextResult = deg2abc(wind_dir[0]) + rnd1dig(wind_strength[0]);
                }
            } else {
                wind_max = -1;
                wind_max_idx = -1;
                for (int i = 0; i < wind_locations.length; i++) {
                    if (wind_locations_ns[i].equals("n") && wind_locations_show_marquee[i].equals("y") && wind_dir[i] >= 90 && wind_dir[i] < 270 && wind_strength[i] > wind_max) {
                        wind_max = wind_strength[i];
                        wind_max_idx = i;
                    }
                }
                if (wind_max_idx != -1) {
                    secondtextResult = deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
                } else {
                    secondtextResult = deg2abc(wind_dir[0]) + rnd1dig(wind_strength[0]);
                }
            }

            thirdtextResult = "";
            for (int i = 0; i < wind_locations.length; i++) {
                if (wind_dir[i] != -1 && wind_strength[i] != -1 && wind_locations_show_marquee[i].equals("y"))
                    thirdtextResult = thirdtextResult + wind_str[i] + " ";
            }
            thirdtextResult = thirdtextResult.trim();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            firsttextResult = "-";
        } catch (IOException e) {
            e.printStackTrace();
            firsttextResult = "-";
        } catch (Exception e) {
            e.printStackTrace();
            firsttextResult = "-";
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        Log.w("onPostExecute", "commenced with firsttextResult " + firsttextResult);

        if (!firsttextResult.equals("-") && TextViewID == R.id.firststockview) {
            cpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + firsttextResult + " hPa" + updownfunkypress(deltapress) + "</b></html>";
            views.setTextViewText(R.id.firststockview, Html.fromHtml(cpy));
            // store deltapress value in GlobalConstants
            GlobalConstants.storeSharedDouble("currentdeltapress", deltapress, cntxt);
            fortyfiveminutestoolate(deltapress);

            if (ninetyminutestoolate(tconstants.getLastNeuOverride()) && deltapress <= 3 && deltapress >= -3) {
                Log.w("ninetyminutestoolate", "write 1");
                cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Neuchâtel wind max " + "</a><b> " + secondtextResult + " km/h</b></html>";
            } else if (wind_max_idx != -1) {
                Log.w("ninetyminutestoolate", "write 2");
                cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + wind_locations[wind_max_idx] + " wind max " + "</a><b> " + secondtextResult + " km/h</b></html>";
            }

            views.setTextViewText(R.id.secondstockview, Html.fromHtml(cpy));
            views.setTextViewText(R.id.thirdstockview, Html.fromHtml(this.cntxt.getString(R.string.marquee_text, thirdtextResult)));
        }

        if (!firsttextResult.equals("-")) {
            views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
            views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);
            views.setInt(R.id.firststockview, "setTextColor", Color.WHITE);
            views.setInt(R.id.secondstockview, "setTextColor", Color.WHITE);
            views.setInt(R.id.thirdstockview, "setTextColor", Color.WHITE);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd MMM HH:mm");
            MyWidgetProvider.formattedDate = df.format(c.getTime());
            views.setTextViewText(R.id.updatetime, MyWidgetProvider.formattedDate);
            views.setInt(R.id.updatetime, "setTextColor", Color.WHITE);
            views.setTextViewText(R.id.source, "Source: MeteoSwiss");
            views.setInt(R.id.source, "setTextColor", Color.WHITE);

            GlobalConstants.storeSharedString("firststockview", firsttextResult, cntxt);
            GlobalConstants.storeSharedString("secondstockview", cpy, cntxt);
            GlobalConstants.storeSharedString("source", "source: MeteoSwiss", cntxt);
            GlobalConstants.storeSharedString("thirdstockview", thirdtextResult, cntxt);
            GlobalConstants.storeSharedString("updatetime", MyWidgetProvider.formattedDate, cntxt);
        } else {
            views.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
            views.setInt(R.id.secondstockview, "setTextColor", Color.GRAY);
            views.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
            views.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
            views.setInt(R.id.source, "setTextColor", Color.GRAY);
            views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
            views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);

            // load from prefs wrapped in GlobalConstants
            String interimresult = GlobalConstants.getSharedString("firststockview", cntxt);
            String interimcpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + interimresult + " hPa" + "</b></html>";
            views.setTextViewText(R.id.firststockview, Html.fromHtml(interimcpy));
            views.setTextViewText(R.id.secondstockview, Html.fromHtml(GlobalConstants.getSharedString("secondstockview", cntxt)));
            views.setTextViewText(R.id.thirdstockview, GlobalConstants.getSharedString("thirdstockview", cntxt));
            views.setTextViewText(R.id.source, GlobalConstants.getSharedString("source", cntxt));
            views.setTextViewText(R.id.updatetime, GlobalConstants.getSharedString("updatetime", cntxt));
            // try and reload within 1 minute
            MyWidgetProvider.formattedDate = null;
        }
        WidgetManager.updateAppWidget(WidgetID, views);
        // super.onPostExecute();
    }

    public String deg2abc(double deg) {
        String str = "";
        if (deg <= 203 && deg > 157) {
            str = " S@";
        } else if (deg > 203 && deg <= 248) {
            str = "SW@";
        } else if (deg > 248 && deg <= 292) {
            str = " W@";
        } else if (deg > 292 && deg <= 337) {
            str = "NW@";
        } else if (deg > 337 || deg <= 22) {
            str = " N@";
        } else if (deg > 22 && deg <= 68) {
            str = "NE@";
        } else if (deg > 68 && deg <= 113) {
            str = " E@";
        } else if (deg > 113 && deg <= 157) {
            str = "SE@";
        }
        return str;
    }

    public double rnd1dig(double kritz) {
        kritz = Math.round(10 * kritz);
        return kritz / 10;
    }

    public boolean ninetyminutestoolate(Date tlastneuoverride) {
        if (tlastneuoverride == null) {
            return false;
        }
        Date datenow = new Date();
        long diff = datenow.getTime() - tlastneuoverride.getTime();
        long diffMinutes = diff / (60 * 1000);
        return diffMinutes <= 90;
    }

    public void fortyfiveminutestoolate(double tdeltapress) {
        Date datenow = new Date();

        // initialize values
        if (tconstants.getLastDPOverride() == null) {
            tconstants.setLastButOneDeltaPress(tdeltapress);
            tconstants.setLastDeltaPress(tdeltapress);
            tconstants.setLastDPOverride(datenow);
        }
        long diff = datenow.getTime() - tconstants.getLastDPOverride().getTime();
        long diffMinutes = diff / (60 * 1000);
        // shift register
        if (diffMinutes >= 45) {
            tconstants.setLastButOneDeltaPress(tconstants.getLastDeltaPress());
            tconstants.setLastDeltaPress(tdeltapress);
            tconstants.setLastDPOverride(datenow);
        }
    }

    public String updownfunkypress(double tdeltapress) {
        // return zero String while nothing is initialized
        if (tconstants.getLastDPOverride() == null || tconstants.getLastDeltaPress() == -100) {
            return "";
        }
        Date datenow = new Date();
        long diff = datenow.getTime() - tconstants.getLastDPOverride().getTime();
        long diffMinutes = diff / (60 * 1000);

        // make sure relpress is older than 15 minutes
        double relpress = tconstants.getLastDeltaPress();
        if (diffMinutes <= 45) {
            relpress = tconstants.getLastButOneDeltaPress();
        }

        if (Math.abs(tdeltapress - relpress) < 0.2) {
            return "→";
        }
        if (tdeltapress - relpress > 0) {
            return "↑";
        }
        return "↓";
    }

    public boolean keeponseparated(String[] separated, int first_idx, int second_idx) {
        try {
            String str2proc = separated[first_idx].trim();
            if (str2proc.isEmpty() || str2proc.equals("-")) {
                return false;
            }
            str2proc = separated[second_idx].trim();
            if (str2proc.isEmpty() || str2proc.equals("-")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
