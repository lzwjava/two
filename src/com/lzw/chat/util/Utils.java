package com.lzw.chat.util;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by lzw on 14-5-25.
 */
public class Utils {

  public static void toast(Activity cxt, int id) {
    Toast.makeText(cxt,id,Toast.LENGTH_SHORT).show();
  }
}
