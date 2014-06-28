package com.lzw.chat.service;

import com.lzw.chat.base.App;
import com.lzw.chat.utils.VpnUtils;
import com.lzw.commons.HttpUtils;

import java.io.IOException;

/**
 * Created by lzw on 14-6-29.
 */
public class PersonInfoService {
  public static String getPersonInfoHtml() throws IOException {
    return VpnUtils.callVpnOrNot(new VpnUtils.VpnCallBack() {
      @Override
      public String getByVpn() throws IOException {
        return HttpUtils.post(App.client, "https://vpn.bjfu.edu.cn/jwxt/Student/," +
            "DanaInfo=jwxt.bjfu.edu.cn+StudentUserInfo.asp");
      }

      @Override
      public String getByNoVpn() throws IOException {
        return HttpUtils.post(App.client, "http://jwxt.bjfu.edu.cn/jwxt/Student/StudentUserInfo.asp");
      }
    });
  }
}
