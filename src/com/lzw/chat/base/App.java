package com.lzw.chat.base;

import android.app.Application;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.lzw.chat.R;
import com.lzw.chat.avobject.Msg;

/**
 * Created by lzw on 14-5-24.
 */
public class App extends Application {
  public static int room;
  public static boolean debug = true;

  @Override
  public void onCreate() {
    super.onCreate();
    AVObject.registerSubclass(Msg.class);
    AVOSCloud.initialize(this, "0upi3x18tihc2eu8ie4ringefg0lm6bwmddb5g6xfvzmhvir",
        "ywi5z5az107oj1fxzsitz7b1kiv3x3eiqsca03qtqj7oldbo");
    room = Integer.parseInt(getResources().getString(R.string.room));
  }
}
