package com.lzw.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("UpdateInfo")
public class UpdateInfo extends AVObject{
  public static String VERSION="version";
  public static String DESCRIPTION="description";
  public static String APK_URL ="apkUrl";
  String apkUrl;

  public UpdateInfo(){ 
  }
  
  public int getVersion() {
    return getInt(VERSION);
  }
  public void setVersion(int version) {
    put(VERSION, version);
  }
  public String getDescription() {
    return getString(DESCRIPTION);
  }
  public void setDescription(String description) {
    put(DESCRIPTION,description);
  }

  public String getApkUrl() {
    return getString(APK_URL);
  }

  public void setApkUrl(String apkUrl) {
    put(APK_URL,apkUrl);
  }
}
