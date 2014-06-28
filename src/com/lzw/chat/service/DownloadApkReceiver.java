package com.lzw.chat.service;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class DownloadApkReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    String action = intent.getAction();
    if (action!=null && action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
      SharedPreferences preferences = context.getSharedPreferences("data",
          Context.MODE_PRIVATE);
      long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
      long myId = preferences.getLong(UpdateTask.DOWNLOAD_APK, 0);
      if (id != 0 && id == myId) {
        Query query = new Query();
        query.setFilterById(id);
        DownloadManager manager = (DownloadManager) context
            .getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = manager.query(query);
        if(cursor==null){
          return;
        }
        int columnCount = cursor.getColumnCount();
        String path = null;
        String type = null;
        while (cursor.moveToNext()) {
          for (int j = 0; j < columnCount; j++) {
            String columnName = cursor.getColumnName(j);
            String string = cursor.getString(j);
            if (columnName.equals(DownloadManager.COLUMN_LOCAL_FILENAME)) {
              path = string;
            }
            if (columnName.equals(DownloadManager.COLUMN_MEDIA_TYPE)) {
              type = string;
            }
          }
        }
        cursor.close();

        Intent intent1 = new Intent();
        intent1.setAction(Intent.ACTION_VIEW);
        File file = new File(path);
        boolean b = file.renameTo(new File(file.getParentFile(), "chat1" + ".apk"));
        if (b) {
          Log.i("lzw", file.getAbsolutePath());
          intent1.setDataAndType(Uri.fromFile(file), type);
          intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent1);
        }
      }
    }
  }
}
