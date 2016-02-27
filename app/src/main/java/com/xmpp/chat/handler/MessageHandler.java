package com.xmpp.chat.handler;

import java.util.Calendar;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.delay.packet.DelayInfo;

import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.framework.Notifications;
import com.xmpp.chat.xmpp.XMPP;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.util.Log;

public class MessageHandler implements PacketListener {
	Context context;
	DatabaseHelper db;

	public MessageHandler(Context paramContext) {
		this.context = paramContext;
		this.db = DatabaseHelper.getInstance(paramContext);
	}

	public void processPacket(Packet paramPacket) {
		MessageItem messageItem = null;

		if ((paramPacket instanceof Message)) {

			Message localMessage = (Message) paramPacket;
			if (localMessage.getFrom().equals(XMPP.LIVESERVICE)) {
				return;
			}

			if ((localMessage.getBody() != null)
					&& (!localMessage.getBody().equals(""))) {

				messageItem = new MessageItem();
				messageItem.opponent = StringUtils.parseName(localMessage
						.getFrom());
				messageItem.timestamp = Calendar.getInstance()
						.getTimeInMillis();
				messageItem.message = localMessage.getBody();
				messageItem.isNewMessage = true;

				boolean newMessage = true;
				for (PacketExtension ext : localMessage.getExtensions()) {
					if (ext instanceof DelayInfo) {
						newMessage = false;
						DelayInfo delay = (DelayInfo) ext;
					}
				}
				// if (!newMessage) {
				// return;
				// }

				// if (localMessage.getBody().equals("ANONYMOUS REQUEST")) {
				// ContactItem contact = new ContactItem();
				// contact.username =
				// StringUtils.parseName(localMessage.getFrom());
				// VCard card = new VCard();
				// try {
				// card.load(XMPP.getInstance().getConnection(context),
				// StringUtils.parseBareAddress(localMessage.getFrom()));
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// if (card == null || card.getNickName() == null) {
				// contact.displayName = "Anonymous user";
				// } else {
				// contact.displayName = card.getNickName();
				// }
				// contact.isShowHome = true;
				// contact.isRegistered = true;
				// contact.anonymous = true;
				// DatabaseHelper.getInstance(context).updateContact(contact);
				// // return;
				// }
				// boolean messageNew = true;
				for (PacketExtension ext : ((Message) paramPacket)
						.getExtensions()) {
					if (ext instanceof DelayInfo) {
						newMessage = false;
						DelayInfo delay = (DelayInfo) ext;
					}
				}
				if (!newMessage) {
					return;
				}
				// }

				if (!isAppRunning(context)) {

					Notifications.showIncomingMessageNotification(
							context,
							localMessage.getType() == Type.groupchat,
							localMessage.getFrom(),
							messageItem.message,
							DatabaseHelper.getInstance(context).getDisplayName(
									context,
									StringUtils.parseBareAddress(localMessage
											.getFrom())));
					Log.e("tag", "message is not AppRunning context chat"
							+ localMessage);

				} else {
					Log.e("tag", "message came isAppRunning context"
							+ localMessage + "," + messageItem.opponent);

				}

			}
		}
	}

	public static boolean isAppRunning(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Activity.ACTIVITY_SERVICE);
		// The first in the list of RunningTasks is always the foreground task.
		RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
		String foregroundTaskPackageName = foregroundTaskInfo.topActivity
				.getPackageName();
		return foregroundTaskPackageName.equals(context.getPackageName());
	}
}