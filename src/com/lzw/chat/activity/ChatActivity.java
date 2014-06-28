package com.lzw.chat.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.avos.avoscloud.*;
import com.lzw.chat.R;
import com.lzw.chat.adapter.ChatMsgViewAdapter;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.avobject.User;
import com.lzw.chat.base.App;
import com.lzw.chat.dao.MsgDao;
import com.lzw.chat.db.DBHelper;
import com.lzw.chat.entity.ChatMsgEntity;
import com.lzw.chat.receiver.AVReceiver;
import com.lzw.chat.service.ChatService;
import com.lzw.chat.service.PairQuery;
import com.lzw.chat.utils.PathUtils;
import com.lzw.chat.view.RecordButton;
import com.lzw.commons.Logger;
import com.lzw.commons.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends Activity implements View.OnClickListener {
  public static final String CUR_MODE = "curMode";
  public static final int KEYBOARD = 0;
  public static final int VOICE_MODE = 1;
  private Button mBtnSend;
  private EditText mEditTextContent;
  private ListView mListView;
  private ChatMsgViewAdapter mAdapter;
  private List<ChatMsgEntity> mDataArrays;
  public static ChatActivity instance;
  Activity cxt;
  public static boolean isPause;
  RecordButton recordBtn;
  int recordN = 0;
  MediaPlayer player;
  private View recordLayout;
  private View keyboardLayout;
  private View voiceBtn;
  private View keyboardBtn;
  ChatMsgViewAdapter.ViewHolder curHolder;
  MyOnCompletionListener completionListener;
  int curMode;
  DBHelper dbHelper;
  public static String pairedUser;
  AVUser pairedAVUser;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    instance = this;
    dbHelper = new DBHelper(cxt, App.DB_NAME, App.DB_VER);
    App.initRoomInfo(cxt);
    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.chat_xiaohei);
    findView();
    saveInstallId();
    initView();
    initPlayer();
    initData();
    initPush();
    completionListener = new MyOnCompletionListener();
    loadCurMode();
    AVReceiver.cancelNotify(this);
    getAVUser();
  }

  private void getAVUser() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          pairedAVUser = User.getAVUser(pairedUser);
        } catch (AVException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void loadCurMode() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    int mode = pref.getInt(CUR_MODE, VOICE_MODE);
    setLayoutByMode(mode);
  }

  private void setLayoutByMode(int mode) {
    if (mode == KEYBOARD) {
      keyboardBtn.performClick();
    } else {
      voiceBtn.performClick();
    }
  }

  private void initPlayer() {
    player = new MediaPlayer();
  }

  private void findView() {
    recordBtn = (RecordButton) findViewById(R.id.recordBtn);
    String path = PathUtils.getRecordDir() + recordN;
    recordBtn.setSavePath(path);
    recordBtn.setOnFinishedRecordListener(new RecordButton.RecordEventListener() {
      @Override
      public void onFinishedRecord(String audioPath, int length) {
        recordN++;
        //Utils.toast(cxt,R.string.ok);
        SendTask sendTask = new SendTask(null, audioPath);
        sendTask.setLen(length);
        sendTask.execute();
      }

      @Override
      public void onStartRecord() {
        pausePlayer();
      }
    });
    recordLayout = findViewById(R.id.recordLayout);
    keyboardLayout = findViewById(R.id.rl_bottom);
    voiceBtn = findViewById(R.id.voiceBtn);
    keyboardBtn = findViewById(R.id.keyboardBtn);
    voiceBtn.setOnClickListener(this);
    keyboardBtn.setOnClickListener(this);
  }

  private void pausePlayer() {
    if (player != null && player.isPlaying()) {
      player.pause();
    }
  }

  private void initPush() {
    PushService.setDefaultPushCallback(this, MainActivity.class);
  }

  @Override
  protected void onPause() {
    super.onPause();
    isPause = true;
    AVAnalytics.onPause(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    isPause = false;
    postRefresh();
    AVAnalytics.onResume(this);
  }

  private void postRefresh() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        refresh();
      }
    }, 200);
  }

  private void saveInstallId() {
    AVInstallation.getCurrentInstallation().saveInBackground();
  }

  public void initView() {
    mListView = (ListView) findViewById(R.id.listview);
    mBtnSend = (Button) findViewById(R.id.btn_send);
    mBtnSend.setOnClickListener(this);

    mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
  }

  public void initData() {
    mDataArrays = new ArrayList<ChatMsgEntity>();
    mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
    mAdapter.setOnClickListener(new ClickListener());
    mListView.setAdapter(mAdapter);
    pairedUser = getIntent().getStringExtra(PairQuery.PAIRED_USER);
  }


  public void refresh() {
    new GetDataTask().execute();
  }

  class GetDataTask extends AsyncTask<Void, Void, Void> {
    boolean res;
    List<Msg> msgs;
    int start;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      start = mAdapter.getCount();
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        msgs = MsgDao.avGetMsgs(User.getMyName(), pairedUser, start);
        ChatService.prepareMsgsDataByNet(msgs);
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (res) {
        addMsgsAndRefresh(msgs);
      } else {
        Utils.toast(cxt, R.string.getDataFailed);
      }
    }
  }


  public void addMsgAndRefresh(Msg msg) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    addMsgsAndRefresh(msgs);
  }

  public void addMsgsAndRefresh(List<Msg> msgs) {
    List<ChatMsgEntity> sublists = new ArrayList<ChatMsgEntity>();
    for (Msg msg : msgs) {
      ChatMsgEntity entity = ChatService.getChatMsgEntity(msg);
      sublists.add(entity);
    }
    mDataArrays.addAll(sublists);
    mAdapter.notifyDataSetChanged();
    scroolToLast();
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.btn_send:
        send();
        break;
      case R.id.keyboardBtn:
        showKeyboard();
        break;
      case R.id.voiceBtn:
        showVoice();
        break;
    }
  }

  private void showVoice() {
    curMode = VOICE_MODE;
    keyboardLayout.setVisibility(View.GONE);
    recordLayout.setVisibility(View.VISIBLE);
  }

  private void showKeyboard() {
    curMode = KEYBOARD;
    keyboardLayout.setVisibility(View.VISIBLE);
    recordLayout.setVisibility(View.GONE);
  }

  private void send() {
    String contString = mEditTextContent.getText().toString();
    if (contString.length() > 0) {
      new SendTask(contString, null).execute();
      mEditTextContent.setText("");
    }
  }

  class SendTask extends AsyncTask<Void, Void, Void> {
    String text;
    Msg msg;
    String voicePath;
    int len;
    boolean res;

    public SendTask(String text, String voicePath) {
      this.text = text;
      this.voicePath = voicePath;
    }

    public void setLen(int len) {
      this.len = len;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        msg = new Msg();
        if (text == null) {
          AVFile file = AVFile.withAbsoluteLocalPath(User.getMyName() + System.currentTimeMillis(),
              voicePath);
          file.save();
          msg.setVoiceUrl(file.getUrl());
          Logger.d("save a recod file");
        } else {
          msg.setText(text);
        }
        msg.setLength(len);
        msg.setFromUser(User.getMyName());
        msg.setToUser(pairedUser);
        msg.setPostTime(new Date());
        msg.save();
        AVReceiver.pushNotify(cxt, pairedAVUser, msg);
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (res) {
        ChatService.prepareMsgByLocal(msg, voicePath);
        addMsgAndRefresh(msg);
      } else {
        Utils.toast(cxt, R.string.no_network);
      }
    }
  }

  private void scroolToLast() {
    mListView.setSelection(mListView.getCount() - 1);
  }

  class ClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      ChatMsgViewAdapter.ViewHolder holder =
          (ChatMsgViewAdapter.ViewHolder) v.getTag();
      try {
        playAudio(holder);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void playAudio(ChatMsgViewAdapter.ViewHolder holder) throws Exception {
    String voicePath = holder.msg.getVoicePath();
    if (player.isPlaying()) {
      showVoiceImg();
      //player.pause();
      //player.reset();
    }
    Logger.d("play voice");
    player.reset();
    ChatService.preparePlayerByFD(player, voicePath);
    player.setOnCompletionListener(completionListener);
    player.start();
    curHolder = holder;
    hideVoiceImg();
  }

  private void showVoiceImg() {
    curHolder.voiceImgView.setVisibility(View.VISIBLE);
  }

  private void hideVoiceImg() {
    curHolder.voiceImgView.setVisibility(View.GONE);
  }


  private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
    @Override
    public void onCompletion(MediaPlayer mp) {
      if (curHolder != null) {
        showVoiceImg();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (player != null) {
      player.release();
    }
    saveCurMode();
  }

  private void saveCurMode() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(cxt);
    pref.edit().putInt(CUR_MODE, curMode).commit();
  }
}


