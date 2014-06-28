package com.lzw.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.avos.avoscloud.AVClassName;

/**
 * Created by lzw on 14-5-28.
 */
@Deprecated
public class DBHelper extends SQLiteOpenHelper {

  public DBHelper(Context context, String name,int version) {
    super(context, name, null, version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    DBMsg.createTable(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
