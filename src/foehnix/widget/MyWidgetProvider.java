package foehnix.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
public static final String ACTION_MUELL = "Muell";
static String formattedDate;

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
	  Log.w(this.getClass().getName(),"commenced onDeleted");
	  Intent intent = new Intent("ContactWidgetUpdate");
	  PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	  AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	  alarmManager.cancel(sender);
	  intent = new Intent("Muell");
	  sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	  alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	  alarmManager.cancel(sender);
	  super.onDeleted(context, appWidgetIds);
  }

  @Override
  public void onDisabled(Context context) {
	Log.w(this.getClass().getName(),"commenced onDisabled");
	Intent intent = new Intent("ContactWidgetUpdate");
	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
 	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 	alarmManager.cancel(sender);
 	super.onDisabled(context);
  }

  @Override
  public void onEnabled(Context context) {
  		Log.w("AlarmManager","run enablement proc");
 		PendingIntent anIntent=PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmMgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
	    super.onEnabled(context);	        
  }

  public void onReceive(Context context, Intent intent) {
      Log.w(this.getClass().getName(), "onReceive: intent="+intent);
      
      if("Muell".equalsIgnoreCase(intent.getAction())){
    	  Log.w("Muell","trash");
    	  if(formattedDate==null) {
    	      ComponentName componentName=new ComponentName(context,getClass().getName());
              AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
              int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
              onUpdate(context,appWidgetManager,appWidgetIds);	  
    	  }
      }
      
      if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")||"android.appwidget.action.BOOT_COMPLETED".equalsIgnoreCase(intent.getAction())){
	        PendingIntent anIntent=PendingIntent.getBroadcast(context, 0, new Intent("ContactWidgetUpdate"), PendingIntent.FLAG_UPDATE_CURRENT);
	  		AlarmManager alarmMgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	  		alarmMgr.cancel(anIntent);
	  		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, anIntent);
	  		super.onEnabled(context);	
      }
      
      if("ContactWidgetUpdate".equalsIgnoreCase(intent.getAction())||"android.appwidget.action.APPWIDGET_UPDATE".equalsIgnoreCase(intent.getAction())){
    	  Log.w(this.getClass().getName(), "commenced ContactWidgetUpdate");
          ComponentName componentName=new ComponentName(context,getClass().getName());
          AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
          int[] appWidgetIds=appWidgetManager.getAppWidgetIds(componentName);
          onUpdate(context,appWidgetManager,appWidgetIds);
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
          
          String url = "http://windundwetter.ch/Stations/filter/abo,alt,chu,cim,loc/show/time,wind,windarrow,qff";
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          i.setData(Uri.parse(url));
          context.startActivity(i);
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
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
	Log.w("onUpdate","commenced");
	if(isnite()==true&&formattedDate!=null) {
		return;
	}
	RemoteViews remoteViews;  
    // Get all ids
    ComponentName thisWidget = new ComponentName(context,
        MyWidgetProvider.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
    for (int widgetId : allWidgetIds) {
      remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
      remoteViews.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh_gray);
	  remoteViews.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
	  remoteViews.setInt(R.id.secondstockview, "setTextColor", Color.GRAY);
	  remoteViews.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
	  remoteViews.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
      String surl = new String();
      surl = new String("http://data.geo.admin.ch.s3.amazonaws.com/ch.meteoschweiz.swissmetnet/VQHA69.csv");
      new dspclass(context, remoteViews, widgetId, appWidgetManager, R.id.firststockview).execute(surl);

      // Register an onClickListener
      Intent intent = new Intent(context, MyWidgetProvider.class);
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      remoteViews.setOnClickPendingIntent(R.id.updatebutton, pendingIntent);   
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
      // Register another onClickListener
      remoteViews.setOnClickPendingIntent(R.id.firststockview, getPendingSelfIntent(context, "firststockviewclicked"));
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
      // Register yet another onClickListener
      remoteViews.setOnClickPendingIntent(R.id.secondstockview, getPendingSelfIntent(context, "secondstockviewclicked"));
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
  }
   
 private class dspclass extends AsyncTask<String, Void, Void>{
      
      private String firsttextResult = new String();
      private String secondtextResult = new String();
      private String thirdtextResult = new String();
      private String cpy = new String();
      
      private Context cntxt;
      private RemoteViews views;
      private int WidgetID;
      private AppWidgetManager WidgetManager;
      private int TextViewID;
      
      private double deltapress;

      public dspclass (Context cntxt, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID) {
  	     this.cntxt = cntxt;
    	 this.views = views;
  	     this.WidgetID = appWidgetID;
  	     this.WidgetManager = appWidgetManager;  
  	     this.TextViewID = textViewID;
       }
      
      public dspclass (RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, int textViewID) {
 	     this.views = views;
 	     this.WidgetID = appWidgetID;
 	     this.WidgetManager = appWidgetManager;  
 	     this.TextViewID = textViewID;
      }
      
      public dspclass (RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager) {
    	     this.views = views;
    	     this.WidgetID = appWidgetID;
    	     this.WidgetManager = appWidgetManager;  
      }
    
      public dspclass(RemoteViews views){
          this.views = views;
      }
      
      @Override
      protected Void doInBackground(String...murls) {
     
    	  URL textUrl;
    	  String lugstr= "";
    	  String locstr="";
          String klostr="";
          String altstr="";
          String cimstr="";
          String abostr="";
          String chustr="";
          String smastr="";
          double lugpress=-1;
          double klopress=-1;
          double locpress=-1;
          double smapress=-1;
          double altwnd=-1;
          double altdir=-1;
          double cimwnd=-1;
          double cimdir=-1;
          double locwnd=-1;
          double locdir=-1;
          double abownd=-1;
          double abodir=-1;
          double chuwnd=-1;
          double chudir=-1;
          
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
           BufferedReader bufferReader 
            = new BufferedReader(new InputStreamReader(resultingInputStream));
           Log.w("bufferReader", "created bufferReader");
           String StringBuffer;
           while ((StringBuffer = bufferReader.readLine()) != null) {
    		   if(StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("LUG")) {
    			   lugstr = StringBuffer;
    			   String[] separated = lugstr.split("\\|");
    			   int l = separated.length;
    			   if(separated[l-1].trim().length()>0&&!separated[l-1].trim().equals("-")) {
	    			   lugpress = Double.parseDouble(separated[l-1]);
	    			   Log.w("LUGANO", "pressure " + lugpress);
    			   }
    		   } else if(StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("KLO")) {
    			   klostr = StringBuffer;
    			   String[] separated = klostr.split("\\|");
    			   int l = separated.length;
    			   if(separated[l-1].trim().length()>0&&!separated[l-1].trim().equals("-")) {
	    			   klopress = Double.parseDouble(separated[l-1]);
	    			   Log.w("KLOTEN", "pressure " + klopress);
	    		   }
    		   } else if(StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("SMA")) {
    			   smastr = StringBuffer;
    			   String[] separated = smastr.split("\\|");
    			   int l = separated.length;
    			   if(separated[l-1].trim().length()>0&&!separated[l-1].trim().equals("-")) {
	    			   smapress = Double.parseDouble(separated[l-1]);
	    			   Log.w("FLUNTERN", "pressure " + smapress);
	    		   }
    		   } else if (StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("ALT")) {
    			   altstr = StringBuffer;
    			   String[] separated = altstr.split("\\|");
    			   if(keeponseparated(separated)) {
    			   altdir = Double.parseDouble(separated[5]);
    			   altwnd = Double.parseDouble(separated[8]);
    			   altstr = "Altdorf " + deg2abc(altdir).trim() + altwnd;
    			   Log.w("ALTDORF", "wind " + altwnd);
    			   Log.w("ALTDORF", "wind dir " + altdir);
    			   }
    		   } else if (StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("CIM")) {
    			   cimstr = StringBuffer;
    			   String[] separated = cimstr.split("\\|");
    			   if(keeponseparated(separated)) {
    			   cimdir = Double.parseDouble(separated[5]);
    			   cimwnd = Double.parseDouble(separated[8]);
    			   cimstr = "Cimetta " + deg2abc(cimdir).trim() + cimwnd;
    			   Log.w("CIMETTA", "wind " + cimwnd);
    			   Log.w("CIMETTA", "wind dir " + cimdir);
    			   }
    		   } else if (StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("OTL")) {
    			   locstr = StringBuffer;
    			   String[] separated = locstr.split("\\|");
    			   int l = separated.length;
    			   if(separated[l-1].trim().length()>0&&!separated[l-1].trim().equals("-")) {
	    			   locpress = Double.parseDouble(separated[l-1]);
	    			   Log.w("LOCARNO", "pressure " + klopress);
	    		   }
    			   if(keeponseparated(separated)) {
    			   locdir = Double.parseDouble(separated[5]);
    			   locwnd = Double.parseDouble(separated[8]);
    			   locstr = "Locarno " + deg2abc(locdir).trim() + locwnd;
    			   Log.w("Locarno", "wind " + locwnd);
    			   Log.w("Locarno", "wind dir " + locdir);
    			   }
    		   } else if (StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("ABO")) {
    			   abostr = StringBuffer;
    			   String[] separated = abostr.split("\\|");
    			   if(keeponseparated(separated)) {
    			   abodir = Double.parseDouble(separated[5]);
    			   abownd = Double.parseDouble(separated[8]);
    			   abostr = "Adelboden " + deg2abc(abodir).trim() + abownd;
    			   Log.w("Adelboden", "wind " + abownd);
    			   Log.w("Adelboden", "wind dir " + abodir);
    			   }
    		   } else if (StringBuffer.length() > 3 && StringBuffer.substring(0,3).equals("CHU")) {
    			   chustr = StringBuffer;
    			   String[] separated = chustr.split("\\|");
    			   if(keeponseparated(separated)) {
    			   chudir = Double.parseDouble(separated[5]);
    			   chuwnd = Double.parseDouble(separated[8]);
    			   chustr = "Chur " + deg2abc(chudir).trim() + chuwnd;
    			   }
    		   }
           }
           firsttextResult="-";
           secondtextResult="-";
           thirdtextResult="-";
           deltapress=-100;
           DecimalFormat df = new DecimalFormat("0.0");
           if(klopress!=-1&&lugpress!=-1&&TextViewID==R.id.firststockview) {
        	   deltapress = lugpress - klopress;
        	   firsttextResult = df.format(deltapress);
           } else if (smapress!=-1&&lugpress!=-1&&TextViewID==R.id.firststockview) {
        	   deltapress = lugpress - smapress;
        	   firsttextResult = df.format(deltapress);
           } else if (klopress!=-1&&locpress!=-1&&TextViewID==R.id.firststockview) {
        	   deltapress = locpress - klopress;
        	   firsttextResult = df.format(deltapress);
           } else if (smapress!=-1&&locpress!=-1&&TextViewID==R.id.firststockview) {
        	   deltapress = locpress - smapress;
        	   firsttextResult = df.format(deltapress);
           }
           if (deltapress >= 0 && altwnd!=-1) {
        	   secondtextResult=deg2abc(altdir);
        	   secondtextResult = secondtextResult + df.format(altwnd);
           }  else if(locwnd!=-1) {
        	   secondtextResult=deg2abc(locdir);
        	   secondtextResult = secondtextResult + df.format(locwnd);
           }
           thirdtextResult = "";
           if(abodir!=-1&&abownd!=-1) {
        	   thirdtextResult = thirdtextResult + abostr + " ";
           }
           if(altdir!=-1&&altwnd!=-1) {
        	   thirdtextResult = thirdtextResult + altstr + " ";
           }
           if(chudir!=-1&&chuwnd!=-1) {
        	   thirdtextResult = thirdtextResult + chustr + " ";
           }
           if(cimdir!=-1&&cimwnd!=-1) {
        	   thirdtextResult = thirdtextResult + cimstr + " ";
           }
           if(locdir!=-1&&locwnd!=-1) {
        	   thirdtextResult = thirdtextResult + locstr;
           }
           bufferReader.close();
           Log.w("bufferReader", "closed bufferedreader");
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
    	   cpy = "<html><a href=\"http://www.meteocentrale.ch/en/weather/foehn-and-bise/foehn.html\">" + "Δp Lugano-Kloten " + "</a><b> " + firsttextResult + " hPa</b></html>";
    	   views.setTextViewText(R.id.firststockview, Html.fromHtml(cpy));
    	   if(deltapress >= 0) {
    		   cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Altdorf wind max "  + "</a><b> " + secondtextResult + " km/h</b></html>";
    	   } else {
    		   cpy = "<html><a href=\"http://windundwetter.ch/Stations/filter/alt/show/time,wind,windarrow,qff\">" + "Locarno wind max "  + "</a><b> " + secondtextResult + " km/h</b></html>";
    	   }
    	   views.setTextViewText(R.id.secondstockview, Html.fromHtml(cpy));
    	   views.setTextViewText(R.id.thirdstockview, thirdtextResult);
       } 
       
       if(!firsttextResult.equals("-")) {
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
       } else if(firsttextResult.equals("-")) {
    	   views.setInt(R.id.firststockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.secondstockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.thirdstockview, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.updatetime, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.source, "setTextColor", Color.GRAY);
    	   views.setInt(R.id.updatebutton, "setBackgroundResource", R.drawable.refresh);
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
  }
 
 public boolean keeponseparated(String[] separated) {
	 try {
		if(separated[8].trim().length()==0||separated[8].trim().equals("-")) {
			return(false);
		}
		if(separated[5].trim().length()==0||separated[5].trim().equals("-")) {
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