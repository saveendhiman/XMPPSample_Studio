package com.xmpp.chat.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;

@DatabaseTable
public class MessageItem {
	@DatabaseField(generatedId = true)
	public long id;
	@DatabaseField
	public String message;
	@DatabaseField(index = true)
	public String opponent;
	@DatabaseField
	public String groupSender;
	@DatabaseField
	public boolean outMessage;
	@DatabaseField
	public boolean outgoing;
	@DatabaseField
	public long timestamp = 0;
	@DatabaseField
	public boolean isNewMessage = false;
	@DatabaseField
	public String opponentDisplay;

	public boolean groupBell;

	@DatabaseField
	public String file;
	@DatabaseField
	public Boolean outFile = false;

	@DatabaseField
	public int progress;

	public transient Bitmap bitmap;
}