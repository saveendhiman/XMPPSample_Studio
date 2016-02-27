package com.xmpp.chat.adapter;

import org.jivesoftware.smack.packet.Message;

import com.xmpp.chat.xmpp.XMPP;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<Message> {
	public MessageAdapter(Context paramContext) {
		super(paramContext, 17367043);
	}

	public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		if (paramView == null) {
			paramView = LayoutInflater.from(getContext()).inflate(17367044, null);
		}
		((TextView) paramView.findViewById(16908308)).setText(((Message) getItem(paramInt)).getBody());
		TextView localTextView = (TextView) paramView.findViewById(16908309);
		if (((Message) getItem(paramInt)).getFrom().equals(XMPP.getInstance().getConnection(getContext()).getUser())) {
		}
		for (String str = "Outgoing";; str = "Incoming") {
			localTextView.setText(str);
			return paramView;
		}
	}
}
