package com.xmpp.chat.dao;

import org.json.JSONException;
import org.json.JSONObject;

public class StatusItem {

	public String status = "Using Live!";
	public int mood = 0;

	public static StatusItem fromJSON(String json) {
		StatusItem result = new StatusItem();
		if (json == null) {
			return result;
		}
		try {
			JSONObject js = new JSONObject(json);
			result.status = js.getString("status");
			result.mood = js.getInt("mood");
		} catch (JSONException e) {
			result.status = json;
		}
		return result;
	}

	public String toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("status", status);
			json.put("mood", mood);
		} catch (JSONException e) {
		}
		return json.toString();
	}
}
