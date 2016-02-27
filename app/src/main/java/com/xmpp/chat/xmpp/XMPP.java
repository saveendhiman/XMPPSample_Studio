package com.xmpp.chat.xmpp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.AppSettings;

public class XMPP {

	public static String HOST1 = "192.168.0.233";
	public static String HOST = "192.168.0.233";

	public static final String LIVESERVICE = "liveappgroup@liveapp." + HOST;

	public static final int PORT = 5222;
	private static XMPP instance;
	private XMPPConnection connection;

	private XMPPConnection connect() throws XMPPException, SmackException,
			IOException {
		if ((this.connection != null) && (this.connection.isConnected())) {
			return this.connection;
		}

		ConnectionConfiguration config = new ConnectionConfiguration(HOST1,
				5222);
		SmackConfiguration.DEBUG_ENABLED = true;
		SASLAuthentication.supportSASLMechanism("MD5", 0);
		System.setProperty("smack.debugEnabled", "true");
		config.setCompressionEnabled(false);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		config.setReconnectionAllowed(true);
		config.setSendPresence(true);
		config.setRosterLoadedAtLogin(true);

		if (Build.VERSION.SDK_INT >= 14) {
			config.setKeystoreType("AndroidCAStore");
			config.setKeystorePath(null);
		} else {
			config.setKeystoreType("BKS");
			String str = System.getProperty("javax.net.ssl.trustStore");
			if (str == null) {
				str = System.getProperty("java.home") + File.separator + "etc"
						+ File.separator + "security" + File.separator
						+ "cacerts.bks";
			}
			config.setKeystorePath(str);
		}
		if (connection == null) {
			this.connection = new XMPPTCPConnection(config);
		}
		this.connection.connect();
		Roster roster = connection.getRoster();
		roster.addRosterListener(new RosterListener() {

			@Override
			public void presenceChanged(Presence arg0) {
				Log.d("deb", "ug");
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
				Log.d("deb", "ug");
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {
				Log.d("deb", "ug");
			}

			@Override
			public void entriesAdded(Collection<String> arg0) {
				Log.d("deb", "ug");
			}
		});
		return this.connection;
	}

	public static XMPP getInstance() {
		if (instance == null) {
			instance = new XMPP();
		}
		return instance;
	}

	public void close() {
		if (this.connection != null) {
			try {
				this.connection.disconnect();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}

	public XMPPConnection getConnection(Context context) {

		if ((this.connection == null) || (!this.connection.isConnected())) {
			try {
				connection = connect();
				try {
					connection.login(AppSettings.getUser(context),
							AppSettings.getPassword(context));
				} catch (Exception e) {
					e.printStackTrace();
				}

				context.sendBroadcast(new Intent("liveapp.loggedin"));

			} catch (XMPPException localXMPPException) {
			} catch (SaslException e) {
				e.printStackTrace();
			} catch (SmackException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	public Roster getRoaster() throws XMPPException {
		try {
			return connect().getRoster();
		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isConnected() {
		return (this.connection != null) && (this.connection.isConnected());
	}

	public void login(String user, String pass, StatusItem status,
			String username) throws XMPPException, SaslException,
			SmackException, IOException {

		XMPPConnection connect = connect();
		if (connect.isAuthenticated()) {
			return;
		}

		connect.login(user, pass);
		Presence p = new Presence(Presence.Type.available);
		p.setMode(Presence.Mode.available);
		p.setPriority(24);
		p.setFrom(connect.getUser());
		if (status != null) {
			p.setStatus(status.toJSON());
		} else {
			p.setStatus(new StatusItem().toJSON());
		}
		p.setTo("");
		VCard ownVCard = new VCard();
		ownVCard.load(connect);
		ownVCard.setNickName(username);
		ownVCard.save(connect);

		PingManager pingManager = PingManager.getInstanceFor(connect);
		pingManager.setPingInterval(150000);
		connect.sendPacket(p);

	}

	public void register(String user, String pass) throws XMPPException,
			NoResponseException, NotConnectedException {
		try {
			AccountManager.getInstance(connect()).createAccount(user, pass);
		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}