package com.lzw.chat.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lzw on 14-6-19.
 */
public class PrefDao {
  public static final String SYSTEM_ID = "systemId";
  public static final String SYSTEM_PWD = "systemPwd";
  Context cxt;
  SharedPreferences pref;
  SharedPreferences.Editor editor;

  public PrefDao(Context cxt) {
    this.cxt = cxt;
    pref = PreferenceManager.getDefaultSharedPreferences(cxt);
    editor = pref.edit();
  }

  public boolean getBooleanInitFalse(String name) {
    return pref.getBoolean(name, false);
  }

  public void setBoolean(String name, boolean value) {
    editor.putBoolean(name, value).commit();
  }


  public String getSystemId() {
    return getStringInitEmpty(SYSTEM_ID);
  }

  public void setSystemId(String systemId) {
    putString(SYSTEM_ID, systemId);
  }

  public String getSystemPwd() {
    return getStringInitEmpty(SYSTEM_PWD);
  }

  public void setSystemPwd(String systemPwd) {
    putString(SYSTEM_PWD, systemPwd);
  }

  private void putString(String name, String value) {
    editor.putString(name, value).commit();
  }

  public String getStringInitEmpty(String name) {
    return pref.getString(name, "");
  }
}
