package com.lzw.chat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.lzw.chat.avobject.Msg;
import com.lzw.commons.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
@Deprecated
public class DBMsg {
  public static final String MSG = "Msg";

  public static final String TXT = "txt";
  public static final String OBJECT_ID = "objectId";
  public static final String FROM_USER = "fromUser";
  public static final String VOICE_URL = "voiceUrl";
  public static final String POST_TIME = "postTime";
  public static final String TO_USER = "toUser";

  public static void createTable(SQLiteDatabase db) {
    db.execSQL("create table if not exists Msg (objectId text primary key," +
        "fromUser text,toUser text,room text,txt text, voiceUrl text," +
        "postTime int)");
  }

  public static List<Msg> getRoomMsgsByDB(DBHelper dbHelper, int start, String myName,
                                          String herName) {
    List<Msg> msgs = new ArrayList<Msg>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c = db.query(MSG, new String[]{OBJECT_ID, FROM_USER,
        POST_TIME, VOICE_URL, TXT, TO_USER},
        "fromUser=? and toUser=? or fromUser=? and toUser=?",
        new String[]{Utils.quote(myName), Utils.quote(herName),
            Utils.quote(herName), Utils.quote(myName)}, null, null, POST_TIME, start + "," +
        "100000");
    while (c.moveToNext()) {
      Msg msg = new Msg();
      msg.setObjectId(c.getString(c.getColumnIndex(OBJECT_ID)));
      msg.setVoiceUrl(c.getString(c.getColumnIndex(VOICE_URL)));
      msg.setFromUser(c.getString(c.getColumnIndex(FROM_USER)));
      msg.setToUser(c.getString(c.getColumnIndex(TO_USER)));
      msg.setText(c.getString(c.getColumnIndex(TXT)));
      long time = c.getLong(c.getColumnIndex(POST_TIME));
      Date date = new Date(time);
      msg.setPostTime(date);
      msgs.add(msg);
    }
    c.close();
    return msgs;
  }

  public static int insertMsg(DBHelper dbHelper, Msg msg) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    return insertMsgs(dbHelper, msgs);
  }

  public static int insertMsgs(DBHelper dbHelper, List<Msg> msgs) {
    if (msgs == null || msgs.size() == 0) {
      return 0;
    }
    int n = 0;
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    for (Msg msg : msgs) {
      if (existsMsg(dbHelper, msg.getObjectId()) == false) {
        ContentValues cv = new ContentValues();
        cv.put(FROM_USER, msg.getFromUser());
        cv.put(OBJECT_ID, msg.getObjectId());
        cv.put(VOICE_URL, msg.getVoiceUrl());
        Date postTime = msg.getPostTime();
        if (postTime == null) {
          postTime = new Date();
        }
        cv.put(POST_TIME, postTime.getTime());
        cv.put(FROM_USER, msg.getFromUser());
        cv.put(TO_USER, msg.getToUser());
        cv.put(TXT, msg.getText());
        db.insert(MSG, null, cv);
        n++;
      }
    }
    return n;
  }

  private static boolean existsMsg(DBHelper dbHelper, String objectId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor cursor = db.query(MSG, new String[]{OBJECT_ID}, OBJECT_ID + "=?", new String[]{objectId}, null, null
        , null);
    int cnt = cursor.getCount();
    if (cnt > 0) {
      return true;
    } else {
      return false;
    }
  }

  public static int getMsgsCountByDB(DBHelper dbHelper, String myName, String herName) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c = db.rawQuery("select count(*) from Msg where toUser=? and fromUser=?" +
        " or fromUser=? and toUser=?", new String[]{Utils.quote(myName),
        Utils.quote(herName), Utils.quote(herName), Utils.quote(myName)});
    int count = 0;
    if (c.moveToNext()) {
      count = c.getInt(0);
    }
    c.close();
    return count;
  }
}
