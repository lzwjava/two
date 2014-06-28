package com.lzw.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import com.lzw.chat.R;
import com.lzw.chat.service.PersonInfoService;
import com.lzw.commons.Logger;
import com.lzw.commons.NetAsyncTask;

/**
 * Created by lzw on 14-6-29.
 */
public class PersonInfoActivity extends Activity {
  String hometown, name, year, class1;
  TextView infoView;
  Activity cxt;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    setContentView(R.layout.person_info_layout);
    findView();
    new GetDataTask(cxt).execute();
  }

  private void findView() {
    infoView = (TextView) findViewById(R.id.infoView);
  }

  class GetDataTask extends NetAsyncTask {

    protected GetDataTask(Context cxt) {
      super(cxt);
    }

    @Override
    protected void doInBack() throws Exception {
      String html=PersonInfoService.getPersonInfoHtml();
      Logger.d("html"+html);
    }

    @Override
    protected void onPost(boolean res) {
      if(res){

      }
    }
  }
}
