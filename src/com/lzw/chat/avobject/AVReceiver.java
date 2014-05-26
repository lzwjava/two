package com.lzw.chat.avobject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.avos.avoscloud.*;
import com.lzw.chat.base.App;
import com.lzw.chat.ui.ChatActivity;
import com.lzw.chat.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lzw on 14-5-25.
 */
public class AVReceiver extends BroadcastReceiver {
  public static final String _INSTALLATION = "_Installation";
  public static final String CHANNELS = "channels";
  public static String CHAT_ACTION = "com.avos.UPDATE_STATUS";
  private static String INSTALLATION_ID = "installationId";
  private static String ACTION = "action";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");

    Logger.d("got action " + action + " on channel " + channel + " with:");
    try {
      if (action.equals(AVReceiver.CHAT_ACTION)) {
        if (channel.equals(getChannel())) {
          JSONObject json = new JSONObject(intent.getExtras().
              getString("com.avos.avoscloud.Data"));
          if (ChatActivity.instance == null || ChatActivity.isPause) {
            Logger.d("start activity");
            Intent intent1 = new Intent(context, ChatActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
          } else {
            Logger.d("refresh datas");
            ChatActivity.instance.refresh();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static AVPush getAVPushByInstallationId(String installId) {
    AVQuery pushQuery = AVInstallation.getQuery();
    pushQuery.whereEqualTo(INSTALLATION_ID, installId);
    AVPush push = new AVPush();
    push.setQuery(pushQuery);
    return push;
  }

  public static JSONObject getBasicPushJson() throws JSONException {
    JSONObject data = new JSONObject();
    data.accumulate(ACTION, CHAT_ACTION);
    return data;
  }

  public static void pushNotify(Context cxt) throws AVException, JSONException {
    AVPush push = getAVPUshByChannel();
    JSONObject data = getBasicPushJson();
    push.setData(data);
    Logger.d(data.toString() + " json");
    push.send();
  }

  public static String getChannel() {
    return App.room;
  }

  private static AVPush getAVPUshByChannel() {
    AVPush push = new AVPush();
    AVQuery q = AVInstallation.getQuery();
    q.whereEqualTo(CHANNELS, getChannel());
    push.setQuery(q);
    return push;
  }

  private static AVPush getAVPushByIds(List<String> installIds) {
    AVQuery mainQ = getIdsQuery(installIds);
    AVPush push = new AVPush();
    push.setQuery(getIdsQuery(installIds));
    return push;
  }

  private static AVQuery getIdsQuery(List<String> installIds) {
    AVQuery<AVObject> mainQ = null;
    List<AVQuery<AVObject>> qs = new ArrayList<AVQuery<AVObject>>();
    for (String id : installIds) {
      AVQuery q = new AVQuery(_INSTALLATION);
      q.whereEqualTo(INSTALLATION_ID, id);
      qs.add(q);
    }
    mainQ = AVQuery.or(qs);
    return mainQ;
  }
}
