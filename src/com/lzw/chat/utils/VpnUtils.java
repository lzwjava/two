package com.lzw.chat.utils;


import com.lzw.chat.R;
import com.lzw.chat.activity.SystemLogin;
import com.lzw.chat.base.App;
import com.lzw.chat.dao.PrefDao;
import com.lzw.commons.HttpUtils;
import com.lzw.commons.Logger;
import com.lzw.commons.SSLSocketFactoryEx;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyStore;

/**
 * Created by lzw on 14-6-7.
 */
public class VpnUtils {
  private static final int TIME_OUT = 3000;

  public static boolean logOnVpn() throws IOException {
    String url = "https://vpn.bjfu.edu.cn/dana-na/auth/url_default/login.cgi";
    boolean post = vpnLogin(App.client, url, "tz_offset", "480", "username", "130844102",
        "realm", "Users", "password", "130844102", "btnSubmit.x", "0", "btnSubmit.y", "0",
        "btnSubmit", "Sign In");
    return post;
  }

  public static boolean simpleLogon(String vpnId, String vpnPwd) throws IOException {
    String url = "https://vpn.bjfu.edu.cn/dana-na/auth/url_default/login.cgi";
    HttpResponse response = HttpUtils.getResponse(App.client, url, "tz_offset", "480", "username", vpnId,
        "realm", "Users", "password", vpnPwd, "btnSubmit.x", "0", "btnSubmit.y", "0",
        "btnSubmit", "Sign In");
    String post = EntityUtils.toString(response.getEntity());
    if (post.contains(App.cxt.getResources().getString(R.string.continueLogon))) {
      continueLogonByResponse(post);
      Logger.d("continue logon");
    }
    //Logger.d("post ="+post);
    return true;
  }

  public static void logOutVpn() throws IOException {
    String url = "https://vpn.bjfu.edu.cn/dana-na/auth/logout.cgi";
    String s = HttpUtils.doGet(App.client, url);
    //Logger.d("log out response="+s);
  }

  public static HttpClient getNewHttpClient() {
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
      sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      HttpParams params = new BasicHttpParams();
      HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
      SchemeRegistry registry = new SchemeRegistry();
      registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
      registry.register(new Scheme("https", sf, 443));
      ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
      DefaultHttpClient client = new DefaultHttpClient(ccm, params);
      client.getParams().setParameter("http.connection.timeout", new Integer(TIME_OUT));
      client.getParams().setParameter("http.socket.timeout", new Integer(TIME_OUT));
      return client;
    } catch (Exception e) {
      return new DefaultHttpClient();
    }
  }

  public static void continueLogon() throws IOException {
    String url = "https://vpn.bjfu.edu.cn/dana/home/index.cgi";
    String s = HttpUtils.doGet(App.client, url);
  }

  public static boolean vpnLogin(HttpClient httpClient, String url, String... pairs) throws IOException {
    HttpPost post = HttpUtils.getBasePost(url, pairs);
    HttpResponse response;
    HttpContext httpContext = new BasicHttpContext();
    response = httpClient.execute(post, httpContext);
    HttpHost targetHost = (HttpHost) httpContext.
        getAttribute(ExecutionContext.HTTP_TARGET_HOST);
    HttpUriRequest realRequest = (HttpUriRequest) httpContext.
        getAttribute(ExecutionContext.HTTP_REQUEST);
    String uri = realRequest.getURI().toString();
    Logger.d("host=" + targetHost.toString() + " realRequest=" + uri);
    String string = EntityUtils.toString(response.getEntity());
    if (uri.contains("failed")) {
      return false;
    } else if (uri.contains("user-confirm")) {
      Logger.d("user-confirm");
      continueLogonByResponse(string);
      return true;
    } else if (uri.contains("check=yes")) {
      return true;
    } else if (uri.contains("starter.cgi?startpageonly=1")) {
      Logger.d("is start page only");
      return true;
    } else {
      Logger.d("unknown login =" + string);
      throw new UnknownHostException("unknown login");
    }
  }

  public static void continueLogonByResponse(String str) throws IOException {
    Document doc = Jsoup.parse(str);
    Element select = doc.select("#DSIDFormDataStr").first();
    String val = select.val();
    Logger.d("val is" + val);
    String gb2312Encode = com.lzw.commons.Utils.getGb2312Encode(val);
    String post1 = HttpUtils.post(App.client, "https://vpn.bjfu.edu.cn/dana-na/auth/url_default/login.cgi",
        "btnContinue", "%E7%BB%A7%E7%BB%AD%E4%BC%9A%E8%AF%9D", "FormDataStr",
        gb2312Encode);
  }

  public static String callVpnOrNot(VpnCallBack vpnCallBack) throws IOException {
    PrefDao prefDao = new PrefDao(App.cxt);
    String post = null;
    if (App.isUseVpn) {
      logOnVpn();
      vpnCallBack.getByVpn();
      boolean login = SystemLogin.login(prefDao.getSystemId(), prefDao.getSystemPwd());
      SystemLogin.logOff();
      logOutVpn();
    } else {
      boolean login = SystemLogin.login(prefDao.getSystemId(), prefDao.getSystemPwd());
      post = vpnCallBack.getByNoVpn();
    }
    return post;
  }

  public interface VpnCallBack {
    String getByVpn() throws IOException;

    String getByNoVpn() throws IOException;
  }
}
