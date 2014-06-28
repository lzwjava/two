package com.lzw.chat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.lzw.chat.R;
import com.lzw.chat.base.App;
import com.lzw.chat.dao.PrefDao;
import com.lzw.chat.service.LoginService;
import com.lzw.chat.utils.Http;
import com.lzw.commons.Logger;
import com.lzw.commons.NetAsyncTask;
import com.lzw.commons.Utils;

/**
 * Created by lzw on 14-5-21.
 */
public class SplashActivity extends Activity implements View.OnClickListener {
  public static final int SPLASH_TIME = 800;
  PrefDao prefDao;
  private Activity cxt;
  TextView infoView;
  View retry, normalLayout, errorLayout, continueIt;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.spalash_layout);
    cxt = this;
    prefDao = new PrefDao(cxt);
    findView();

    runTask();
  }

  private void runTask() {
    NetAsyncTask netAsyncTask = new NetAsyncTask(cxt) {
      boolean schoolConnect;
      boolean httpConnect;

      @Override
      protected void doInBack() {
        long st = System.currentTimeMillis();
        httpConnect = com.lzw.commons.HttpUtils.isConnectNet();
        if (httpConnect == false) {
          return;
        }
        schoolConnect = Http.connectSchoolChannel();
        try {
          long now;
          do {
            now = System.currentTimeMillis();
            Thread.sleep(100);
          } while (now - st < SPLASH_TIME);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      @Override
      protected void onPost(boolean res) {
        if (httpConnect) {
          if (schoolConnect) {
            dontUseVpn();
          } else {
            useVpn();
          }
        } else {
          errorLayout.setVisibility(View.VISIBLE);
          normalLayout.setVisibility(View.GONE);
          Utils.toast(cxt, R.string.no_net_reopen);
        }
      }
    };
    netAsyncTask.setOpenDialog(false);
    netAsyncTask.execute();
  }

  private void dontUseVpn() {
    Logger.d("don't use");
    App.isUseVpn = false;
    checkAndLogin();
    finish();
  }

  private void checkAndLogin() {
    String id = prefDao.getSystemId();
    String pwd = prefDao.getSystemPwd();
    if (id.equals("") == false) {
      LoginService.AVLogin(cxt, id, pwd);
    }
    if (prefDao.getSystemId().equals("")) {
      com.lzw.commons.Utils.goActivity(cxt, SystemLogin.class);
    } else {
      com.lzw.commons.Utils.goActivity(cxt, MainActivity.class);
    }
  }

  private void useVpn() {
    App.isUseVpn = true;
    Logger.d("use");
    checkAndLogin();
    finish();
  }

  private void findView() {
    infoView = (TextView) findViewById(R.id.infoView);
    retry = findViewById(R.id.retry);
    normalLayout = findViewById(R.id.normalLayout);
    errorLayout = findViewById(R.id.errorLayout);
    continueIt = findViewById(R.id.continueIt);
    retry.setOnClickListener(this);
    continueIt.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.retry) {
      runTask();
      errorLayout.setVisibility(View.GONE);
      normalLayout.setVisibility(View.VISIBLE);
    } else if (id == R.id.continueIt) {
      useVpn();
    }
  }
}
