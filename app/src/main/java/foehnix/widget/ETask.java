package foehnix.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

 import	javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ETask {

    private final RemoteViews views;
    int WidgetID;
    private String firsttextResult = "-";
    private String secondtextResult = "";
    private String thirdtextResult = "";
    private String cpy = "";
    private GlobalConstants tconstants;
    @SuppressLint("StaticFieldLeak")
    private Context cntxt;
    private AppWidgetManager WidgetManager;
    private int TextViewID;

    private double deltapress;

    private double neuwnd = -1;

    private double wind_max = -1;
    private int wind_max_idx = -1;

    private String[] wind_locations;
    private String[] wind_locations_ns;
    private String[] wind_locations_short;
    private String[] wind_locations_show_marquee;

    ExecutorService executor;
    // Handler handler;


    public ETask(Context cntxt, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID, String sUrl) {
        this.cntxt = cntxt;
        this.views = views;
        this.WidgetID = appWidgetID;
        this.WidgetManager = appWidgetManager;
        this.TextViewID = textViewID;

        this.tconstants = new GlobalConstants(cntxt);
        this.wind_locations = this.cntxt.getResources().getStringArray(R.array.wind_locations);
        this.wind_locations_ns = this.cntxt.getResources().getStringArray(R.array.wind_locations_ns);
        this.wind_locations_short = this.cntxt.getResources().getStringArray(R.array.wind_locations_short);
        this.wind_locations_show_marquee = this.cntxt.getResources().getStringArray(R.array.wind_locations_show_marquee);

        this.executor = Executors.newCachedThreadPool();

        Callable<Object> getDataTask = () -> {
            getData(sUrl);
            return "getDataTask commplete";
        };

        try {
            List<Callable<Object>> callables = new ArrayList<>();
            callables.add(getDataTask);
            List<Future<Object>> futures = executor.invokeAll(callables, 5000L, TimeUnit.MILLISECONDS);
            for (int i = 0; i < futures.size(); i++) {
                Future<Object> future = futures.get(i);
                // Getting result
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showData();

        executor.shutdown();
        try {
            if (!executor.awaitTermination(800L, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private BufferedReader getBufferedReader(URL textUrl) throws java.net.SocketTimeoutException, IOException {
        System.out.println("start of getBufferedReader()");

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
        System.setProperty("sun.net.client.defaultReadTimeout", "5000");
        InputStream resultingInputStream;
        TrafficStats.setThreadStatsTag(42);
        HttpsURLConnection urlConnection = (HttpsURLConnection)  textUrl.openConnection();
        // new Thread(new InterruptThread(urlConnection, this.views)).start();
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);
        urlConnection.setDoOutput(false);
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        // TrafficStats.setThreadStatsTag(42);


        System.out.println("before urlConnection.connect(), timeout => " + urlConnection.getConnectTimeout());
        urlConnection.connect();
        System.out.println("after urlConnection.connect()");
        String encoding = urlConnection.getContentEncoding();
        System.out.println("encoding in getBufferedReader => " + encoding);
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            resultingInputStream = new GZIPInputStream(urlConnection.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            resultingInputStream = new InflaterInputStream(urlConnection.getInputStream(), new Inflater(true));
        } else {
            resultingInputStream = urlConnection.getInputStream();
        }

        return new BufferedReader(new InputStreamReader(resultingInputStream));
        // return new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
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

    void getData(String sUrl) {
        URL textUrl;
        double klopress = -1;
        double locpress = -1;
        double lugpress = -1;
        double neudir = -1;
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

        BufferedReader bufferReader = null;
        try {
            textUrl = new URL(sUrl);
            bufferReader = getBufferedReader(textUrl);
            System.out.println("after getBufferedReader in getData");
            String StringBuffer;
            long ctm0 = System.currentTimeMillis();
            while ((StringBuffer = bufferReader.readLine()) != null) {
                long ctm1 = System.currentTimeMillis();
                if((ctm1 -ctm0)>5000) {
                    System.out.println("IOException thrown in getData");
                    throw new IOException();
                }
                for (int i = 0; i < wind_locations.length; i++) {
                    if (StringBuffer.length() > 3 && StringBuffer.substring(0, 3).equals(wind_locations_short[i])) {
                        String[] separated = StringBuffer.split(";");
                        if (separated.length < WINDDIR_IDX)
                            separated = StringBuffer.split(","); // just in case they switch from ; to ,
                        String str2proc = separated[PRESS_IDX].trim();
                        if (!str2proc.isEmpty() && !str2proc.equals("-"))
                            pressures[i] = Double.parseDouble(str2proc);
                        if (StringBuffer.startsWith("KLO")) klopress = pressures[i];
                        if (StringBuffer.startsWith("LUG")) lugpress = pressures[i];
                        if (StringBuffer.startsWith("OTL")) locpress = pressures[i];
                        if (StringBuffer.startsWith("SMA")) smapress = pressures[i];

                        if (checkEmpty(separated, WINDDIR_IDX, WINDSPD_IDX)) {
                            wind_dir[i] = Double.parseDouble(separated[WINDDIR_IDX]);
                            wind_strength[i] = Double.parseDouble(separated[WINDSPD_IDX]);
                            wind_str[i] = wind_locations[i] + " " + deg2abc(wind_dir[i]).trim() + wind_strength[i];
                            if (StringBuffer.startsWith("NEU")) {
                                neudir = wind_dir[i];
                                neuwnd = wind_strength[i];
                            }
                        }
                    }
                }
            }
            bufferReader.close();

            GlobalConstants.setWindLocationsShowMarquee(wind_locations_show_marquee, cntxt);
            GlobalConstants.setWindStr(wind_str, cntxt);

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
                    // determine the most windy location in the south
                    for (int i = 0; i < wind_locations.length; i++) {
                        if (wind_locations_ns[i].equals("s") && wind_locations_show_marquee[i].equals("y") && wind_strength[i] > wind_max) {
                            wind_max = wind_strength[i];
                            wind_max_idx = i;
                        }
                    }
                    secondtextResult = deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
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
                    // determine the most windy location in the north
                    for (int i = 0; i < wind_locations.length; i++) {
                        if (wind_locations_ns[i].equals("n") && wind_locations_show_marquee[i].equals("y") && wind_strength[i] > wind_max) {
                            wind_max = wind_strength[i];
                            wind_max_idx = i;
                        }
                    }
                    secondtextResult = deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
                }
            }

            thirdtextResult = "";
            for (int i = 0; i < wind_locations.length; i++) {
                if (wind_dir[i] != -1 && wind_strength[i] != -1 && wind_locations_show_marquee[i].equals("y"))
                    thirdtextResult = thirdtextResult + wind_str[i] + " ";
            }
            thirdtextResult = thirdtextResult.trim();

        } catch(java.net.UnknownHostException e) {
            Log.w("getData", e.toString());
            firsttextResult = "-";
        } catch (MalformedURLException e) {
            Log.w("getData", e.toString());
            firsttextResult = "-";
        } catch(java.net.SocketTimeoutException e) {
            Log.w("getData", e.toString());
            System.out.println("SocketTimeoutExeception in getData()");
            firsttextResult = "-";
        } catch (IOException e) {
            Log.w("getData", e.toString());
            firsttextResult = "-";
        } catch (Exception e) {
            Log.w("getData", e.toString());
            firsttextResult = "-";
        } finally {
                try {
                    if (bufferReader != null) {
                        bufferReader.close();
                    }
                } catch(IOException ignored) {}
        }
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

    protected void showData() {
        Log.w("showData", "commenced with firsttextResult " + firsttextResult);

        if (!firsttextResult.equals("-") && TextViewID == R.id.firststockview) {
            // cancel alarm set up during boot
            Util.cancelAlarm(this.cntxt, Util.getAlarmIntent(this.cntxt));

            cpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + firsttextResult + " hPa" + goesDownUp(deltapress) + "</b></html>";
            views.setTextViewText(R.id.firststockview, Util.fromHtml(cpy));
            // store deltapress value in GlobalConstants
            GlobalConstants.storeSharedDouble("currentdeltapress", deltapress, cntxt);
            fortyfiveminutestoolate(deltapress);

            if (deltapress <= 3 && deltapress >= -3 && neuwnd >= 40) {
                // Log.w("ninetyminutestoolate", "write 1");
                cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Neuchâtel wind max " + "</a><b> " + secondtextResult + " km/h</b></html>";
            } else if (ninetyminutestoolate(tconstants.getLastNeuOverride()) && deltapress <= 3 && deltapress >= -3) {
                cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Neuchâtel wind max " + "</a><b> " + secondtextResult + " km/h</b></html>";
            } else if (wind_max_idx != -1) {
                // Log.w("ninetyminutestoolate", "write 2 , wind_max_idx: " + wind_max_idx);
                cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + wind_locations[wind_max_idx] + " wind max " + "</a><b> " + secondtextResult + " km/h</b></html>";
            }
            views.setTextViewText(R.id.maxwindview, Util.fromHtml(cpy));

            views.setTextViewText(R.id.thirdstockview, Util.fromHtml(this.cntxt.getString(R.string.marquee_text, thirdtextResult)));
        }

        if (!firsttextResult.equals("-")) {
            views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
            views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);
            views.setInt(R.id.firststockview, "setTextColor", Color.WHITE);
            views.setInt(R.id.maxwindview, "setTextColor", Color.WHITE);
            views.setInt(R.id.thirdstockview, "setTextColor", Color.WHITE);

            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd MMM HH:mm");
            MyWidgetProvider.formattedDate = df.format(c.getTime());
            views.setTextViewText(R.id.updatetime, MyWidgetProvider.formattedDate);
            views.setInt(R.id.updatetime, "setTextColor", Color.WHITE);
            views.setTextViewText(R.id.source, "Source: MeteoSwiss");
            views.setInt(R.id.source, "setTextColor", Color.WHITE);

            StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
            try {
                GlobalConstants.storeSharedString("firststockview", firsttextResult, cntxt);
                GlobalConstants.storeSharedString("maxwindview", cpy, cntxt);
                GlobalConstants.storeSharedString("source", "source: MeteoSwiss", cntxt);
                GlobalConstants.storeSharedString("thirdstockview", thirdtextResult, cntxt);
                GlobalConstants.storeSharedString("updatetime", MyWidgetProvider.formattedDate, cntxt);
            } finally {
                StrictMode.setThreadPolicy(oldPolicy);
            }

        } else {
            views.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
            views.setInt(R.id.maxwindview, "setTextColor", Color.GRAY);
            views.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
            views.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
            views.setInt(R.id.source, "setTextColor", Color.GRAY);
            views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
            views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);

            // load from prefs wrapped in GlobalConstants
            String interimResult;
            StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
            try {
                interimResult = GlobalConstants.getSharedString("firststockview", cntxt);
            } finally {
                StrictMode.setThreadPolicy(oldPolicy);
            }

            String interimCpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + interimResult + " hPa" + "</b></html>";
            views.setTextViewText(R.id.firststockview, Util.fromHtml(interimCpy));
            views.setTextViewText(R.id.maxwindview, Util.fromHtml(GlobalConstants.getSharedString("maxwindview", cntxt)));
            views.setTextViewText(R.id.thirdstockview, GlobalConstants.getSharedString("thirdstockview", cntxt));
            views.setTextViewText(R.id.source, GlobalConstants.getSharedString("source", cntxt));
            views.setTextViewText(R.id.updatetime, GlobalConstants.getSharedString("updatetime", cntxt));
            // try and reload within 1 minute
            MyWidgetProvider.formattedDate = null;
        }
        WidgetManager.updateAppWidget(WidgetID, views);
        // super.onPostExecute();
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

    public String goesDownUp(double tdeltapress) {
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

    public boolean checkEmpty(String[] separated, int first_idx, int second_idx) {
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
            Log.w("checkEmpty",e.toString());
            return false;
        }
        return true;
    }

    public double rnd1dig(double kritz) {
        kritz = Math.round(10 * kritz);
        return kritz / 10;
    }


    private static class InterruptThread implements Runnable {

        HttpsURLConnection con;

        public InterruptThread(URLConnection con, RemoteViews mViews) {
            this.con = (HttpsURLConnection) con;
        }

        public void run() {
            try {
                Thread.sleep(5000); // or Thread.sleep(con.getConnectTimeout())
            } catch (InterruptedException e) {

            }
            con.disconnect();
            System.out.println("Timer thread forcing to quit connection");
        }
    }


}
