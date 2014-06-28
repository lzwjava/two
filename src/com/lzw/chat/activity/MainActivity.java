package com.lzw.chat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.lzw.chat.R;

/**
 * Created by lzw on 14-6-28.
 */
public class MainActivity extends Activity implements View.OnClickListener {
  private Context cxt;
  View mainLayout;
  View[] tabButtons;
  int[] selectedDrawable;
  int[] normalDrawable;
  int[] btnIds;
  public static final int TAB_N = 4;
  ChatPairFragment chatPairFragment;
  SettingFragment settingFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    setContentView(R.layout.main_layout);
    findView();
    initData();
  }

  private void initData() {
    tabButtons = new Button[TAB_N];
    selectedDrawable = new int[]{R.drawable.message_selected,
        R.drawable.linkman_selected, R.drawable.dynamic_selected,
        R.drawable.set_selected};
    normalDrawable = new int[]{R.drawable.message, R.drawable.linkman,
        R.drawable.dynamic, R.drawable.set};
    btnIds = new int[]{
        R.id.tabMsg, R.id.tabFriends, R.id.tabdynamic, R.id.tabset};
    for (int i = 0; i < TAB_N; i++) {
      tabButtons[i] = findViewById(btnIds[i]);
      tabButtons[i].setOnClickListener(new TabBtnClickListener());
    }
  }

  private void findView() {
    mainLayout = findViewById(R.id.mainLayout);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
  }

  private class TabBtnClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      int id = v.getId();
      int p;
      for (p = 0; p < TAB_N; p++) {
        if (btnIds[p] == id) {
          break;
        }
      }
      setAllTabNormal();
      tabButtons[p].setBackgroundResource(selectedDrawable[p]);
      FragmentTransaction trans = getFragmentManager().beginTransaction();
      hideFragments(trans);
      if (id == R.id.tabdynamic) {
        if (chatPairFragment == null) {
          chatPairFragment = new ChatPairFragment();
          trans.add(R.id.mainLayout, chatPairFragment);
        } else {
          trans.show(chatPairFragment);
        }
      } else if (id == R.id.tabset) {
        if (settingFragment == null) {
          settingFragment = new SettingFragment();
          trans.add(R.id.mainLayout, settingFragment);
        } else {
          trans.show(settingFragment);
        }
      }
      trans.commit();
    }
  }

  private void hideFragments(FragmentTransaction trans) {
    Fragment[] fragments = new Fragment[]{chatPairFragment};
    for (int i = 0; i < fragments.length; i++) {
      if (fragments[i] != null) {
        trans.hide(fragments[i]);
      }
    }
  }

  private void setAllTabNormal() {
    for (int i = 0; i < TAB_N; i++) {
      tabButtons[i].setBackgroundResource(normalDrawable[i]);
    }
  }
}
