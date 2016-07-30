package com.xmpp.chat.handler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.util.Log;

import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.framework.Notifications;
import com.xmpp.chat.xmpp.XMPP;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import java.util.Calendar;
import java.util.List;

public class MessageHandler implements StanzaListener {
    Context context;
    DatabaseHelper db;

    public MessageHandler(Context paramContext) {
        this.context = paramContext;
        this.db = DatabaseHelper.getInstance(paramContext);
    }

    public void processPacket(Stanza paramPacket) {
        MessageItem messageItem = null;

        if ((paramPacket instanceof Message)) {
            Message localMessage = (Message) paramPacket;
            if (localMessage.getFrom().equals(XMPP.LIVESERVICE)) {
                return;
            }

            if ((localMessage.getBody() != null)
                    && (!localMessage.getBody().equals(""))) {

                messageItem = new MessageItem();
                messageItem.opponent = StringUtils.maybeToString(localMessage.getFrom().getLocalpartOrNull());
                messageItem.timestamp = Calendar.getInstance()
                        .getTimeInMillis();
                messageItem.message = localMessage.getBody();
                messageItem.isNewMessage = true;

                boolean newMessage = true;
                List<ExtensionElement> extnElements = localMessage.getExtensions();
                for (ExtensionElement ext : extnElements) {
                    if (ext instanceof DelayInformation) {
                        newMessage = false;
                        DelayInformation delay = (DelayInformation) ext;
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
                List<ExtensionElement> paramPacketExtension = paramPacket.getExtensions();
                for (ExtensionElement ext : ((Message) paramPacket)
                        .getExtensions()) {
                    if (ext instanceof DelayInformation) {
                        newMessage = false;
                        DelayInformation delay = (DelayInformation) ext;
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
//                            localMessage.getFrom(),
                            StringUtils.maybeToString(localMessage.getFrom().getLocalpartOrNull()),
                            messageItem.message,
                            DatabaseHelper.getInstance(context).getDisplayName(
                                    context,
                                    StringUtils.maybeToString(localMessage.getFrom().getLocalpartOrNull())));

//                    StringUtils.maybeToString(localMessage.getFrom().getLocalpartOrNull()),
////                                    StringUtils.parseBareAddress(localMessage.getBody()), StringUtils.parseBareAddress(localMessage.getFrom()));
//                            localMessage.getBody(), StringUtils.maybeToString(localMessage.getFrom().getLocalpartOrNull()));

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