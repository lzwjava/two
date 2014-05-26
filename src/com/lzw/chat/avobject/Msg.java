package com.lzw.chat.avobject;

import com.avos.avoscloud.*;
import com.lzw.chat.base.App;

import java.util.List;

/**
 * Created by lzw on 14-5-24.
 */
@AVClassName("Msg")
public class Msg extends AVObject {
  public static final String VOICE = "voice";
  public static final String FROM_ID = "fromId";
  public static final String TEXT = "text";
  public static final String ROOM = "room";
  public static final String CREATED_AT = "createdAt";
  //public static final String LENGTH = "length";
  String voicePath;
  int length;
  //String room;
  //AVFile voice;
  //String fromId;
  //String text;

  public Msg() {
  }

  public AVFile getVoice() {
    return getAVFile(VOICE);
  }

  public void setVoice(AVFile voice) {
    put(VOICE, voice);
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

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public static List<Msg> getRoomMsgs(int start) throws AVException {
    AVQuery q=AVObject.getQuery(Msg.class);
    q.setLimit(1000);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.whereEqualTo(ROOM, App.room);
    q.skip(start);
    q.orderByAscending(CREATED_AT);
    return q.find();
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
