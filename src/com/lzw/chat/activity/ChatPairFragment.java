package com.lzw.chat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.lzw.chat.R;
import com.lzw.chat.service.PairQuery;
import com.lzw.commons.Utils;


/**
 * Created by lzw on 14-5-22.
 */
public class ChatPairFragment extends Fragment implements View.OnClickListener {
  public static final int PREPARE_PAIR = 0;
  private static final int PAIRING = 1;
  LinearLayout pairingLayout, pairToLayout;
  View cancelPairBtn;
  Activity cxt;
  PairQuery pairQuery;
  int status;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.chat_pair_layout, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    cxt = getActivity();
    findView();
    setPreparePairLayout();
  }

  private void findView() {
    pairingLayout = (LinearLayout) cxt.findViewById(R.id.pairingLayout);
    pairToLayout = (LinearLayout) cxt.findViewById(R.id.pairToLayout);
    cancelPairBtn = cxt.findViewById(R.id.cancelPairBtn);
    pairToLayout.setOnClickListener(this);
    cancelPairBtn.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.cancelPairBtn) {
      if (pairQuery != null) {
        pairQuery.cancel();
      }
      setPreparePairLayout();
    } else if (id == R.id.pairToLayout) {
      pairQuery = new PairQuery(cxt);
      pairQuery.findInBackgroud(new PairQuery.PairCallBack() {

        @Override
        public void done(String pairedUser, Exception e) {
          if (e == null) {
            Intent intent = new Intent(cxt, ChatActivity.class);
            intent.putExtra(PairQuery.PAIRED_USER, pairedUser);
            startActivity(intent);
            Utils.toast(cxt, R.string.pair_succeed);
          } else {
            Utils.toast(cxt, R.string.cannot_pair_succeed);
            e.printStackTrace();
          }
          setPreparePairLayout();
          pairQuery.finishQuery();
        }
      });
      status = PAIRING;
      setLayoutVisiableByStatus();
    }
  }

  public void setPreparePairLayout() {
    status = PREPARE_PAIR;
    setLayoutVisiableByStatus();
  }

  private void setLayoutVisiableByStatus() {
    if (status == PREPARE_PAIR) {
      pairToLayout.setVisibility(View.VISIBLE);
      pairingLayout.setVisibility(View.GONE);
    } else if (status == PAIRING) {
      pairToLayout.setVisibility(View.GONE);
      pairingLayout.setVisibility(View.VISIBLE);
    }
  }
}
