package com.lzw.chat.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import com.avos.avoscloud.*;
import com.lzw.chat.R;
import com.lzw.chat.activity.ChatActivity;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.avobject.User;
import com.lzw.chat.base.C;
import com.lzw.chat.service.ChatService;
import com.lzw.chat.utils.Logger;
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
  public static final String ALERT = "alert";
  public static String CHAT_ACTION = "com.avos.UPDATE_STATUS";
  private static String INSTALLATION_ID = "installationId";
  private static String ACTION = "action";
  public static int REPLY_NOTIFY_ID = 2;
  private static String FROM_USER = "fromUser";

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      if (intent == null) {
        return;
      }
      String action = intent.getAction();
      if (action != null && action.equals(AVReceiver.CHAT_ACTION)) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
          return;
        }
        JSONObject json = new JSONObject(extras.
            getString("com.avos.avoscloud.Data"));
        String fromUser = json.getString(FROM_USER);
        String msgId = json.getString(C.MSG_ID);
        if (ChatActivity.instance == null || ChatActivity.isPause) {
          openChat(context);
          notify(context, json.getString(ALERT));
        } else {
          ChatService.addMsgById(msgId);
        }
      } else if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
        openChat(context);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void openChat(Context cxt) {
    Logger.d("start activity");
    Intent intent1 = new Intent(cxt, ChatActivity.class);
    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    cxt.startActivity(intent1);
  }

  public void notify(Context context, String msg) throws JSONException {
    int icon = context.getApplicationInfo().icon;
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        new Intent(context, ChatActivity.class), 0);
    Resources res = context.getResources();
    Notification.Builder builder = new Notification.Builder(context);
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(msg)
        .setContentTitle(res.getString(R.string.newReply))
        .setContentText(msg)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(REPLY_NOTIFY_ID, builder.getNotification());
  }

  public static void cancelNotify(Context cxt) {
    cancelNotification(cxt, REPLY_NOTIFY_ID);
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

  public static void pushNotify(Context cxt, AVUser pairedUser, Msg msg) throws AVException, JSONException {
    AVPush push = getAVPushByInstallationId(User.getInstallationId(pairedUser));
    JSONObject data = getBasicPushJson();
    data.accumulate(FROM_USER, pairedUser.getUsername());
    data.accumulate(C.MSG_ID, msg.getObjectId());
    push.setData(data);
    Logger.d(data.toString() + " json");
    push.send();
  }

  public static String getChannel(String pairedUser) {
    String myName = User.getMyName();
    if (pairedUser.compareTo(myName) <= 0) {
      return pairedUser + "_" + myName;
    } else {
      return myName + "_" + pairedUser;
    }
  }

  private static AVPush getAVPUshByChannel(String pairedUser) {
    AVPush push = new AVPush();
    AVQuery q = AVInstallation.getQuery();
    q.whereEqualTo(CHANNELS, getChannel(pairedUser));
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
