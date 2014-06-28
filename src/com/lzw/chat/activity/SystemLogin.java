package com.lzw.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.avos.avoscloud.AVAnalytics;
import com.lzw.chat.R;
import com.lzw.chat.base.App;
import com.lzw.chat.dao.PrefDao;
import com.lzw.chat.service.LoginService;
import com.lzw.chat.utils.VpnUtils;
import com.lzw.commons.HttpUtils;
import com.lzw.commons.NetAsyncTask;
import com.lzw.commons.Utils;

import java.io.IOException;

import static com.lzw.commons.HttpUtils.doGet;

/**
 * Created by lzw on 14-5-29.
 */
public class SystemLogin extends Activity implements View.OnClickListener {
  View login;
  EditText idEdit;
  EditText pwdEdit;
  private Activity cxt;
  PrefDao prefDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    setContentView(R.layout.system_login);
    AVAnalytics.trackAppOpened(getIntent());
    prefDao = new PrefDao(cxt);
    login = findViewById(R.id.login);
    idEdit = (EditText) findViewById(R.id.studentId);
    pwdEdit = (EditText) findViewById(R.id.password);
    login.setOnClickListener(this);

    String systemId = prefDao.getSystemId();
    if (systemId.equals("") == false) {
      idEdit.setText(systemId);
      pwdEdit.setText(prefDao.getSystemPwd());
      //loginTask(App.id, App.pwd);
    }
    if (App.debug) {
      //idEdit.setText("130824115");
      //pwdEdit.setText("37068319");
    }
  }

  public void loginTask(final String id, final String pwd) {
    new NetAsyncTask(cxt) {
      boolean loginRes = false;

      @Override
      protected void doInBack() throws Exception {
        if (App.isUseVpn) {
          VpnUtils.logOnVpn();
        }
        loginRes = login(id, pwd);
        //logOff();
        if (App.isUseVpn) {
          VpnUtils.logOutVpn();
        }
      }

      @Override
      protected void onPost(boolean res) {
        if (res) {
          if (loginRes) {
            prefDao.setSystemId(id);
            prefDao.setSystemPwd(pwd);
            LoginService.AVLogin(cxt, id, pwd);
            goMenu(id);
            finish();
          } else {
            Utils.toast(cxt, R.string.wrongPwd);
          }
        } else {
          Utils.toast(cxt, R.string.no_net);
        }
      }
    }.execute();
  }

  private void goMenu(String id) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra(PrefDao.SYSTEM_ID, id);
    startActivity(intent);
  }

  public static boolean login(String userCode, String pwd) throws IOException {
    String submit;
    String post;
    submit = "+%CC%E1%A1%A1%BD%BB+";
    if (App.isUseVpn) {
      post = HttpUtils.post(App.client, "https://vpn.bjfu.edu.cn/jwxt/,DanaInfo=jwxt.bjfu.edu.cn+logon.asp",
          "type", "Logon", "UserCode", userCode, "UserPassword", pwd,
          "B1", submit);
    } else {
      post = HttpUtils.post(App.client, "http://jwxt.bjfu.edu.cn/jwxt/Logon.asp",
          "type", "Logon", "UserCode", userCode, "UserPassword", pwd,
          "B1", submit);
    }
    //
    String string = App.cxt.getString(R.string.pleaseLogin);
    //Logger.d("logon post=" + post);
    if (post != null && post.contains(string)) {
      return true;
    } else {
      return false;
    }
  }

  public static void logOff() throws IOException {
    String url = "https://vpn.bjfu.edu.cn/jwxt/,DanaInfo=jwxt.bjfu.edu.cn+Logoff.asp";
    String s = doGet(App.client, url);
    //Logger.d("logOff=" + s);
  }

  @Override
  public void onClick(View v) {
    final String id = idEdit.getText().toString();
    final String pwd = pwdEdit.getText().toString();
    if (id.isEmpty() == false && pwd.isEmpty() == false) {
      loginTask(id, pwd);
    }
  }
}
