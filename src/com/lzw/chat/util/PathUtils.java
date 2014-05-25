package com.lzw.chat.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by lzw on 14-5-25.
 */
public class PathUtils {
  static String appDir = "/two/";

  public static String getAppDir() {
    String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + appDir;
    checkDir(dir);
    return dir;
  }

  public static void checkDir(String dirPath) {
    File dir = new File(dirPath);
    if (dir.exists() == false) {
      dir.mkdirs();
    }
  }

  public static String getRecordDir() {
    String dir = getAppDir() + "record/";
    checkDir(dir);
    return dir;
  }

  public static String getCacheDir() {
    String dir=getAppDir()+"cache/";
    checkDir(dir);
    return dir;
  }
}
