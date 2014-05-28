package com.lzw.chat.avobject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import com.avos.avoscloud.*;
import com.lzw.chat.base.App;
import com.lzw.chat.ui.ChatActivity;
import com.lzw.chat.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.lzw.chat.R;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lzw on 14-5-25.
 */
public class AVReceiver extends BroadcastReceiver {
  public static final String _INSTALLATION = "_Installation";
  public static final String CHANNELS = "channels";
  public static final String ALERT = "alert";
  public static String CHAT_ACTION = "com.avos.UPDATE_STATUS";
  private static String INSTALLATION_ID = "installationId";
  private static String ACTION = "action";
  public static int REPLY_NOTIFY_ID=2;

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if(intent==null){
        return;
      }
      String action = intent.getAction();
      if (action!=null && action.equals(AVReceiver.CHAT_ACTION)) {
        Bundle extras = intent.getExtras();
        if(extras ==null){
          return;
        }
        String channel = extras.getString("com.avos.avoscloud.Channel");
        Logger.d("got action " + action + " on channel " + channel + " with:");
        if (channel!=null && channel.equals(getChannel())) {
          JSONObject json = new JSONObject(extras.
              getString("com.avos.avoscloud.Data"));
          if (ChatActivity.instance == null || ChatActivity.isPause) {
            Logger.d("start activity");
            Intent intent1 = new Intent(context, ChatActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
            notify(context,json);
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

  public void notify(Context context, JSONObject json) throws JSONException {
    int icon=context.getApplicationInfo().icon;
    PendingIntent pend=PendingIntent.getActivity(context,0,
        new Intent(context,ChatActivity.class),0);
    Resources res=context.getResources();
    Notification.Builder builder=new Notification.Builder(context);
    String msg=json.getString(ALERT);
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(msg)
        .setContentTitle(res.getString(R.string.newReply))
        .setContentText(msg)
        .setAutoCancel(true);
    NotificationManager man= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(REPLY_NOTIFY_ID,builder.getNotification());
  }

  public static void cancelNotify(Context cxt){
    cancelNotification(cxt,REPLY_NOTIFY_ID);
  }

  public static void cancelNotification(Context ctx, int notifyId) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
    nMgr.cancel(notifyId);
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

  public static void pushNotify(Context cxt, String pushMsg) throws AVException, JSONException {
    AVPush push = getAVPUshByChannel();
    JSONObject data = getBasicPushJson();
    data.accumulate(ALERT,pushMsg);
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
