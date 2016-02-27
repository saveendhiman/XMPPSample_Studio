package com.xmpp.chat.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;

import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.xmpp.XMPP;

import android.content.Context;
import android.text.format.DateFormat;

public class RosterManager {

	public static String getLastActivity(Context context, String jid, boolean showStatus) {
		try {
			LastActivity la = LastActivityManager.getInstanceFor(XMPP.getInstance().getConnection(context)).getLastActivity(jid);
			long seconds = la.lastActivity;
			if (seconds == 0) {
				if (showStatus) { 
					Iterator<Presence> pr = XMPP.getInstance().getConnection(context).getRoster().getPresences(jid).iterator();
					while (pr.hasNext()) {
						Presence p = ((Presence) pr.next());
						if (p.getType() == Presence.Type.available) {
							StatusItem statusItem = StatusItem.fromJSON(p.getStatus());
							String status = statusItem.status;
							if(statusItem.mood>0)
								DatabaseHelper.getInstance(context).updateContactStatus(jid, statusItem.status, statusItem.mood);
							if (status == null || status.equals("")) {
								return "Status not set";
							} 
							return status;
						}
					}
				}
				return "Online";
			}

			Calendar curCalendar = Calendar.getInstance();
			Calendar laCalendar = Calendar.getInstance();
			laCalendar.add(Calendar.SECOND, (int) -seconds);
			if (curCalendar.get(Calendar.DAY_OF_MONTH) != laCalendar.get(Calendar.DAY_OF_MONTH)) {
				return "Last online: " + new SimpleDateFormat("MM/dd").format(laCalendar.getTime());
			} else {
				return "Last online: " + DateFormat.getTimeFormat(context).format(laCalendar.getTime());
			}
			// int day = (int) TimeUnit.SECONDS.toDays(seconds);
			// long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
			// long minute = TimeUnit.SECONDS.toMinutes(seconds) -
			// (TimeUnit.SECONDS.toHours(seconds) * 60);
			// long second = TimeUnit.SECONDS.toSeconds(seconds) -
			// (TimeUnit.SECONDS.toMinutes(seconds) * 60);
		} catch (XMPPException e) {
			// e.printStackTrace();
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Using live App";
	}
}
