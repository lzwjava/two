package com.lzw.chat.base;

import android.app.Application;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.lzw.chat.R;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.util.Logger;

import java.io.*;

/**
 * Created by lzw on 14-5-24.
 */
public class App extends Application {
  public static String room;
  public static boolean debug = true;

  @Override
  public void onCreate() {
    super.onCreate();
    AVObject.registerSubclass(Msg.class);
    AVOSCloud.initialize(this, "0upi3x18tihc2eu8ie4ringefg0lm6bwmddb5g6xfvzmhvir",
        "ywi5z5az107oj1fxzsitz7b1kiv3x3eiqsca03qtqj7oldbo");
    InputStream in= getResources().openRawResource(R.raw.room);
    InputStreamReader reader=new InputStreamReader(in);
    BufferedReader bf=new BufferedReader(reader);
    try {
      String s = bf.readLine().trim();
      Logger.d(s);
      room =s;
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
