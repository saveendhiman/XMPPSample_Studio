package com.xmpp.chat.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.xmppsample.R;
import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.AppSettings;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.handler.MessageHandler;
import com.xmpp.chat.xmpp.XMPP;
//import com.crashlytics.android.Crashlytics;

public class LiveAppService extends Service {

	public static final String ACTION_LOGGED_IN = "liveapp.loggedin";
	private final LocalBinder binder = new LocalBinder();
	DatabaseHelper db;
	XMPP xmppConnection;

	// ***********************//

	private void onLoggedIn() {
		FromMatchesFilter localFromMatchesFilter = new FromMatchesFilter(
				"livegroup@liveapp." + XMPP.HOST, false);
		XMPP.getInstance().getConnection(this)
				.addPacketListener(new MessageHandler(this), null);
		ChatManager.getInstanceFor(XMPP.getInstance().getConnection(this))
				.addChatListener(new ChatManagerListener() {
					public void chatCreated(Chat paramAnonymousChat,
							boolean paramAnonymousBoolean) {
					}
				});

		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(XMPP.getInstance().getConnection(this));
		sdm.addFeature("jabber.org/protocol/si");
		sdm.addFeature("http://jabber.org/protocol/si");
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("jabber:iq:privacy");

		FileTransferNegotiator.setServiceEnabled(XMPP.getInstance()
				.getConnection(this), true);
		FileTransferNegotiator.IBB_ONLY = true;

		final Roster roster = XMPP.getInstance().getConnection(this)
				.getRoster();
		roster.addRosterListener(new RosterListener() {

			@Override
			public void presenceChanged(Presence pr) {
				if (pr.isAvailable()) {
					StatusItem status = StatusItem.fromJSON(pr.getStatus());
					DatabaseHelper.getInstance(LiveAppService.this)
							.updateContactStatus(pr.getFrom(), status.status,
									status.mood);
				}
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
				Log.d("LOL", "Test");
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {

			}

			@Override
			public void entriesAdded(Collection<String> arg0) {
				for (String jid : arg0) {
					Iterator<Presence> pr = XMPP.getInstance()
							.getConnection(LiveAppService.this).getRoster()
							.getPresences(jid).iterator();
					while (pr.hasNext()) {
						Presence p = ((Presence) pr.next());
						if (p.getType() == Presence.Type.available) {
							StatusItem statusItem = StatusItem.fromJSON(p
									.getStatus());
							String status = statusItem.status;
							if (statusItem.mood > 0)
								DatabaseHelper.getInstance(LiveAppService.this)
										.updateContactStatus(jid,
												statusItem.status,
												statusItem.mood);
						}
					}
				}
				Log.d("LOL", "Test");
			}
		});

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				Collection<RosterEntry> entries = roster.getEntries();
				for (RosterEntry entry : entries) {
					if (entry.getType() != ItemType.to) {
						Presence sub = new Presence(Type.subscribe);
						sub.setFrom(XMPP.getInstance()
								.getConnection(LiveAppService.this).getUser());
						sub.setTo(entry.getUser());
						try {
							XMPP.getInstance()
									.getConnection(LiveAppService.this)
									.sendPacket(sub);
						} catch (NotConnectedException e) {
						}
					} else {
						Presence pr = roster.getPresence(entry.getUser());
						if (pr.isAvailable()) {
							StatusItem status = StatusItem.fromJSON(pr
									.getStatus());
							DatabaseHelper.getInstance(LiveAppService.this)
									.updateContactStatus(pr.getFrom(),
											status.status, status.mood);
						}
					}
				}

			}
		}, 15000);
	}

	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (LiveAppService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public XMPP XMPP() {
		return this.xmppConnection;
	}

	public IBinder onBind(Intent paramIntent) {
		return this.binder;
	}

	Handler holdConnectionHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if (XMPP.getInstance().getConnection(LiveAppService.this) == null
					|| !XMPP.getInstance().getConnection(LiveAppService.this)
							.isConnected()) {
				final String user = AppSettings.getUser(LiveAppService.this);
				final String pass = AppSettings
						.getPassword(LiveAppService.this);
				final String username = AppSettings
						.getUserName(LiveAppService.this);
				final StatusItem status = AppSettings
						.getStatus(LiveAppService.this);
				try {
					XMPP.getInstance().login(user, pass, status, username);
				} catch (SaslException e) {
					e.printStackTrace();
				} catch (XMPPException e) {
					e.printStackTrace();
				} catch (SmackException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			holdConnectionHandler.sendEmptyMessageDelayed(0, 60 * 1000);
		};
	};

	public void onCreate() {
		super.onCreate();
		this.xmppConnection = XMPP.getInstance();
		this.db = DatabaseHelper.getInstance(this);
		holdConnectionHandler.sendEmptyMessage(0);
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				stopService(new Intent(LiveAppService.this, FakeService.class));
			}
		}, 500);

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context paramAnonymousContext,
					Intent paramAnonymousIntent) {
				LiveAppService.this.onLoggedIn();
			}
		}, new IntentFilter("liveapp.loggedin"));

		if ((AppSettings.getUser(this) != null)
				&& (AppSettings.getPassword(this) != null)) {
			final String user = AppSettings.getUser(this);
			final String pass = AppSettings.getPassword(this);
			final String username = AppSettings
					.getUserName(LiveAppService.this);
			final StatusItem status = AppSettings.getStatus(this);
			new Thread(new Runnable() {
				public void run() {
					try {
						LiveAppService.this.xmppConnection.login(user, pass,
								status, username);
					} catch (XMPPException e) {
						e.printStackTrace();
					} catch (SaslException e) {
						e.printStackTrace();
					} catch (SmackException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					LiveAppService.this.sendBroadcast(new Intent(
							"liveapp.loggedin"));
					return;
				}
			}).start();
		}

	}

	public class LocalBinder extends Binder {
		public LocalBinder() {
		}

		public LiveAppService getService() {
			return LiveAppService.this;
		}
	}
}
