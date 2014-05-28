package com.lzw.chat.mgr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.lzw.chat.R;
import com.lzw.chat.avobject.UpdateInfo;

/**
 * Created by lzw on 14-3-25.
 */
public class UpdateTask extends AsyncTask<Void, Void, UpdateInfo> {
  public static final String VERSION = "version";
  public static final String PACKAGE_NAME = "com.lzw.chat";
  private Activity context;
  public static String DOWNLOAD_APK = "downloadApk";
  int curVersion;
  CallBack callBack;
  private String UPDATE_INFO_ID = "5384e215e4b089312c485d85";
  boolean taskRes;

  public UpdateTask(Activity context, CallBack callBack) {
    this.context = context;
    this.callBack = callBack;
  }

  public static void update(Context context, UpdateInfo info) {
    systemDownloadUrl(context, info.getApkUrl());
  }

  public static void systemDownloadUrl(Context context, String url) {
    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    Uri uri = Uri.parse(url);
    DownloadManager.Request request = new DownloadManager.Request(uri);
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
        | DownloadManager.Request.NETWORK_WIFI);
    request.setTitle(context.getString(R.string.two));
    request.setVisibleInDownloadsUi(false);
    long id = manager.enqueue(request);
    SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putLong(DOWNLOAD_APK, id);
    editor.commit();
  }

  public static int getVersionCode(Context context) {
    int versionCode = 0;
    try {
      versionCode = context.getPackageManager().getPackageInfo(
          PACKAGE_NAME, 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return versionCode;
  }

  public static String getVersionName(Context context) {
    String versionName = null;
    try {
      versionName = context.getPackageManager().getPackageInfo(
          PACKAGE_NAME, 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return versionName;
  }

  public static void runUpdateTask(final Activity cxt) {
    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(cxt);
    new UpdateTask(cxt, new CallBack() {
      @Override
      public void done(final UpdateInfo info) {
        int ver = info.getVersion();
        int curVer = getVersionCode(cxt);
        if (curVer < ver) {
          long lastPromptTime = pref.getLong("lastPromptTime", 0);
          final long now = System.currentTimeMillis();
          if (now - lastPromptTime > 1000 * 60 * 60 * 24) {
            AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
            builder.setTitle(R.string.haveNewVersion)
                .setPositiveButton(R.string.installNew, new DialogInterface.OnClickListener() {

                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    UpdateTask.update(cxt, info);
                  }
                })
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    pref.edit().putLong("lastPromptTime",now).commit();
                  }
                }).show();
          }
        } else if (curVer == ver) {
          int lastVer = pref.getInt(VERSION, 0);
          if (lastVer < curVer) {
            pref.edit().putInt(VERSION, curVer).commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
            builder.setMessage(Html.fromHtml(info.getDescription()))
                .setPositiveButton(cxt.getString(R.string.ok), null)
                .setTitle(cxt.getString(R.string.update_title)
                    + " " + getVersionName(cxt))
                .show();
          }
        }
      }
    }).execute();
  }

  @Override
  protected void onPreExecute() {
    // TODO Auto-generated method stub
    super.onPreExecute();
    curVersion = getVersionCode(context);
  }

  @Override
  protected UpdateInfo doInBackground(Void... params) {
    // TODO Auto-generated method stub
    UpdateInfo info = null;
    try {
      AVQuery<UpdateInfo> query = AVObject.getQuery(UpdateInfo.class);
      info = query.get(UPDATE_INFO_ID);
      taskRes=true;
    } catch (AVException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      taskRes = false;
    }
    return info;
  }

  @Override
  protected void onPostExecute(final UpdateInfo info) {
    // TODO Auto-generated method stub
    if(taskRes){
      if (info == null) {
        throw new NullPointerException("update apk info is null");
      }
      callBack.done(info);
    }
    super.onPostExecute(info);
  }

  public interface CallBack {
    void done(UpdateInfo info);
  }
}
