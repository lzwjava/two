package com.lzw.chat.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.PushService;
import com.lzw.chat.R;
import com.lzw.chat.adapter.ChatMsgViewAdapter;
import com.lzw.chat.avobject.AVReceiver;
import com.lzw.chat.avobject.Msg;
import com.lzw.chat.base.App;
import com.lzw.chat.entity.ChatMsgEntity;
import com.lzw.chat.mgr.UpdateTask;
import com.lzw.chat.util.Logger;
import com.lzw.chat.util.PathUtils;
import com.lzw.chat.util.TimeUtils;
import com.lzw.chat.util.Utils;
import com.lzw.chat.view.RecordButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity implements OnClickListener {
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
  ProgressBar progressBar;
  int curMode;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    instance = this;
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
    UpdateTask.runUpdateTask(cxt);
    loadCurMode();
  }

  private void loadCurMode() {
    SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
    int mode= pref.getInt(CUR_MODE, VOICE_MODE);
    setLayoutByMode(mode);
  }

  private void setLayoutByMode(int mode) {
    if(mode==KEYBOARD){
      keyboardBtn.performClick();
    }else{
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
    progressBar= (ProgressBar) findViewById(R.id.progressBar);
    recordLayout = findViewById(R.id.recordLayout);
    keyboardLayout = findViewById(R.id.rl_bottom);
    voiceBtn = findViewById(R.id.voiceBtn);
    keyboardBtn = findViewById(R.id.keyboardBtn);
    voiceBtn.setOnClickListener(this);
    keyboardBtn.setOnClickListener(this);
  }

  private void pausePlayer() {
    if(player!=null && player.isPlaying()){
      player.pause();
    }
  }

  private void initPush() {
    PushService.setDefaultPushCallback(this, ChatActivity.class);
    PushService.subscribe(this, AVReceiver.getChannel(), ChatActivity.class);
  }

  @Override
  protected void onPause() {
    super.onPause();
    isPause = true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    isPause = false;
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
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        refresh();
      }
    }, 100);
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
        msgs = Msg.getRoomMsgs(start);
        for (Msg msg : msgs) {
          AVFile voice = msg.getVoice();
          String objId = msg.getObjectId();
          String dir = PathUtils.getCacheDir();
          String path = dir + objId;
          File f = new File(path);
          if(msg.isText()==false){
            if (f.exists() == false) {
              byte[] data = voice.getData();
              FileOutputStream out = new FileOutputStream(f);
              out.write(data, 0, data.length);
              out.close();
            }
            int len = getAudioLength(path);
            msg.setLength(Math.round(len*1.0f/1000));
            msg.setVoicePath(path);
          }
        }
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
      progressBar.setVisibility(View.INVISIBLE);
      if (res) {
        List<ChatMsgEntity> sublists=new ArrayList<ChatMsgEntity>();
        for (Msg msg : msgs) {
          ChatMsgEntity entity = getChatMsgEntity(msg);
          sublists.add(entity);
        }
        mDataArrays.addAll(sublists);
        mAdapter.notifyDataSetChanged();
        scroolToLast();
      } else {
        Utils.toast(cxt, R.string.no_network);
      }
    }
  }


  private int getAudioLength(String path) throws IOException {
    int drt=3;
    try{
      player.reset();
      player.setDataSource(path);
      player.prepare();
      drt= player.getDuration();
    }catch (Exception e){
      e.printStackTrace();
    }
    return drt;
  }

  private ChatMsgEntity getChatMsgEntity(Msg msg) {
    ChatMsgEntity entity = new ChatMsgEntity();
    entity.setDate(TimeUtils.getDate(msg.getCreatedAt()));
    String fromId = msg.getFromId();
    String curId = Utils.getWifiMac(cxt);
    if (curId.equals(fromId)) {
      entity.setName(getString(R.string.me));
      entity.setMsgType(false);
    } else {
      entity.setName(getString(R.string.ta));
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
    curMode=VOICE_MODE;
    keyboardLayout.setVisibility(View.GONE);
    recordLayout.setVisibility(View.VISIBLE);
  }

  private void showKeyboard() {
    curMode=KEYBOARD;
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
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        msg = new Msg();
        if (text == null) {
          AVFile file = AVFile.withAbsoluteLocalPath(App.room+ System.currentTimeMillis(),
              voicePath);
          file.save();
          msg.setVoice(file);
          Logger.d("save a recod file");
        } else {
          msg.setText(text);
        }
        msg.setFromId(Utils.getWifiMac(cxt));
        msg.setRoom(App.room);
        msg.save();
        AVReceiver.pushNotify(cxt);
        res=true;
      } catch (Exception e) {
        e.printStackTrace();
        res=false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if(res){
      }else{
        Utils.toast(cxt,R.string.no_network);
      }
    }
  }

  private void scroolToLast() {
    mListView.setSelection(mListView.getCount() - 1);
  }

  class ClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      ChatMsgViewAdapter.ViewHolder holder =
          (ChatMsgViewAdapter.ViewHolder) v.getTag();
      try {
        playAudio(holder);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void playAudio(ChatMsgViewAdapter.ViewHolder holder) throws IOException {
    String voicePath = holder.msg.getVoicePath();
    if (player.isPlaying()) {
      showVoiceImg();
      //player.pause();
      //player.reset();
    }
    Logger.d("play voice");
    player.reset();
    player.setDataSource(voicePath);
    player.prepare();
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
    SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(cxt);
    pref.edit().putInt(CUR_MODE,curMode).commit();
  }
}


