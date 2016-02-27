package com.xmpp.chat.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.xmpp.chat.dao.StatusItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {
	
	public static String getPassword(Context paramContext) {
		return getPrefs(paramContext).getString("password", null);
	}
  
	public static void setPassword(Context paramContext, String paramString) {
		getPrefs(paramContext).edit().putString("password", paramString).commit();
	}

	private static SharedPreferences getPrefs(Context paramContext) {
		return PreferenceManager.getDefaultSharedPreferences(paramContext);
	}

	public static String getUser(Context context) {
		return getPrefs(context).getString("user", null);
	}

	public static void setUser(Context context, String paramString) {
		getPrefs(context).edit().putString("user", paramString).commit();
	}

	public static String getUserName(Context context) {
		return getPrefs(context).getString("username", null);
	}

	public static void setUserName(Context context, String paramString) {
		getPrefs(context).edit().putString("username", paramString).commit();
	}

	public static long getGroupLastRead(Context context, String chatItem) {
		return getPrefs(context).getLong("grouplastread_" + chatItem, 0);
	}

	public static void setGroupLastRead(Context context, String chatItem, long lastread) {
		getPrefs(context).edit().putLong("grouplastread_" + chatItem, lastread).commit();
	}
	
	public static StatusItem getStatus(Context context) {
		
		StatusItem result = new StatusItem();
		String status = getPrefs(context).getString("status", null);
		if(status==null)
		{
			return result;
		}
		JSONObject json;
		try {
			json = new JSONObject(status);
			result.status = json.getString("status");
			result.mood = json.getInt("mood");
		} catch (JSONException e) {
		}
		return result;
	}

	public static void setStatus(Context context, StatusItem status) {
		if(status==null)
		{
			getPrefs(context).edit().putString("status", null).commit();
		}
		JSONObject json = new JSONObject();
		try {
			json.put("status", status.status);
			json.put("mood", status.mood);
			getPrefs(context).edit().putString("status", json.toString()).commit();
		} catch (JSONException e) {
			getPrefs(context).edit().putString("status", null).commit();
		}		
	}

	
	public static String getUserHash(Context context) {
		return getPrefs(context).getString("userhash", null);
	}

	public static void setUserHash(Context context, String hash) {
		getPrefs(context).edit().putString("userhash", hash).commit();
	}

	public static String getSaveMessage(Context context,String user) {
		return getPrefs(context).getString("savemessage_"+user, "");
	}

	public static void setSaveMessage(Context context, String user, String message) {
		getPrefs(context).edit().putString("savemessage_"+user, message).commit();
	}
}
