package com.lzw.chat.utils;

import android.content.Context;
import android.content.Intent;
import com.avos.avoscloud.*;
import com.avos.avoscloud.feedback.FeedbackAgent;
import com.lzw.chat.avobject.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lzw on 14-6-24.
 */
public class AVUtils {
  public static void syncFeedBack(Context cxt) {
    FeedbackAgent agent = new FeedbackAgent(cxt);
    agent.sync();
  }

  public static void startFeedBackActivity(Context cxt) {
    FeedbackAgent agent = new FeedbackAgent(cxt);
    agent.startDefaultThreadActivity();
  }

  public static JSONObject getJsonByAVIntent(Intent intent) throws JSONException {
    return new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
  }

  public static AVPush getAVPushByUser(AVUser to) {
    String installId = User.getInstallationId(to);
    AVPush push = getAVPushByInstallationId(installId);
    return push;
  }

  public static AVPush getAVPushByUsername(String to) throws AVException {
    return getAVPushByUser(User.getAVUser(to));
  }

  public static AVPush getAVPushByInstallationId(String installId) {
    AVQuery pushQuery = AVInstallation.getQuery();
    pushQuery.whereEqualTo(User.INSTALLATION_ID, installId);
    AVPush push = new AVPush();
    push.setQuery(pushQuery);
    return push;
  }
}
