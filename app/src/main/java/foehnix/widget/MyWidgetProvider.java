package foehnix.widget;
// todo: thirdstockview marquee via html
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import foehnix.widget.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import foehnix.widget.ScrollTextView;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	public static final String ACTION_TRSH = "TRSH";
	static String formattedDate;
	static int disablenite=0;
	private GlobalConstants tconstants;

  public double rnd1dig(double kritz) {
	  kritz = Math.round(10*kritz);
	  return(kritz/10);
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
	  Log.w(this.getClass().getName(),"commenced onDeleted");
	  Intent intent = new Intent("ContactWidgetUpdate");
	  PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	  AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	  alarmManager.cancel(sender);
	  intent = new Intent("TRSH");
	  sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	  alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	  alarmManager.cancel(sender);
	  // ensure the widget will restart even after a delete event at night
	  disablenite=0;
	  
	  super.onDeleted(context, appWidgetIds);
  }

  @Override
  public void onDisabled(Context context) {
	Log.w(this.getClass().getName(),"commenced onDisabled");
	Intent intent = new Intent("ContactWidgetUpdate");
	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
 	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 	alarmManager.cancel(sender);
	// ensure the widget will restart even after a disable event at night
	disablenite=0;
    
	super.onDisabled(context);
  }

  @Override
  public void onEnabled(Context context) {
  		Log.w("AlarmManager","run enablement proc");
 		PendingIntent anIntent=PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmMgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
	    this.tconstants = new GlobalConstants(context);
	    super.onEnabled(context);
  }

  public void onReceive(Context context, Intent intent) {
      Log.w(this.getClass().getName(), "onReceive: intent="+intent);
      
      if("TRSH".equalsIgnoreCase(intent.getAction())){
    	  Log.w("TRSH","trash");
    	  if(formattedDate==null) {
    	      ComponentName componentName=new ComponentName(context,getClass().getName());
              AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
              int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
              onUpdate(context,appWidgetManager,appWidgetIds);
    	  }
      }
      
      if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")||"android.appwidget.action.BOOT_COMPLETED".equalsIgnoreCase(intent.getAction())){
	        //ensure the widget will start even after a reboot at nite
    	    disablenite=0;
    	    
    	    PendingIntent anIntent=PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
	  		AlarmManager alarmMgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	  		alarmMgr.cancel(anIntent);
	  		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
	  		super.onEnabled(context);
      }

      // act on update button pressed
      if("android.appwidget.action.APPWIDGET_UPDATE".equalsIgnoreCase(intent.getAction())){
    	  Log.w(this.getClass().getName(), "commenced APPWIDGET_UPDATE");
		  Log.w(this.getClass().getName(), intent.getAction());
		  ComponentName componentName=new ComponentName(context,getClass().getName());
		  AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
		  int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
		  onUpdate(context,appWidgetManager,appWidgetIds);
      }

      // act on alarm manager
	  if("ContactWidgetUpdate".equalsIgnoreCase(intent.getAction())){
		  Log.w(this.getClass().getName(), "commenced ContactWidgetUpdate");
		  Log.w(this.getClass().getName(), intent.getAction());
		  if(isnite()==false||formattedDate==null) {
			  ComponentName componentName=new ComponentName(context,getClass().getName());
			  AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
			  int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
			  onUpdate(context,appWidgetManager,appWidgetIds);
		  }
	  }
      
      if("firststockviewclicked".equalsIgnoreCase(intent.getAction())){
          Log.w("firststockview","clicked");
          
          String url = "http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html";
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          i.setData(Uri.parse(url));
          context.startActivity(i);
      }
      
      if("secondstockviewclicked".equalsIgnoreCase(intent.getAction())){
          Log.w("secondstockview","clicked");
          
          String url = "http://windundwetter.ch/Stations/filter/abo,altd,chu,cim,loc,meir,neu/show/time,wind,windarrow,qff";
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          i.setData(Uri.parse(url));
          context.startActivity(i);
      }
      
      if("shareactionclicked".equalsIgnoreCase(intent.getAction())){
          Log.w("share action","clicked");
          
          // register an intent for sharing
      	  Intent i=new Intent(android.content.Intent.ACTION_SEND);
      	  i.setType("text/plain");
      	  i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      	  i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		  double currentdeltapress=GlobalConstants.getSharedDouble("currentdeltapress",context);
      	  if(currentdeltapress!=-100) {
	      	  // Add data to the intent, the receiving app will decide what to do with it.
      		  String txtmsg = "Δp Lugano-Kloten " + rnd1dig(currentdeltapress) + " hPa\nwind gusts [km/h]:";
			  txtmsg = txtmsg + "\n" + GlobalConstants.produceTexto();
      		  i.putExtra(Intent.EXTRA_SUBJECT, "foehnix brief");
	      	  i.putExtra(Intent.EXTRA_TEXT, txtmsg);
	          context.startActivity(i);
      	  }
      }
      
      if("android.appwidget.action.APPWIDGET_DISABLED".equalsIgnoreCase(intent.getAction())){
          onDisabled(context);
      }
      
      if("android.appwidget.action.APPWIDGET_DELETED".equalsIgnoreCase(intent.getAction())){
    	  ComponentName componentName=new ComponentName(context,getClass().getName());
          AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
          int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
    	  onDeleted(context, appWidgetIds);
      }
      
      if(intent.getAction().toLowerCase().indexOf("enabled")>0) {  
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
	  if(this.tconstants==null) {
		    Log.w("onUpdate","enabling tconstants");
			this.tconstants=new GlobalConstants(context);
	  }
      new dspclass(context, remoteViews, this.tconstants, widgetId, appWidgetManager, R.id.firststockview).execute(surl);
      
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
   
 private class dspclass extends AsyncTask<String, Void, Void> {
      private String firsttextResult = new String();
      private String secondtextResult = new String();
      private String thirdtextResult = new String();
      private String cpy = new String();

	  private GlobalConstants tconstants;
      
      private Context cntxt;
      private RemoteViews views; int WidgetID;
      private AppWidgetManager WidgetManager;
      private int TextViewID;
      
      private double deltapress;

	 private double wind_max=-1;
	 private int wind_max_idx=-1;

	 private String[] wind_locations;
	 private String[] wind_locations_ns;
	 private String[] wind_locations_short;
	 private String[] wind_locations_show_marquee;

      public dspclass (Context cntxt, RemoteViews views, GlobalConstants cnstnts, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID) {
		this.cntxt = cntxt;
		this.views = views;
		this.WidgetID = appWidgetID;
		this.WidgetManager = appWidgetManager;
		this.TextViewID = textViewID;
		this.tconstants=cnstnts;

		wind_locations=this.cntxt.getResources().getStringArray(R.array.wind_locations);
		wind_locations_ns=this.cntxt.getResources().getStringArray(R.array.wind_locations_ns);
		wind_locations_short=this.cntxt.getResources().getStringArray(R.array.wind_locations_short);
		wind_locations_show_marquee=this.cntxt.getResources().getStringArray(R.array.wind_locations_show_marquee);
       }

      public dspclass (RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID) {
		this.views = views;
		this.WidgetID = appWidgetID;
		this.WidgetManager = appWidgetManager;
		this.TextViewID = textViewID;

		wind_locations=this.cntxt.getResources().getStringArray(R.array.wind_locations);
		wind_locations_ns=this.cntxt.getResources().getStringArray(R.array.wind_locations_ns);
		wind_locations_short=this.cntxt.getResources().getStringArray(R.array.wind_locations_short);
		wind_locations_show_marquee=this.cntxt.getResources().getStringArray(R.array.wind_locations_show_marquee);
      }
      
      public dspclass (RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager) {
    	     this.views=views;
    	     this.WidgetID=appWidgetID;
    	     this.WidgetManager=appWidgetManager;
      }
    
      public dspclass(RemoteViews views){
          this.views = views;
      }

      @Override
      protected Void doInBackground(String...murls) {
    	  URL textUrl;
          double klopress=-1;
          double locpress=-1;
          double lugpress=-1;
          double neudir=-1;
          double neuwnd=-1;
          double smapress=-1;
		  int PRESS_IDX=this.cntxt.getResources().getInteger(R.integer.PRESS_IDX);
		  int WINDDIR_IDX=this.cntxt.getResources().getInteger(R.integer.WINDDIR_IDX);
		  int WINDSPD_IDX=this.cntxt.getResources().getInteger(R.integer.WINDSPD_IDX);

		  double[] pressures=new double[wind_locations.length];
		  double[] wind_dir=new double[wind_locations.length];
		  double[] wind_strength=new double[wind_locations.length];
		  String[] wind_str=new String[wind_locations.length];
		  for(int i=0;i<wind_locations.length;i++) {
			  wind_dir[i]=-1;
			  wind_strength[i]=-1;
			  pressures[i]=-1;
		  }

          try {
           cpy=murls[0];
           textUrl = new URL(murls[0]);
           URLConnection urlConnection = textUrl.openConnection();
           urlConnection.setConnectTimeout(5000);
           urlConnection.setReadTimeout(5000);
           urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
           urlConnection.connect();
           InputStream resultingInputStream = null;
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
			   for(int i=0;i<wind_locations.length;i++) {
				   if(StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals(wind_locations_short[i])) {
					   String[] separated = StringBuffer.split(";");
					   if(separated.length<WINDDIR_IDX) separated = StringBuffer.split(","); // just in case they switch from ; to ,
					   int l = separated.length;
					   String str2proc=separated[PRESS_IDX].trim();
					   if(str2proc.length()>0&&!str2proc.equals("-")) pressures[i] = Double.parseDouble(str2proc);
					   if(StringBuffer.substring(0,3).equals("KLO")) klopress=pressures[i];
					   if(StringBuffer.substring(0,3).equals("LUG")) lugpress=pressures[i];
					   if(StringBuffer.substring(0,3).equals("OTL")) locpress=pressures[i];
					   if(StringBuffer.substring(0,3).equals("SMA")) smapress=pressures[i];

					   if(keeponseparated(separated,WINDDIR_IDX, WINDSPD_IDX)) {
						   wind_dir[i] = Double.parseDouble(separated[WINDDIR_IDX]);
						   wind_strength[i] = Double.parseDouble(separated[WINDSPD_IDX]);
						   wind_str[i] = wind_locations[i] + " " + deg2abc(wind_dir[i]).trim() + wind_strength[i];
						   if(StringBuffer.substring(0,3).equals("NEU")) neuwnd=wind_strength[i];
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

		  firsttextResult="-";
		  deltapress=-100;
		  if(klopress!=-1&&lugpress!=-1&&TextViewID==R.id.firststockview) {
			  deltapress = lugpress - klopress;
			  firsttextResult = "" + rnd1dig(deltapress);
		  } else if (smapress!=-1&&lugpress!=-1&&TextViewID==R.id.firststockview) {
			  deltapress = lugpress - smapress;
			  firsttextResult = "" + rnd1dig(deltapress);
		  } else if (klopress!=-1&&locpress!=-1&&TextViewID==R.id.firststockview) {
			  deltapress = locpress - klopress;
			  firsttextResult = "" + rnd1dig(deltapress);
		  } else if (smapress!=-1&&locpress!=-1&&TextViewID==R.id.firststockview) {
			  deltapress = locpress - smapress;
			  firsttextResult = "" + rnd1dig(deltapress);
		  }

		  secondtextResult="-";
		  if (deltapress <= 3 && deltapress >=-3 && neuwnd>=40) {
			  secondtextResult=deg2abc(neudir) + rnd1dig(neuwnd);
			  tconstants.setLastNeuOverride(new Date());
		  } else if (ninetyminutestoolate(tconstants.getLastNeuOverride()) && deltapress <= 3 && deltapress >=-3) {
			  secondtextResult = deg2abc(neudir) + rnd1dig(neuwnd);
		  } else if (deltapress <= 0){
			  wind_max=-1;
			  wind_max_idx=-1;
			  for(int i=0;i<wind_locations.length;i++) {
				  if(wind_locations_ns[i].equals("s")&&wind_locations_show_marquee[i].equals("y")&&(wind_dir[i]<90||wind_dir[i]>270)&&wind_strength[i]>wind_max) {
					  wind_max=wind_strength[i];
					  wind_max_idx=i;
				  }
			  }
			  if(wind_max_idx==-1) {
                  for(int i = 0; i < wind_locations.length; i++) {
                      if (wind_locations_ns[i].equals("s")&&wind_locations_show_marquee[i].equals("y")&&wind_strength[i]>wind_max) {
                          wind_max = wind_strength[i];
                          wind_max_idx = i;
                      }
                  }
              }
			  if(wind_max_idx!=-1) {
				  secondtextResult=deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
			  } else {
				  secondtextResult=deg2abc(wind_dir[0]) + rnd1dig(wind_strength[0]);
			  }
		  } else {
			  wind_max=-1;
			  wind_max_idx=-1;
			  for(int i=0;i<wind_locations.length;i++) {
				  if(wind_locations_ns[i].equals("n")&&wind_locations_show_marquee[i].equals("y")&&wind_dir[i]>=90&&wind_dir[i]<270&&wind_strength[i]>wind_max) {
					  wind_max=wind_strength[i];
					  wind_max_idx=i;
				  }
			  }
			  if(wind_max_idx!=-1) {
				  secondtextResult=deg2abc(wind_dir[wind_max_idx]) + rnd1dig(wind_strength[wind_max_idx]);
			  } else {
				  secondtextResult=deg2abc(wind_dir[0]) + rnd1dig(wind_strength[0]);
			  }
		  }

		   thirdtextResult = "";
		   for(int i=0;i<wind_locations.length;i++) {
			   if(wind_dir[i]!=-1&&wind_strength[i]!=-1&&wind_locations_show_marquee[i].equals("y")) thirdtextResult = thirdtextResult + wind_str[i] + " ";
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
       
       if(!firsttextResult.equals("-")&&TextViewID==R.id.firststockview) {
    	   cpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + firsttextResult + " hPa" + updownfunkypress(deltapress) + "</b></html>";
    	   views.setTextViewText(R.id.firststockview, Html.fromHtml(cpy));
    	   // store deltapress value in GlobalConstants
		   GlobalConstants.storeSharedDouble("currentdeltapress",deltapress,cntxt);
           fortyfiveminutestoolate(deltapress);
    	   
           if (ninetyminutestoolate(tconstants.getLastNeuOverride()) && deltapress <= 3 && deltapress >=-3) {
			   Log.w("ninetyminutestoolate", "write 1");
        	   cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Neuchâtel wind max "  + "</a><b> " + secondtextResult + " km/h</b></html>";
		   } else if (wind_max_idx!=-1) {
			   Log.w("ninetyminutestoolate", "write 2");
			   cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + wind_locations[wind_max_idx] + " wind max "  + "</a><b> " + secondtextResult + " km/h</b></html>";
           }
    	   
    	   views.setTextViewText(R.id.secondstockview, Html.fromHtml(cpy));
		   views.setTextViewText(R.id.thirdstockview, Html.fromHtml(this.cntxt.getString(R.string.marquee_text, thirdtextResult)));
       } 
       
       if(!firsttextResult.equals("-")) {
    	   views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
    	   views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);
    	   views.setInt(R.id.firststockview, "setTextColor", Color.WHITE);
    	   views.setInt(R.id.secondstockview, "setTextColor", Color.WHITE);
    	   views.setInt(R.id.thirdstockview, "setTextColor", Color.WHITE);
    	   
    	   Calendar c = Calendar.getInstance();
    	   SimpleDateFormat df = new SimpleDateFormat("dd MMM HH:mm");
           MyWidgetProvider.formattedDate = df.format(c.getTime());
           views.setTextViewText(R.id.updatetime, formattedDate);
    	   views.setInt(R.id.updatetime, "setTextColor", Color.WHITE);
    	   views.setTextViewText(R.id.source,"Source: MeteoSwiss");
    	   views.setInt(R.id.source, "setTextColor", Color.WHITE);

		   GlobalConstants.storeSharedString("firststockview",firsttextResult,cntxt);
		   GlobalConstants.storeSharedString("secondstockview",cpy,cntxt);
		   GlobalConstants.storeSharedString("source","source: MeteoSwiss",cntxt);
		   GlobalConstants.storeSharedString("thirdstockview",thirdtextResult,cntxt);
		   GlobalConstants.storeSharedString("updatetime",formattedDate,cntxt);
       } else if(firsttextResult.equals("-")) {
    	   views.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.secondstockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.source, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.sharebutton, "setBackgroundResource", R.drawable.share_icon_white);
    	   views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);
    	   
    	   // load from prefs wrapped in GlobalConstants
		   String interimresult = GlobalConstants.getSharedString("firststockview",cntxt);
    	   String interimcpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + interimresult + " hPa" + "</b></html>";
    	   views.setTextViewText(R.id.firststockview, Html.fromHtml(interimcpy));
    	   views.setTextViewText(R.id.secondstockview, Html.fromHtml(GlobalConstants.getSharedString("secondstockview",cntxt)));
    	   views.setTextViewText(R.id.thirdstockview, GlobalConstants.getSharedString("thirdstockview",cntxt));
    	   views.setTextViewText(R.id.source, GlobalConstants.getSharedString("source",cntxt));
    	   views.setTextViewText(R.id.updatetime, GlobalConstants.getSharedString("updatetime",cntxt));
    	   // try and reload within 1 minute
    	   MyWidgetProvider.formattedDate = null;
       }
       WidgetManager.updateAppWidget(WidgetID, views);
       // super.onPostExecute();   
      }
      
      public String deg2abc(double deg) {
    	  String str = new String();
    	   if(deg<=203&&deg>157) {
	   		   str = " S@";
	   	   } else if(deg>203&&deg<=248) {
	   		   str = "SW@";
	   	   } else if(deg>248&&deg<=292) {
	   		   str = " W@";
	   	   } else if(deg>292&&deg<=337) {
	   		   str = "NW@";
	   	   } else if(deg>337||deg<=22) {
	   		   str = " N@";
	   	   } else if(deg>22&&deg<=68) {
	   		   str = "NE@";
	   	   } else if(deg>68&&deg<=113) {
	   		   str = " E@";
	   	   } else if(deg>113&&deg<=157) {
	   		   str = "SE@";
	   	   }
    	  return(str);
      }
      
      public double rnd1dig(double kritz) {
    	  kritz = Math.round(10*kritz);
    	  return(kritz/10);
      }
      
      public boolean ninetyminutestoolate(Date tlastneuoverride) {
    	  Date datenow;
    	  long diff;
    	  long diffMinutes;
    	  if(tlastneuoverride==null) {
    		  return(false);
    	  }
    	  datenow = new Date();
    	  diff=datenow.getTime()-tlastneuoverride.getTime();
    	  diffMinutes = diff / (60 * 1000);
    	  if(diffMinutes <= 90) {
    		  return(true);
    	  } else {
    		  return(false);
    	  }
      }
      
  	public void fortyfiveminutestoolate(double tdeltapress) {
		  Date datenow;
		  long diff;
		  long diffMinutes;
		  datenow = new Date();
		  
		  // initialize values
		  if(tconstants.getLastDPOverride()==null) {
			  tconstants.setLastButOneDeltaPress(tdeltapress);
			  tconstants.setLastDeltaPress(tdeltapress);
			  tconstants.setLastDPOverride(datenow);
		  }
		  diff=datenow.getTime()- tconstants.getLastDPOverride().getTime();
		  diffMinutes = diff / (60 * 1000);
		  // shift register
		  if(diffMinutes >= 45) {
			  tconstants.setLastButOneDeltaPress(tconstants.getLastDeltaPress());
			  tconstants.setLastDeltaPress(tdeltapress);
			  tconstants.setLastDPOverride(datenow);
		  }
	}

	public String updownfunkypress(double tdeltapress) {
		  Date datenow;
		  long diff;
		  long diffMinutes;
		  double relpress;
		  // return zero String while nothing is initialized
		  if(tconstants.getLastDPOverride()==null||tconstants.getLastDeltaPress()==-100) {
			  return("");
		  }
		  datenow = new Date();
		  diff=datenow.getTime()-tconstants.getLastDPOverride().getTime();
		  diffMinutes = diff / (60 * 1000);
		  
		  // make sure relpress is older than 15 minutes
		  relpress=tconstants.getLastDeltaPress();
		  if(diffMinutes <= 45) {
			  relpress=tconstants.getLastButOneDeltaPress();
		  }
		  
		  if(Math.abs(tdeltapress-relpress)<0.2) { 
			  return("→");
		  } else if((tdeltapress-relpress)>0) { 
			  return("↑");
		  } else {
			  return("↓");
		  }
	}
  }
 
 public boolean keeponseparated(String[] separated, int first_idx, int second_idx) {
	 try {
		 String str2proc=separated[first_idx].trim();
		 if(str2proc.length()==0||str2proc.equals("-")) {
			return(false);
		 }
		 str2proc=separated[second_idx].trim();
		 if(str2proc.length()==0||str2proc.equals("-")) {
			 return(false);
		 }
	 } catch (Exception e) {
         e.printStackTrace();
         return(false);
	 }
	 return(true);
 }
 
 public boolean isnite() {
	  String hhdate;
	  String mmdate;
	  int hh;
	  int mm;
	  Calendar c = Calendar.getInstance();
	  SimpleDateFormat df = new SimpleDateFormat("mm");
	  if(disablenite<5) {
		  disablenite++;
		  return(false);
	  }
      mmdate = df.format(c.getTime());
      mm=Integer.valueOf(mmdate);
      if(mm>-1&&mm<17) {
    	  return(false);
      }
	  df = new SimpleDateFormat("HH");
      hhdate = df.format(c.getTime());
	  hh=Integer.valueOf(hhdate);
	  if(hh>21||hh<7) {
		  return(true);
	  } else {
		  return(false);
	  }
  }
		
	protected PendingIntent getPendingSelfIntent(Context context, String action) {
	     Intent intent = new Intent(context, getClass());
	     intent.setAction(action);
	     return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
}