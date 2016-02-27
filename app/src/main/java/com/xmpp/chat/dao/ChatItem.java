package com.xmpp.chat.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;

@SuppressWarnings("serial")
@DatabaseTable
public class ChatItem implements Serializable {
	@DatabaseField
	public String groupID = "-1";
	// @DatabaseField
	// public String id;
	@DatabaseField
	public boolean isGroup = false;
	@DatabaseField(id = true)
	public String jid;
	@DatabaseField
	public String lastMessage = "";
	@DatabaseField
	public long lastMessageTimestamp = 0;
	@DatabaseField
	public String name;
	@DatabaseField
	public String displayName;
	@DatabaseField
	public String status;

	@DatabaseField
	public int chatcount;


	@DatabaseField
	public int chatcount_unreadmsges;

	@DatabaseField
	public String photo;

	@DatabaseField
	public String jointime = "0";

	@DatabaseField
	public boolean blocked;

	public long getJoinTime() {
		try {
			return Long.parseLong(jointime);
		} catch (Exception e) {
			return 0;
		}
	}

	public byte[] imageByte;

	public transient Bitmap imageBitmap;

	public boolean isNewMessages = false;

	public String typing = "";
	public boolean anonymous;
	public String thread;


}
