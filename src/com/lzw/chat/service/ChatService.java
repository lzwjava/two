package com.lzw.chat.service;

import android.media.MediaPlayer;
import com.avos.avoscloud.AVException;
import com.lzw.chat.R;
import com.lzw.chat.activity.ChatActivity;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.avobject.User;
import com.lzw.chat.base.App;
import com.lzw.chat.dao.MsgDao;
import com.lzw.chat.db.DBHelper;
import com.lzw.chat.db.DBMsg;
import com.lzw.chat.entity.ChatMsgEntity;
import com.lzw.chat.utils.PathUtils;
import com.lzw.commons.NetAsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-6-28.
 */
public class ChatService {
  @Deprecated
  public static void insertMsgById(String msgId) throws AVException {
    Msg msg = MsgDao.getMsgById(msgId);
    DBHelper dbHelper = new DBHelper(App.cxt, App.DB_NAME, App.DB_VER);
    DBMsg.insertMsg(dbHelper, msg);
  }

  public static ChatMsgEntity getChatMsgEntity(Msg msg) {
    ChatMsgEntity entity = new ChatMsgEntity();
    entity.setDate(com.lzw.commons.TimeUtils.getDate(msg.getPostTime()));
    String fromUser = msg.getFromUser();
    String myName = User.getMyName();
    if (myName.equals(fromUser)) {
      entity.setName(App.cxt.getString(R.string.me));
      entity.setMsgType(false);
    } else {
      entity.setName(App.cxt.getString(R.string.ta));
      entity.setMsgType(true);
    }
    if (msg.isText()) {
      entity.setText(msg.getText());
    } else {
      entity.setVoicePath(msg.getVoicePath());
      entity.setLength(msg.getLength());
    }
    return entity;
  }

  public static void addMsgById(final String msgId) throws AVException {
    new NetAsyncTask(App.cxt, false) {
      Msg msg;

      @Override
      protected void doInBack() throws Exception {
        msg = MsgDao.getMsgById(msgId);
        List<Msg> msgs = new ArrayList<Msg>();
        msgs.add(msg);
        prepareMsgsDataByNet(msgs);
      }

      @Override
      protected void onPost(boolean res) {
        if (res) {
          ChatActivity.instance.addMsgAndRefresh(msg);
        } else {
        }
      }
    }.execute();
  }

  public static void prepareMsgsData(List<Msg> msgs, DataFactory dataFactory) {
    for (Msg msg : msgs) {
      String objId = msg.getObjectId();
      String dir = PathUtils.getCacheDir();
      String path = dir + objId;
      File f = new File(path);
      if (msg.isText() == false) {
        if (f.exists() == false) {
          String voiceUrl = msg.getVoiceUrl();
          if (voiceUrl != null) {
            dataFactory.prepareData(voiceUrl, path);
          }
        }
        msg.setVoicePath(path);
      }
    }
  }

  public static void prepareMsgsDataByNet(List<Msg> msgs) throws IOException {
    prepareMsgsData(msgs, new ChatService.DataFactory() {
      @Override
      public void prepareData(String voiceUrl, String path) {
        Network.downloadUrlToPath(voiceUrl, path);
      }
    });
  }

  public static void prepareMsgByLocal(Msg msg, final String localPath) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    prepareMsgsData(msgs, new DataFactory() {

      @Override
      public void prepareData(String voiceUrl, String path) {
        File file = new File(localPath);
        file.renameTo(new File(path));
      }
    });
  }

  public static void preparePlayerByFD(MediaPlayer player, String path) throws Exception {
    if (path == null) {
      return;
    }
    File f = new File(path);
    if (!f.exists()) {
      throw new Exception("no such file exists");
    } else {
      FileInputStream in = new FileInputStream(f);
      player.setDataSource(in.getFD());
      player.prepare();
    }
  }


  public interface DataFactory {
    void prepareData(String voiceUrl, String path);
  }
}
