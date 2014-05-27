package com.lzw.chat.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.lzw.chat.R;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.avobject.UpdateInfo;
import com.lzw.chat.util.Logger;

import java.io.*;

/**
 * Created by lzw on 14-5-24.
 */
public class App extends Application {
  public static final String IS_FIRST_INSTALL = "isFirstInstall";
  public static final String ROOM = "room";
  public static String room;
  public static boolean debug = false;


  @Override
  public void onCreate() {
    super.onCreate();
    fixAsyncTaskBug();
    AVObject.registerSubclass(Msg.class);
    AVObject.registerSubclass(UpdateInfo.class);
    AVOSCloud.initialize(this, "0upi3x18tihc2eu8ie4ringefg0lm6bwmddb5g6xfvzmhvir",
        "ywi5z5az107oj1fxzsitz7b1kiv3x3eiqsca03qtqj7oldbo");
    initRoomInfo();
    if(!debug){
      AVAnalytics.enableCrashReport(this, true);
    }
  }

  public void initRoomInfo() {
    SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
    boolean isFirstInstall = pref.getBoolean("isFirstInstall", true);
    if(isFirstInstall){
      InputStream in= getResources().openRawResource(R.raw.room);
      InputStreamReader reader=new InputStreamReader(in);
      BufferedReader bf=new BufferedReader(reader);
      try {
        String s = bf.readLine().trim();
        Logger.d(s);
        pref.edit().putString(ROOM,s).commit();
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      pref.edit().putBoolean(IS_FIRST_INSTALL,false).commit();
      initRoomInfo();
    }else{
      App.room= pref.getString(ROOM,"2");
    }
  }


  public void fixAsyncTaskBug() {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        return null;
      }
    }.execute();
  }
}
