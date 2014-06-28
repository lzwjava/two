package com.lzw.chat.avobject;

import com.avos.avoscloud.*;

import java.util.List;

/**
 * Created by lzw on 14-6-26.
 */
public class User {
  public static final String HAS_VOTED = "hasVoted";
  public static final String USERNAME = "username";
  public static final String INSTALLATION_ID = "installationId";

  public static boolean isHasVoted() {
    return curUser().getBoolean(HAS_VOTED);
  }

  public static void setHasVoted(boolean hasVoted) {
    curUser().put(HAS_VOTED, hasVoted);
  }

  public static AVUser curUser() {
    return AVUser.getCurrentUser();
  }

  public static AVUser getAVUser(String username) throws AVException {
    AVQuery<AVUser> q = getUserQuery(username);
    List<AVUser> users = q.find();
    if (users != null && users.isEmpty() == false) {
      return users.get(0);
    }
    return null;
  }

  public static AVQuery<AVUser> getUserQuery(String username) {
    AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
    q.whereEqualTo(USERNAME, username);
    q.setLimit(1);
    return q;
  }

  public static boolean isEqualCurrentUser(AVUser fromUser) {
    AVUser curUser = AVUser.getCurrentUser();
    return fromUser.getUsername().equals(curUser.getUsername());
  }

  public static boolean isEqual(AVUser fromUser, AVUser currentUser) {
    return fromUser.getUsername().equals(currentUser.getUsername());
  }

  public static void saveIntallationId() throws AVException {
    AVUser user = AVUser.getCurrentUser();
    AVInstallation.getCurrentInstallation().save();
    String id = AVInstallation.getCurrentInstallation().getInstallationId();
    String curId = User.getInstallationId(user);
    if (curId == null || curId.equals(id) == false) {
      user.put(INSTALLATION_ID, id);
      user.save();
    }
  }

  public static String getInstallationIdByName(String name) throws AVException {
    AVUser avUser = User.getAVUser(name);
    return getInstallationId(avUser);
  }

  public static String getInstallationId(AVUser user) {
    if (user == null) {
      throw new NullPointerException("user is null");
    }
    return user.getString(INSTALLATION_ID);
  }

  public static String getMyName() {
    return AVUser.getCurrentUser().getUsername();
  }

  public static void saveIntallationIdInBack() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          saveIntallationId();
        } catch (AVException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
