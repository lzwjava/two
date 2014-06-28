package com.lzw.chat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lzw.chat.R;

/**
 * Created by lzw on 14-6-29.
 */
public class SettingFragment extends Fragment {
  View personInfo;
  Activity cxt;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.setting_layout, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    cxt = getActivity();
    findView();
  }

  private void findView() {
    personInfo = cxt.findViewById(R.id.personInfo);
  }
}
