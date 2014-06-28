package com.lzw.chat.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.PushService;
import com.lzw.chat.R;
import com.lzw.chat.base.App;

/**
 * Created by lzw on 14-5-25.
 */
public class Utils {

  public static void toast(Activity cxt, int id) {
    Toast.makeText(cxt, id, Toast.LENGTH_SHORT).show();
  }

  public static ProgressDialog showSpinnerDialog(Activity activity) {
    activity = com.lzw.commons.Utils.modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(App.cxt.getString(R.string.hardLoading));
    dialog.show();
    return dialog;
  }


  public static void playRingtone(Context cxt) {
    MediaPlayer player = new MediaPlayer();
    Uri uri = com.lzw.commons.Utils.getDefaultRingtoneUri(cxt, RingtoneManager.TYPE_ALARM);
    try {
      player.setDataSource(cxt, uri);
    } catch (Exception e) {
      e.printStackTrace();
    }
    final AudioManager audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
      player.setAudioStreamType(AudioManager.STREAM_ALARM);
      player.setLooping(true);
      try {
        player.prepare();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      player.start();
    }
  }

  public static void playRingTone(Context ctx, int type) {
    MediaPlayer mMediaPlayer = MediaPlayer.create(ctx,
        com.lzw.commons.Utils.getDefaultRingtoneUri(ctx, type));
    //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
    mMediaPlayer.setLooping(true);
    mMediaPlayer.start();
  }

  public static ProgressDialog showHorizontalDialog(Activity activity) {
    activity = com.lzw.commons.Utils.modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setCancelable(true);
    dialog.show();
    return dialog;
  }

  public static void initPushService(Activity cxt, Class<? extends Activity> clz) {
    PushService.setDefaultPushCallback(cxt, clz);
    if (App.debug) {
      PushService.unsubscribe(cxt, "public");
      PushService.subscribe(cxt, "debug", clz);
    } else {
      PushService.unsubscribe(cxt, "debug");
      PushService.subscribe(cxt, "public", clz);
    }
    AVInstallation.getCurrentInstallation().saveInBackground();
  }
}
