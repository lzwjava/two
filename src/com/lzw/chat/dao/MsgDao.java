package com.lzw.chat.dao;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.db.DBHelper;
import com.lzw.chat.db.DBMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-6-28.
 */
public class MsgDao {
  @Deprecated
  public static List<Msg> avInsertRoomMsg(DBHelper dbHelper, int start, String myName,
                                          String herName) {
    int count = DBMsg.getMsgsCountByDB(dbHelper, myName, herName);
    try {
      List<Msg> msgs = avGetMsgs(myName, herName, count);
      DBMsg.insertMsgs(dbHelper, msgs);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return DBMsg.getRoomMsgsByDB(dbHelper, start, myName, herName);
  }


  public static List<Msg> avGetMsgs(String myName, String herName, int count) throws AVException {
    AVQuery<Msg> q = getAVQueryOfTwo(myName, herName);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.setLimit(1000);
    q.skip(count);
    q.orderByAscending(Msg.CREATED_AT);
    List<Msg> msgs;
    msgs = q.find();
    return msgs;
  }

  @Deprecated
  public static List<Msg> getRoomMsgs(DBHelper dbHelper, int start, String myName,
                                      String herName) {
    return DBMsg.getRoomMsgsByDB(dbHelper, start, myName, herName);
  }

  private static AVQuery<Msg> getAVQueryOfTwo(String myName, String herName) {
    List<AVQuery<Msg>> qs = new ArrayList<AVQuery<Msg>>();
    AVQuery<Msg> q1 = AVObject.getQuery(Msg.class);
    q1.whereEqualTo(Msg.FROM_USER, myName);
    q1.whereEqualTo(Msg.TO_USER, herName);
    qs.add(q1);
    AVQuery<Msg> q2 = AVObject.getQuery(Msg.class);
    q2.whereEqualTo(Msg.FROM_USER, herName);
    q2.whereEqualTo(Msg.TO_USER, myName);
    qs.add(q2);
    return AVQuery.or(qs);
  }

  public static Msg getMsgById(String msgId) throws AVException {
    AVQuery<Msg> q = AVObject.getQuery(Msg.class);
    return q.get(msgId);
  }
}
