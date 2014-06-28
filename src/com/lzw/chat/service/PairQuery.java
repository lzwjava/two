package com.lzw.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVPush;
import com.lzw.chat.avobject.User;
import com.lzw.chat.utils.AVUtils;
import com.lzw.commons.HttpUtils;
import com.lzw.commons.Logger;
import com.lzw.commons.NetAsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lzw on 14-5-22.
 */
public class PairQuery {
  public static final String PAIR_ACTION = "com.avos.pair";
  public static final String STATUS = "status";
  public static final String WAIT = "wait";
  public static final String PAIRED = "paired";
  public static final String PAIRED_USER = "pairedUser";
  public static final String TYPE = "type";
  public static final String CANCEL = "cancel";
  public static final String WEB_URL = "http://114.215.107.217:8080/english/pair?";
  public static final String USER = "user";
  private static String ACTION = "action";
  PairCallBack pairCallBack;
  Context cxt;

  public PairQuery(Context cxt) {
    this.cxt = cxt;
  }

  BroadcastReceiver recevier = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      try {
        if (action != null && action.equals(PAIR_ACTION)) {
          JSONObject json = AVUtils.getJsonByAVIntent(intent);
          String pairedUser = json.getString(PAIRED_USER);
          pairCallBack.done(pairedUser, null);
          finishQuery();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  private void initPair() {
    IntentFilter intentFilter = new IntentFilter(PAIR_ACTION);
    cxt.registerReceiver(recevier, intentFilter);
  }

  public void findInBackgroud(PairCallBack pairCallBack) {
    this.pairCallBack = pairCallBack;
    initPair();
    new AsyncTask<Void, Void, Void>() {
      String pairedUser;
      Exception e;
      String status = null;

      @Override
      protected Void doInBackground(Void... voids) {
        try {
          String json = HttpUtils.httpGetEntityStr(WEB_URL, "user", User.getMyName(),
              "type", "pair");
          Logger.d("json=" + json);
          JSONObject jobj = new JSONObject(json);
          status = jobj.getString(STATUS);
          if (status.equals(WAIT)) {
          } else if (status.equals(PAIRED)) {
            pairedUser = jobj.getString(PAIRED_USER);
            pushToPairedNotifyOk(pairedUser);
          }
        } catch (Exception e1) {
          e1.printStackTrace();
          e = e1;
        }
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (status == null) {
          PairQuery.this.pairCallBack.done(null, e);
          finishQuery();
        } else if (status.equals(PAIRED)) {
          PairQuery.this.pairCallBack.done(pairedUser, e);
          finishQuery();
        }
      }
    }.execute();
  }

  private void pushToPairedNotifyOk(String username) throws JSONException, AVException {
    AVPush push = AVUtils.getAVPushByUsername(username);
    JSONObject json = getBasicPushJson();
    json.put(PAIRED_USER, User.getMyName());
    Logger.d("push json" + json);
    push.setData(json);
    push.send();
  }

  public static JSONObject getBasicPushJson() throws JSONException {
    JSONObject data = new JSONObject();
    data.accumulate(ACTION, PAIR_ACTION);
    return data;
  }

  public void cancel() {
    new NetAsyncTask(cxt, false) {
      boolean cancelSucceed;

      @Override
      protected void doInBack() throws Exception {
        String res = HttpUtils.httpGetEntityStr(WEB_URL, USER, User.getMyName(),
            TYPE, CANCEL);
        if (res.equals("cancelSucceed")) {
          cancelSucceed = true;
        } else {
          cancelSucceed = false;
        }
      }

      @Override
      protected void onPost(boolean res) {
        if (res) {
          if (cancelSucceed) {
            finishQuery();
            Logger.d("cancel succeed");
          }
        }
      }
    }.execute();
  }

  public interface PairCallBack {
    void done(String pairedUser, Exception e);
  }

  public void finishQuery() {
    try {
      cxt.unregisterReceiver(recevier);
    } catch (Exception e) {
      Log.e("lzw", "receiver", e);
      e.printStackTrace();
    }
  }
}
