package com.xmpp.chat.xmpp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.AppSettings;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class XMPP {

    public static String HOST1 = "192.168.0.233";
    public static String HOST = "192.168.0.233";


    public static final String LIVESERVICE = "liveappgroup@liveapp." + HOST;

    public static final int PORT = 5222;
    private static XMPP instance;
    private XMPPTCPConnection connection;
    private static String TAG = "SAMPLE-XMPP";

    private XMPPTCPConnectionConfiguration buildConfiguration() throws XmppStringprepException {
        XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder();


        builder.setHost(HOST1);
        builder.setPort(PORT);
        builder.setCompressionEnabled(false);
        builder.setDebuggerEnabled(true);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setSendPresence(true);
        if (Build.VERSION.SDK_INT >= 14) {
            builder.setKeystoreType("AndroidCAStore");
            // config.setTruststorePassword(null);
            builder.setKeystorePath(null);
        } else {
            builder.setKeystoreType("BKS");
            String str = System.getProperty("javax.net.ssl.trustStore");
            if (str == null) {
                str = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security"
                        + File.separator + "cacerts.bks";
            }
            builder.setKeystorePath(str);
        }
        DomainBareJid serviceName = JidCreate.domainBareFrom(HOST);
        builder.setServiceName(serviceName);


        return builder.build();
    }

    private XMPPTCPConnection connect() throws XMPPException, SmackException, IOException, InterruptedException {
        Log.i(TAG, "Getting XMPP Connect");
        if ((this.connection != null) && (this.connection.isConnected())) {
            Log.i(TAG, "Returning already existing connection");
            return this.connection;
        }
        Log.i(TAG, "Connection not found, creating new one");
        long l = System.currentTimeMillis();
        XMPPTCPConnectionConfiguration config = buildConfiguration();
        SmackConfiguration.DEBUG = true;

        if (connection == null) {
            this.connection = new XMPPTCPConnection(config);
        }
        this.connection.connect();
//        Roster roster = Roster.getInstanceFor(connection);

//        if (!roster.isLoaded())
//            roster.reloadAndWait();
        Log.i(TAG, "Connection Properties: " + connection.getHost() + " " + connection.getServiceName());
        Log.i(TAG, "Time taken in first time connect: " + (System.currentTimeMillis() - l));
        Roster roster = Roster.getInstanceFor(connection);

        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {
                Log.d("deb", "ug");
            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {
                Log.d("deb", "ug");
            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {
                Log.d("deb", "ug");
            }

            @Override
            public void presenceChanged(Presence presence) {
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
        Log.i(TAG, "inside XMPP close method");
        if (this.connection != null) {
            this.connection.disconnect();
        }
        instance = null;
    }

    public XMPPTCPConnection getConnection(Context context) {
        Log.i(TAG, "Inside getConnection");
        if ((this.connection == null) || (!this.connection.isConnected())) {
            try {
                this.connection = connect();
                this.connection.login(AppSettings.getUser(context),
                        AppSettings.getPassword(context));
                Log.i(TAG, "inside XMPP getConnection method after login");
                context.sendBroadcast(new Intent("liveapp.loggedin"));
            } catch (XMPPException localXMPPException) {
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Inside getConnection - Returning connection");
        return connection;
    }

    public Roster getRoster() throws XMPPException {
        XMPPTCPConnection connection = null;
        Roster roster = null;
        try {
            connection = connect();
            roster = Roster.getInstanceFor(connection);
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return roster;
    }


    public boolean isConnected() {
        return (this.connection != null) && (this.connection.isConnected());
    }

    public void login(String user, String pass, StatusItem status, String username)
            throws XMPPException, SmackException, IOException, InterruptedException {
        Log.i(TAG, "inside XMPP getlogin Method");
        long l = System.currentTimeMillis();
        XMPPTCPConnection connect = connect();
        if (connect.isAuthenticated()) {
            Log.i(TAG, "User already logged in");
            return;
        }

        Log.i(TAG, "Time taken to connect: " + (System.currentTimeMillis() - l));

        l = System.currentTimeMillis();
        connect.login(user, pass);
        Log.i(TAG, "Time taken to login: " + (System.currentTimeMillis() - l));

        Log.i(TAG, "login step passed");

        Presence p = new Presence(Presence.Type.available);
        p.setMode(Presence.Mode.available);
        p.setPriority(24);
        p.setFrom(connect.getUser());
        if (status != null) {
            p.setStatus(status.toJSON());
        } else {
            p.setStatus(new StatusItem().toJSON());
        }
//        p.setTo("");
        VCard ownVCard = new VCard();
        ownVCard.load(connect);
        ownVCard.setNickName(username);
        ownVCard.save(connect);

        PingManager pingManager = PingManager.getInstanceFor(connect);
        pingManager.setPingInterval(150000);
        connect.sendPacket(p);


    }

    public void register(String user, String pass) throws XMPPException, SmackException.NoResponseException, SmackException.NotConnectedException {
        Log.i(TAG, "inside XMPP register method, " + user + " : " + pass);
        long l = System.currentTimeMillis();
        try {
            AccountManager accountManager = AccountManager.getInstance(connect());
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(user), pass);
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Time taken to register: " + (System.currentTimeMillis() - l));
    }
}