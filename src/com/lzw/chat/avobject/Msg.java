package com.lzw.chat.avobject;

import android.content.Context;
import com.avos.avoscloud.*;
import com.lzw.chat.base.App;
import com.lzw.chat.db.DBHelper;
import com.lzw.chat.db.DBMsg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lzw on 14-5-24.
 */
@AVClassName("Msg")
public class Msg extends AVObject {
  public static final String FROM_ID = "fromId";
  public static final String TEXT = "text";
  public static final String ROOM = "room";
  public static final String CREATED_AT = "createdAt";
  public static final String VOICE_URL = "voiceUrl";
  public static final String POST_TIME = "postTime";
  //public static final String LENGTH = "length";
  String voicePath;
  int length;
  //Date postTime;
  //String room;
  //String voiceUrl;
  //String fromId;
  //String text;

  public Msg() {
  }

  public String getVoiceUrl() {
    return getString(VOICE_URL);
  }

  public void setVoiceUrl(String voiceUrl) {
    put(VOICE_URL,voiceUrl);
  }

  public String getFromId() {
    return getString(FROM_ID);
  }

  public void setFromId(String fromId) {
    put(FROM_ID, fromId);
  }

  public String getText() {
    return getString(TEXT);
  }

  public void setText(String text) {
    put(TEXT,text);
  }

  public String getRoom() {
    return getString(ROOM);
  }

  public void setRoom(String room) {
    put(ROOM,room);
  }

  public Date getPostTime() {
    return getDate(POST_TIME);
  }

  public void setPostTime(Date postTime) {
    put(POST_TIME,postTime);
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public static List<Msg> getRoomMsgs(DBHelper dbHelper,int start) {
    int count=DBMsg.getMsgsCountByDB(dbHelper);
    try {
      AVQuery<Msg> q=AVObject.getQuery(Msg.class);
      q.setLimit(1000);
      q.whereEqualTo(ROOM, App.room);
      q.skip(count);
      q.orderByAscending(CREATED_AT);
      List<Msg> msgs ;
      msgs = q.find();
      DBMsg.insertMsgs(dbHelper,msgs);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return DBMsg.getRoomMsgsByDB(dbHelper, start);
  }

  public boolean isText() {
    return getText()!=null;
  }

  public String getVoicePath() {
    return voicePath;
  }

  public void setVoicePath(String voicePath) {
    this.voicePath = voicePath;
  }
}
