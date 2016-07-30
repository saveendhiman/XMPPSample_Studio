package com.xmpp.chat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xmpp.chat.dao.EmojiItem;

import java.util.ArrayList;

public class SettingsUtil {


    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static ArrayList<Integer> getHistoryItems(Context context) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        String emojis = getPrefs(context).getString("emojis", "");
        if (emojis.equals("")) {
            return result;
        }
        String[] ids = emojis.split(",");
        for (int i = 0; i < ids.length; i++) {
            result.add(Integer.parseInt(ids[i]));
        }
        return result;
    }

    public static void setHistoryItems(Context context, ArrayList<EmojiItem> history) {
        StringBuilder builder = new StringBuilder(100);
        for (int i = 0; i < 20; i++) {
            if (history.size() >= i)
                break;
            builder.append(history.get(i).id).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        getPrefs(context).edit().putString("emojis", builder.toString()).commit();
    }

    public static long getLastHistory(Context context, String jid, String lastJoinTime) {
        return getPrefs(context).getLong("lasthistory_" + jid, Long.parseLong(lastJoinTime));
    }

    //	StringUtils.parseBareAddress(jid)
    //edited by saveen 12-july-2016
    public static void setLastHistory(Context context, String jid) {
        getPrefs(context).edit().putLong("lasthistory_" + jid, System.currentTimeMillis()).commit();
    }


    public static boolean isSlideshowShown(Context context) {
        return getPrefs(context).getBoolean("slideshown", false);
    }

    public static void setSlideshowShown(Context context) {
        getPrefs(context).edit().putBoolean("slideshown", true).commit();
    }

    public static boolean isUserBlocked(Context context, String jid) {
        return getPrefs(context).getBoolean("blocked_" + jid, false);
    }

    public static void setUserBlocked(Context context, String jid, boolean value) {
        getPrefs(context).edit().putBoolean("blocked_" + jid, value).commit();
    }
}
