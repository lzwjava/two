package com.lzw.chat.mgr;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lzw on 14-5-28.
 */
public class Network {
  public static void downloadUrlToPath(String url2, String path) {
    // TODO Auto-generated method stub
    BufferedInputStream bInput = null;
    BufferedOutputStream bOutput = null;
    try {
      HttpURLConnection conn = null;
      URL url = new URL(url2);
      conn = (HttpURLConnection) url.openConnection();
      bInput = getBufferedInput(conn);
      bOutput = getBufferedOutput(path);
      byte[] buffer = new byte[1024];
      int cnt;
      while ((cnt = bInput.read(buffer)) != -1) {
        bOutput.write(buffer, 0, cnt);
      }
      bOutput.flush();
      if (conn != null) {
        conn.disconnect();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (bInput != null)
          bInput.close();
        if (bOutput != null)
          bOutput.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static BufferedOutputStream getBufferedOutput(String path)
      throws IOException {
    BufferedOutputStream bOutput;
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    file.createNewFile();
    bOutput = new BufferedOutputStream(new FileOutputStream(file));
    return bOutput;
  }

  public static BufferedInputStream getBufferedInput(HttpURLConnection conn)
      throws IOException {
    BufferedInputStream bInput;
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(15000);
    conn.setDoInput(true);
    conn.setDoOutput(true);
    bInput = new BufferedInputStream(conn.getInputStream());
    return bInput;
  }
}
