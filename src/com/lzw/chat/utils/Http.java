package com.lzw.chat.utils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lzw on 14-5-29.
 */
public class Http {

  public interface ProgressCallBack {
    void progressUpdate(int per);
  }

  public static boolean connectSchoolChannel() {
    String url = "http://jwxt.bjfu.edu.cn/jwxt/";
    int code;
    HttpURLConnection conn =null;
    try {
      URL url1 = new URL(url);
      conn = (HttpURLConnection) url1.openConnection();
      conn.setConnectTimeout(1000);
      conn.setReadTimeout(1000);
      conn.connect();
      code = conn.getResponseCode();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      try {
        if (conn != null) {
          conn.disconnect();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    com.lzw.commons.Logger.d("code=" + code);
    if (code == 200 || code == 206 || code == 304) {
      return true;
    } else {
      return false;
    }
  }
}
