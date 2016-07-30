package com.xmpp.chat.service;

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
import android.util.Log;

import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.AppSettings;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.handler.MessageHandler;
import com.xmpp.chat.xmpp.XMPP;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

//import com.crashlytics.android.Crashlytics;

public class LiveAppService extends Service {

    public static final String ACTION_LOGGED_IN = "liveapp.loggedin";
    private final LocalBinder binder = new LocalBinder();
    DatabaseHelper db;
    XMPP xmppConnection;

    // ***********************//

    private void onLoggedIn() {
        XMPPTCPConnection connection = XMPP.getInstance().getConnection(this);
        FromMatchesFilter localFromMatchesFilter = null;
        try {
            localFromMatchesFilter = new FromMatchesFilter(JidCreate.domainBareFrom(Domainpart.from("livegroup@liveapp." + XMPP.HOST)), false);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
//        connection.addAsyncStanzaListener(new MessageHandler(this), localFromMatchesFilter);
        connection.addAsyncStanzaListener(new MessageHandler(this), null);

        ChatManager.getInstanceFor(connection)
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


//        FileTransferNegotiator.setServiceEnabled(XMPP.getInstance().getConnection(this), true);
        if (FileTransferNegotiator.isServiceEnabled(connection)) {
            Log.i("Kitean", "File Transfer Service is enabled");
        }
        FileTransferNegotiator.IBB_ONLY = true;


//        final Roster roster = Roster.getInstanceFor(connection);
//        roster.addRosterListener(new RosterListener() {
//
//            @Override
//            public void entriesAdded(Collection<Jid> addresses) {
//                for (Jid jid : addresses) {
//
//                    try {
//                        List<Presence> prs = XMPP.getInstance().getRoster().getPresences((BareJid) jid);
//                        Iterator<Presence> pr = prs.iterator();
//                        while (pr.hasNext()) {
////                            Presence p = ((Presence) pr.next());
////                            if (p.getType() == Type.available) {
////                                StatusItem statusItem = StatusItem.fromJSON(p
////                                        .getStatus());
////                                String status = statusItem.status;
////                                if (statusItem.mood > 0)
////                                    DatabaseHelper.getInstance(LiveAppService.this)
////                                            .updateContactStatus(jid.getLocalpartOrNull().toString(),
////                                                    statusItem.status,
////                                                    statusItem.mood);
////                            }
//                        }
//                    } catch (XMPPException e) {
//                        e.printStackTrace();
//                    }
////                            .getConnection(LiveAppService.this)).getRoster()
//
//
//                }
//                Log.d("LOL", "Test");
//            }
//
//            @Override
//            public void entriesUpdated(Collection<Jid> addresses) {
//                Log.d("LOL", "Test");
//            }
//
//            @Override
//            public void entriesDeleted(Collection<Jid> addresses) {
//
//            }
//
//            @Override
//            public void presenceChanged(Presence pr) {
//                if (pr.isAvailable()) {
////                    StatusItem status = StatusItem.fromJSON(pr.getStatus());
////                    DatabaseHelper.getInstance(LiveAppService.this)
////                            .updateContactStatus(pr.getFrom().getLocalpartOrNull().toString(), status.status,
////                                    status.mood);
//                }
//            }
//
//        });
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//
//                Collection<RosterEntry> entries = roster.getEntries();
//                for (RosterEntry entry : entries) {
//                    if (entry.getType() != RosterPacket.ItemType.to) {
//                        Presence sub = new Presence(Type.subscribe);
//                        sub.setFrom(XMPP.getInstance()
//                                .getConnection(LiveAppService.this).getUser());
//                        sub.setTo(entry.getJid());
//                        try {
//                            XMPP.getInstance()
//                                    .getConnection(LiveAppService.this)
//                                    .sendStanza(sub);
//                        } catch (NotConnectedException e) {
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        Presence pr = null;
//                        try {
//                            pr = roster.getPresence(JidCreate.bareFrom(entry.getUser()));
//                        } catch (XmppStringprepException e) {
//                            e.printStackTrace();
//                        }
//                        if (pr.isAvailable()) {
////                            StatusItem status = StatusItem.fromJSON(pr
////                                    .getStatus());
////                            DatabaseHelper.getInstance(LiveAppService.this)
////                                    .updateContactStatus(pr.getFrom().getLocalpartOrNull().toString(),
////                                            status.status, status.mood);
//                        }
//                    }
//                }
//
//            }
//        }, 15000);
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
            Log.i("Kitean", "Service Handler Running");
            XMPPTCPConnection connection = XMPP.getInstance().getConnection(LiveAppService.this);
            if (connection == null || !connection.isConnected()) {
                Log.i("Kitean", "Service Handler Running, inside if in case of no connection");
                final String user = AppSettings.getUser(LiveAppService.this);
                Log.i("Kitean", "Service Handler Running, user: " + user);
                final String pass = AppSettings
                        .getPassword(LiveAppService.this);
                Log.i("Kitean", "Service Handler Running, password: " + pass);
                final String username = AppSettings
                        .getUserName(LiveAppService.this);
                final StatusItem status = AppSettings
                        .getStatus(LiveAppService.this);
                Log.i("Kitean", "Service Handler Running, username: " + username);

                try {
                    if (connection != null) {
                        if (connection.isAuthenticated()) {
                            Log.i("Kitean", "inside service handler, already authenticated");
                        } else {
                            Log.i("Kitean", "inside service handler, not authenticated, will try to login");
                            XMPP.getInstance().login(user, pass, status, username);
                        }
                    } else {
                        Log.i("Kitean", "inside service handler, connection is null, trying to login");
                        XMPP.getInstance().login(user, pass, status, username);
                    }
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("Kitean", "Service Handler Running, connection already found");
            }
            holdConnectionHandler.sendEmptyMessageDelayed(0, 10 * 1000);
        }

        ;
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
                        XMPPTCPConnection connection = LiveAppService.this.xmppConnection.getConnection(LiveAppService.this);
                        if (connection == null || !connection.isAuthenticated()) {
                            xmppConnection.login(user, pass,
                                    status, username);
                        }
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
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