package com.lzw.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.lzw.chat.R;
import com.lzw.chat.entity.ChatMsgEntity;
import com.lzw.chat.util.Logger;

import java.util.List;

public class ChatMsgViewAdapter extends BaseAdapter {
	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

  View.OnClickListener onClickListener;
	private List<ChatMsgEntity> datas;

	private Context ctx;

	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> datas) {
		ctx = context;
		this.datas = datas;
		mInflater = LayoutInflater.from(context);
	}

  public void setDatas(List<ChatMsgEntity> datas) {
    this.datas = datas;
  }

  public void setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
  }

  public int getCount() {
		return datas.size();
	}

	public Object getItem(int position) {
		return datas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		ChatMsgEntity entity = datas.get(position);

		if (entity.getMsgType()) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = datas.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left,
						null);
			} else {
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right,
						null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
      viewHolder.voiceImgView=convertView.findViewById(R.id.voiceImg);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
		viewHolder.tvContent.setText(entity.getText());
    viewHolder.tvContent.setTag(viewHolder);
    viewHolder.msg =entity;
    if(entity.isText()){
      viewHolder.tvContent.setOnClickListener(null);
      viewHolder.voiceImgView.setVisibility(View.GONE);
    }else{
      int emptyN=0;
      emptyN=entity.getLength();
      if(emptyN>50){
        emptyN=50;
      }
      String empty="";
      for(int i=0;i<emptyN;i++){
        empty+="  ";
      }
      Logger.d("len"+emptyN);
      viewHolder.tvContent.setText(empty);
      viewHolder.tvContent.setOnClickListener(onClickListener);
      viewHolder.voiceImgView.setVisibility(View.VISIBLE);
    }
		return convertView;
	}

	public static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public boolean isComMsg = true;
    public View voiceImgView;
    public ChatMsgEntity msg;
  }
}
