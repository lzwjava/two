package com.lzw.chat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.lzw.chat.avobject.Msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
public class DBMsg {
  public static final String MSG = "Msg";

  public static final String TXT = "txt";
  public static final String OBJECT_ID = "objectId";
  public static final String FROM_ID = "fromId";
  public static final String VOICE_URL = "voiceUrl";
  public static final String ROOM = "room";
  public static final String POST_TIME = "postTime";

  public static void createTable(SQLiteDatabase db) {
    db.execSQL("create table if not exists Msg (objectId text primary key," +
        "fromId text,room text,txt text, voiceUrl text," +
        "postTime int)");
  }

  public static List<Msg> getRoomMsgsByDB(DBHelper dbHelper, int start) {
    List<Msg> msgs = new ArrayList<Msg>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c = db.query(MSG, new String[]{OBJECT_ID, FROM_ID,
        POST_TIME, VOICE_URL,TXT,ROOM},
        null, null, null, null, POST_TIME, start + ",100000");
    while (c.moveToNext()) {
      Msg msg = new Msg();
      msg.setObjectId(c.getString(c.getColumnIndex(OBJECT_ID)));
      msg.setVoiceUrl(c.getString(c.getColumnIndex(VOICE_URL)));
      msg.setFromId(c.getString(c.getColumnIndex(FROM_ID)));
      msg.setRoom(c.getString(c.getColumnIndex(ROOM)));
      msg.setText(c.getString(c.getColumnIndex(TXT)));
      long time = c.getLong(c.getColumnIndex(POST_TIME));
      Date date = new Date(time);
      msg.setPostTime(date);
      msgs.add(msg);
    }
    c.close();
    return msgs;
  }

  public static int insertMsgs(DBHelper dbHelper, List<Msg> msgs) {
    if (msgs == null || msgs.size() == 0) {
      return 0;
    }
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.beginTransaction();
    int n=0;
    try {
      for (Msg msg : msgs) {
        ContentValues cv = new ContentValues();
        cv.put(FROM_ID, msg.getFromId());
        cv.put(OBJECT_ID, msg.getObjectId());
        cv.put(VOICE_URL, msg.getVoiceUrl());
        Date postTime = msg.getPostTime();
        if(postTime==null){
          postTime=new Date();
        }
        cv.put(POST_TIME, postTime.getTime());
        cv.put(ROOM, msg.getRoom());
        cv.put(TXT, msg.getText());
        db.insert(MSG, null, cv);
        n++;
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return n;
  }

  public static int getMsgsCountByDB(DBHelper dbHelper) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c= db.rawQuery("select count(*) from Msg",null);
    int count=0;
    if(c.moveToNext()){
      count= c.getInt(0);
    }
    c.close();
    return count;
  }


}
