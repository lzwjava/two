package com.lzw.chat.service;

import android.content.Context;
import com.avos.avoscloud.AVUser;
import com.lzw.chat.avobject.User;
import com.lzw.commons.NetAsyncTask;

/**
 * Created by lzw on 14-6-26.
 */
public class LoginService {
  public static void AVLogin(Context cxt, final String id, String pwd) {
    new NetAsyncTask(cxt, false) {
      @Override
      protected void doInBack() throws Exception {
        AVUser curUser = AVUser.getCurrentUser();
        if (curUser == null) {
          AVUser user = new AVUser();
          user.setUsername(id);
          user.setPassword(id);
          try {
            user.signUp();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        AVUser.logIn(id, id);
        User.saveIntallationId();
      }

      @Override
      protected void onPost(boolean res) {
        if (res) {

        } else {
          com.lzw.commons.Logger.d("login failed");
        }
      }
    }.execute();
  }
}
