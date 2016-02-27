package com.xmpp.chat.dao;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;

@DatabaseTable
public class ContactItem {
	@DatabaseField
	public boolean isRegistered;

	@DatabaseField
	public boolean isShowHome;

	@DatabaseField(id = true)
	public String username;

	@DatabaseField
	public String displayName;

	@DatabaseField
	public String statusJson = "";

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	public byte[] imageByte;

	@DatabaseField
	public String status = "";

	@DatabaseField
	public int mood = -1;

	@DatabaseField
	public boolean anonymous = false;

	@DatabaseField
	public String onlineStatus = "";

	public Bitmap imageBitmap;

	@DatabaseField
	public boolean blocked;

	public String toString() {
		return username;
	}
}