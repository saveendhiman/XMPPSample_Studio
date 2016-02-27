package com.xmpp.chat.framework;

import java.util.HashSet;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.xmppsample.ActivityChatScreen;
import com.example.xmppsample.R;
import com.xmpp.chat.data.DatabaseHelper;

public class Notifications {

	private static Integer messageNumber = 0;
	private static HashSet<String> messagesFrom = new HashSet<String>();

	public static void cancelIncomingMesssageNotifications(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService("notification");
		nm.cancelAll();
		messageNumber = 0;
		messagesFrom.clear();
	}

	public static void showIncomingMessageNotification(Context context,
			boolean groupChat, String jid, String message, String displayName) {
		if (context == null)
			return;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean notification = prefs.getBoolean("notification", true);
		if (!notification) {
			return;
		}
		NotificationManager nm = (NotificationManager) context
				.getSystemService("notification");
		NotificationCompat.Builder localBuilder = new NotificationCompat.Builder(
				context);
		messagesFrom.add(StringUtils.parseName(jid));
		messageNumber++;
		if (messagesFrom.size() > 1) {
			localBuilder.setContentTitle(messageNumber + " messages from "
					+ messagesFrom.size() + " users");
		} else {
			if (groupChat) {
				String username = StringUtils.parseBareAddress(StringUtils
						.parseResource(jid));
				Log.e("taggi", "groupChat username :" + username);
				localBuilder.setContentTitle(username + " in group");
			} else {
				localBuilder.setContentTitle(displayName);
			}
		}
		localBuilder.setContentText(message);
		localBuilder.setSmallIcon(R.drawable.ic_launcher);
		localBuilder.setLargeIcon(BitmapFactory.decodeResource(
				context.getResources(), R.drawable.ic_launcher));
		localBuilder.setNumber(messageNumber);
		int defaults = 0;
		boolean sound = prefs.getBoolean("notificationsound", true);
		boolean vibrate = prefs.getBoolean("notificationvibrate", true);
		if (sound) {
			defaults |= Notification.DEFAULT_SOUND;
		}
		if (vibrate) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		defaults |= Notification.DEFAULT_LIGHTS;
		localBuilder.setDefaults(defaults);

		if (groupChat) {

		} else {
			Intent notificationIntent = new Intent(context,
					ActivityChatScreen.class);

			if (messagesFrom.size() == 1) {
				notificationIntent.putExtra("id", jid);
				Log.e("tag", "notification jid :" + jid);
				notificationIntent.putExtra(
						"username",
						DatabaseHelper.getInstance(context).getDisplayName(
								context, StringUtils.parseBareAddress(jid)));
			}
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			localBuilder.setContentIntent(contentIntent);
		}

		nm.notify(10201, localBuilder.build());
	}

}
