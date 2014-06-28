package com.lzw.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

import java.util.Date;

/**
 * Created by lzw on 14-5-24.
 */
@AVClassName("Msg")
public class Msg extends AVObject {
  public static final String FROM_USER = "fromUser";
  public static final String TEXT = "text";
  public static final String CREATED_AT = "createdAt";
  public static final String VOICE_URL = "voiceUrl";
  public static final String POST_TIME = "postTime";
  public static final String TO_USER = "toUser";
  public static final String LENGTH = "length";
  //public static final String LENGTH = "length";
  String voicePath;
  //int length;
  //Date postTime;
  //String room;
  //String voiceUrl;
  //String fromId;
  //String text;
  //String toUser;

  public Msg() {
  }

  public String getVoiceUrl() {
    return getString(VOICE_URL);
  }

  public void setVoiceUrl(String voiceUrl) {
    put(VOICE_URL, voiceUrl);
  }

  public String getFromUser() {
    return getString(FROM_USER);
  }

  public void setFromUser(String fromUser) {
    put(FROM_USER, fromUser);
  }

  public String getToUser() {
    return getString(TO_USER);
  }

  public void setToUser(String toUser) {
    put(TO_USER, toUser);
  }

  public String getText() {
    return getString(TEXT);
  }

  public void setText(String text) {
    put(TEXT, text);
  }

  public Date getPostTime() {
    return getDate(POST_TIME);
  }

  public void setPostTime(Date postTime) {
    put(POST_TIME, postTime);
  }

  public int getLength() {
    return getInt(LENGTH);
  }

  public void setLength(int length) {
    put(LENGTH, length);
  }

  public boolean isText() {
    return getText() != null;
  }

  public String getVoicePath() {
    return voicePath;
  }

  public void setVoicePath(String voicePath) {
    this.voicePath = voicePath;
  }
}
