package com.xmpp.chat.data;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.delay.packet.DelayInfo;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.j256.ormlite.stmt.QueryBuilder;
import com.xmpp.chat.dao.ChatItem;
import com.xmpp.chat.dao.ContactItem;
import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.data.app.LiveApp;
import com.xmpp.chat.service.RosterManager;
import com.xmpp.chat.util.EmojiUtil;
import com.xmpp.chat.util.SettingsUtil;
import com.xmpp.chat.xmpp.XMPP;

public class DataProvider {

	// Listeners
	PacketListener packetListener;
	LastActivityManager lastActivityManager;
	FileTransferListener fileListener;
	ChatManagerListener chatListener;
	RosterListener rosterListener;

	// Data storage
	List<ChatItem> chats = new ArrayList<ChatItem>();
	HashMap<String, ContactItem> addedChats = new HashMap<String, ContactItem>();
	HashMap<String, MessageItem> lastMessages = new HashMap<String, MessageItem>();
	HashMap<String, MyListWrapper<MessageItem>> messages = new HashMap<String, MyListWrapper<MessageItem>>();

	// Watchers
	List<OnDataChanged> watchers = new ArrayList<DataProvider.OnDataChanged>();

	public static List<ChatItem> getChats() {
		return get().chats;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static MyListWrapper<MessageItem> getMessages(String jid) {
		if (get().messages.containsKey(StringUtils.parseBareAddress(jid))) {
			return get().messages.get(StringUtils.parseBareAddress(jid));
		}

		MyListWrapper<MessageItem> msgs = null;
		try {
			msgs = new MyListWrapper(DatabaseHelper.getInstance(LiveApp.get())
					.getDao(MessageItem.class)
					.queryForEq("opponent", StringUtils.parseBareAddress(jid)));
		} catch (SQLException e) {
			msgs = new MyListWrapper<MessageItem>();
		}
		get().messages.put(StringUtils.parseBareAddress(jid), msgs);
		return msgs;
	}

	private DataProvider() {

		try {
			QueryBuilder<MessageItem, ?> query = DatabaseHelper
					.getInstance(LiveApp.get()).getDao(MessageItem.class)
					.queryBuilder();
			List<MessageItem> last = query.groupBy("opponent").query();

			for (int i = 0; i < last.size(); i++) {
				lastMessages.put(last.get(i).opponent, last.get(i));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {
			// initRoaster();
		} catch (Exception e) {
			e.printStackTrace();
		}

		rosterListener = new RosterListener() {

			public void entriesAdded(Collection<String> entries) {
				System.out.println(entries.toString());
				listUpdated();
				for (String str : entries) {
					ChatItem chatItem = new ChatItem();
					// Log.e("tag", "DataProvider ::" + chatItem.displayName +
					// "," + chatItem.jid);
					chatItem.jid = str;

					chatItem.displayName = addedChats.containsKey(StringUtils
							.parseBareAddress(chatItem.jid)) ? addedChats
							.get(StringUtils.parseBareAddress(chatItem.jid)).displayName
							: DatabaseHelper
									.getInstance(LiveApp.get())
									.getDisplayName(
											LiveApp.get(),
											StringUtils
													.parseBareAddress(chatItem.jid));
					Log.e("taggi", "values 1 ::" + chatItem.displayName + " , "
							+ str);

					// chatItem.displayName =
					// addedChats.containsKey(StringUtils.parseBareAddress(chatItem.displayName))
					// ?
					// addedChats.get(StringUtils.parseBareAddress(chatItem.displayName)).displayName
					// :
					// DatabaseHelper.getInstance(LiveApp.get()).getDisplayName(LiveApp.get(),
					// StringUtils.parseBareAddress(chatItem.displayName));

					if (addedChats.containsKey(StringUtils
							.parseBareAddress(chatItem.jid))) {
						byte[] img = addedChats.get(StringUtils
								.parseBareAddress(chatItem.jid)).imageByte;
						if (img == null) {
							VCard card = new VCard();
							try {
								card.load(
										XMPP.getInstance().getConnection(
												LiveApp.get()), chatItem.jid);
								addedChats.get(StringUtils
										.parseBareAddress(chatItem.jid)).imageByte = card
										.getAvatar();
								if (addedChats.get(StringUtils
										.parseBareAddress(chatItem.jid)).imageByte != null) {
									DatabaseHelper
											.getInstance(LiveApp.get())
											.updateContact(
													addedChats.get(StringUtils
															.parseBareAddress(chatItem.jid)));
								}
								img = addedChats.get(StringUtils
										.parseBareAddress(chatItem.jid)).imageByte;
							} catch (NoResponseException e) {
								e.printStackTrace();
							} catch (NotConnectedException e) {
								e.printStackTrace();
							} catch (XMPPErrorException e) {
								e.printStackTrace();
							}
						}
						chatItem.imageByte = img;
					}

					if (!lastMessages.containsKey(StringUtils
							.parseBareAddress(chatItem.jid))) {
						continue;
					}

					// chatItem.status = "Offline";
					chatItem.status = RosterManager.getLastActivity(
							LiveApp.get(), chatItem.jid, false);

					chatItem.isNewMessages = true;
					boolean found = false;
					Log.e("tag", "DataProvider chats1 ::" + chats);
					for (ChatItem chat : chats) {
						if (chat.jid.equals(chatItem.jid)) {
							found = true;
							break;
						}
					}
					if (!found) {
						chats.add(chatItem);
						Log.e("tag", "DataProvider chats found ::" + chats);
					}

					if (lastMessages.containsKey(StringUtils
							.parseBareAddress(chatItem.jid))) {
						chatItem.lastMessage = lastMessages.get(StringUtils
								.parseBareAddress(chatItem.jid)).message;
						chatItem.lastMessageTimestamp = lastMessages
								.get(StringUtils.parseBareAddress(chatItem.jid)).timestamp;
						chatItem.isNewMessages = lastMessages.get(StringUtils
								.parseBareAddress(chatItem.jid)).isNewMessage;
					}
				}
				sortChats();
				for (int i = 0; i < chats.size(); i++) {
					Iterator<Presence> pr = XMPP.getInstance()
							.getConnection(LiveApp.get()).getRoster()
							.getPresences(((ChatItem) chats.get(i)).jid)
							.iterator();
					while (pr.hasNext()) {
						if (((Presence) pr.next()).getType() == Presence.Type.available) {
							((ChatItem) chats.get(i)).status = "Online";

						}
					}
				}
				notifyWatchers();
				return;
			}

			public void entriesDeleted(
					Collection<String> paramAnonymousCollection) {
				System.out.println(paramAnonymousCollection.toString());
			}

			public void entriesUpdated(
					Collection<String> paramAnonymousCollection) {
				System.out.println(paramAnonymousCollection.toString());
			}

			public void presenceChanged(Presence presence) {
				for (ChatItem chat : chats) {
					if ((!chat.isGroup)
							&& (StringUtils.parseBareAddress(chat.jid)
									.equals(StringUtils
											.parseBareAddress(presence
													.getFrom())))) {
						chat.status = presence.isAvailable() ? "Online"
								: chat.status;
						notifyWatchers();
					}
				}
			}
		};

		XMPP.getInstance().getConnection(LiveApp.get()).getRoster()
				.addRosterListener(rosterListener);

		chatListener = new ChatManagerListener() {

			@Override
			public void chatCreated(Chat arg0, boolean local) {
				if (arg0.getThreadID() == null && !local) {
					for (ChatItem chat : chats) {

						if (chat.jid.equals(StringUtils.parseBareAddress(arg0
								.getParticipant()))) {
							chat.thread = arg0.getThreadID();
							break;
						}
					}
				}
			}
		};

		ChatManager.getInstanceFor(
				XMPP.getInstance().getConnection(LiveApp.get()))
				.addChatListener(chatListener);
		final FileTransferManager fileManager = new FileTransferManager(XMPP
				.getInstance().getConnection(LiveApp.get()));
		FileTransferNegotiator.setServiceEnabled(XMPP.getInstance()
				.getConnection(LiveApp.get()), true);

		fileListener = new FileTransferListener() {

			@Override
			public void fileTransferRequest(final FileTransferRequest transfer) {

				final String group = transfer.getDescription();
				// boolean isGroup = group!=null &&
				// StringUtils.parseName(group).equals(StringUtils.parseName(chatItem.jid));
				File outFile = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
								+ "/LiveApp/" + transfer.getFileName());
				;
				int filePos = 1;
				while (outFile.exists()) {
					String fileName = transfer.getFileName();
					String extension = MimeTypeMap
							.getFileExtensionFromUrl(transfer.getFileName());
					if (extension != null && extension.length() > 0) {
						fileName = fileName.substring(0,
								fileName.indexOf(extension) - 1)
								+ "(" + filePos++ + ")" + "." + extension;
					} else {
						fileName = fileName + " (" + filePos++ + ")";
					}
					outFile = new File(LiveApp.get().getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS)
							+ "/LiveApp/" + fileName);
				}

				// if(LiveApp.get().getFileTransfer(outFile.getAbsolutePath())!=null)
				// {
				// Crashlytics.log(Log.WARN, "LiveApp", "Transfer for " +
				// outFile.getAbsolutePath() + " already exist");
				// return;
				// }
				final MessageItem messageItem = new MessageItem();
				messageItem.message = EmojiUtil.getFileType(outFile);// "Incoming
																		// file
																		// ";//
																		// +
																		// transfer.getFileName()
																		// +
																		// ".\n"
																		// +
																		// transfer.getFileSize()/1024
																		// +
																		// "kb";
				messageItem.outMessage = false;
				messageItem.file = outFile.getAbsolutePath();
				messageItem.timestamp = Calendar.getInstance()
						.getTimeInMillis();
				messageItem.opponent = StringUtils.parseBareAddress(transfer
						.getRequestor());
				messageItem.opponentDisplay = DatabaseHelper.getInstance(
						LiveApp.get()).getDisplayName(LiveApp.get(),
						StringUtils.parseBareAddress(transfer.getRequestor()));
				if (group != null && group.length() > 0) {
					String sender = StringUtils.parseBareAddress(group);
					messageItem.opponent = sender;
					messageItem.opponentDisplay = DatabaseHelper.getInstance(
							LiveApp.get()).getDisplayName(
							LiveApp.get(),
							StringUtils.parseBareAddress(transfer
									.getRequestor()));
				}
				// Crashlytics.log(Log.WARN, "liveApp", "Message for " +
				// outFile.getAbsolutePath() + " added");
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						// getMessages(group != null && group.length() > 0 ?
						// group : transfer.getRequestor())
						// .add(messageItem);
						// notifyWatchers();
					}
				});

				if (chats == null)
					return;

				// String group = transfer.getDescription();
				Log.e("tag", "DataProvider chats fileListener ::" + chats);
				for (ChatItem chatItem : chats) {
					boolean isGroup = group != null
							&& StringUtils.parseName(group).equals(
									StringUtils.parseName(chatItem.jid));
					if (isGroup
							|| transfer.getRequestor().startsWith(
									StringUtils.parseName(chatItem.jid))) {
						chatItem.lastMessageTimestamp = System
								.currentTimeMillis();
						chatItem.lastMessage = EmojiUtil.getFileType(outFile);
						chatItem.isNewMessages = true;
						sortChats();
						break;
					}
				}

				fileManager.removeFileTransferListener(this);
				ServiceDiscoveryManager sdm = ServiceDiscoveryManager
						.getInstanceFor(XMPP.getInstance().getConnection(
								LiveApp.get()));
				sdm.addFeature("jabber.org/protocol/si");
				sdm.addFeature("http://jabber.org/protocol/si");
				sdm.addFeature("http://jabber.org/protocol/disco#info");
				sdm.addFeature("jabber:iq:privacy");

				FileTransferNegotiator.setServiceEnabled(XMPP.getInstance()
						.getConnection(LiveApp.get()), true);
				FileTransferNegotiator.IBB_ONLY = true;

				// FileTransferManager fileManager = new
				// FileTransferManager(XMPP.getInstance().getConnection(LiveApp.get()));
				FileTransferNegotiator.setServiceEnabled(XMPP.getInstance()
						.getConnection(LiveApp.get()), true);
				fileManager.addFileTransferListener(fileListener);
			}
		};

		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(XMPP.getInstance()
						.getConnection(LiveApp.get()));

		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("http://jabber.org/protocol/si");
		sdm.addFeature("jabber:iq:privacy");

		fileManager.addFileTransferListener(fileListener);

		packetListener = new PacketListener() {

			@Override
			public void processPacket(Packet packet)
					throws NotConnectedException {
				if ((packet instanceof Message)) {
					// Collection<PacketExtension> extensions =
					// packet.getExtensions();
					final ArrayList<ChatItem> groups = new ArrayList<ChatItem>();
					final List<ContactItem> contacts = new ArrayList<ContactItem>();

					// for (PacketExtension extension : extensions) {
					// if ((extension instanceof GetGroupExtension)) {
					// ChatItem chat = PacketParser.parseGroupsPacket(chats,
					// (GetGroupExtension) extension);
					// if (chat != null && !groups.contains(chat))
					// groups.add(chat);
					// } else if (extension instanceof GetContactsExtension) {
					// PacketParser.parseGetContactsPacket(contacts,
					// (GetContactsExtension) extension);
					// }
					// }

					boolean found = false;
					for (ChatItem chat : groups) {
						found = false;
						for (ChatItem chati : chats) {
							if (chati.jid.equals(chat.jid)) {
								found = true;
								chati.isGroup = true;
								chati.lastMessage = chat.lastMessage;
								chati.lastMessageTimestamp = chat.lastMessageTimestamp;
								chati.photo = chat.photo;
								// chati.category1 = chat.category1;
								// chati.category2 = chat.category2;
								// chati.category3 = chat.category3;

								chati.imageByte = chat.imageByte;
								break;
							}
						}
						if (!found) {
							chats.add(chat);
						}
					}

					Collections.sort(contacts, new Comparator<ContactItem>() {
						public int compare(ContactItem c1, ContactItem c2) {
							return c1.displayName.compareTo(c2.displayName);
						}
					});

					DatabaseHelper.getInstance(LiveApp.get()).updateContacts(
							contacts);
					sortChats();

					// if (packet.getFrom().equals(XMPP.LIVESERVICE)) {
					// parseCommands((Message) packet);
					//
					// return;
					// }

					parseMessage((Message) packet);
					// Log.e("tag", "values for chat ::" + packet);

				}
			}
		};

		XMPP.getInstance().getConnection(LiveApp.get())
				.addPacketListener(packetListener, null);

		// ProviderManager.addExtensionProvider("group", "liveapp:iq:group",
		// GetGroupExtension.getProvider());
		// ProviderManager.addExtensionProvider("group",
		// "liveapp:iq:groupcommon", GetGroupCommonExtension.getProvider());
		// ProviderManager.addExtensionProvider("groups", "liveapp:iq:group",
		// GetGroupsExtension.getProvider());
		// ProviderManager.addExtensionProvider("group",
		// "liveapp:iq:groupmember", GetGroupMemberExtension.getProvider());
		//
		// ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si",
		// new StreamInitiationProvider());
		//
		// ProviderManager.addIQProvider("query",
		// "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
		// ProviderManager.addIQProvider("open",
		// "http://jabber.org/protocol/ibb", new OpenIQProvider());
		// ProviderManager.addIQProvider("data",
		// "http://jabber.org/protocol/ibb", new DataPacketProvider());
		// ProviderManager.addIQProvider("close",
		// "http://jabber.org/protocol/ibb", new CloseIQProvider());
		// ProviderManager.addExtensionProvider("data",
		// "http://jabber.org/protocol/ibb", new DataPacketProvider());
		// ProviderManager.addIQProvider("query",
		// "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
		// ProviderManager.addIQProvider("query",
		// "http://jabber.org/protocol/disco#items", new
		// DiscoverItemsProvider());
		// ProviderManager.addIQProvider("query",
		// "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

		listUpdated();
	}

	private static DataProvider instance;

	private static DataProvider get() {
		if (instance == null) {
			instance = new DataProvider();
		}
		return instance;
	}

	public interface OnDataChanged {
		void onDataChange();
	}

	public static void addWatcher(OnDataChanged onDataChanged) {
		get().watchers.add(onDataChanged);
	}

	public static void removeWatcher(OnDataChanged onDataChanged) {
		get().watchers.remove(onDataChanged);
	}

	private void notifyWatchers() {
		for (OnDataChanged on : watchers) {
			on.onDataChange();
		}
	}

	// private void parseCommands(Message packet) {
	// try {
	//
	// if (packet != null && ((Message) packet).getBody() != null) {
	// JSONObject obj = new JSONObject(((Message) packet).getBody());
	// String type = obj.getString("type");
	// if (type.equals("NEW_GROUP")) {
	// // try {
	// // XMPP.getInstance().getConnection(LiveApp.get())
	// // .sendPacket(new
	// //
	// IQGetGroup(XMPP.getInstance().getConnection(LiveApp.get()).getUser()));
	// // } catch (NotConnectedException e) {
	// // }
	// }
	// else if (type.equals("PLUS_ONE")) {
	// String groupId = obj.getString("group");
	// Log.e("tag", "DataProvider chats parseCommands ::" + chats);
	// for (ChatItem chat : chats) {
	// if (chat.groupID.equals(groupId)) {
	// // chat.groupBell = true;
	// // chat.groupBellTo = obj.getString("to");
	// // chat.lastMessageTimestamp =
	// // System.currentTimeMillis();
	// //
	// DatabaseHelper.getInstance(LiveApp.get()).updateGroupBell(chat.groupID,
	// // true,
	// // chat.groupBellTo);
	//
	// sortChats();
	// break;
	// }
	// }
	// Notifications.showPlusNotification(getActivity(),
	// obj.getString("group"),obj.getString("from"),
	// obj.getString("to"));
	// }
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }

	private void sortChats() {
		Collections.sort(chats, new Comparator<ChatItem>() {

			@Override
			public int compare(ChatItem lhs, ChatItem rhs) {
				if (lhs.lastMessageTimestamp == 0
						&& rhs.lastMessageTimestamp == 0) {
					return Integer.parseInt(rhs.groupID)
							- Integer.parseInt(lhs.groupID);
				}
				if (lhs.lastMessageTimestamp == 0) {
					return 1;
				}
				if (rhs.lastMessageTimestamp == 0) {
					return -1;
				}
				return (int) (rhs.lastMessageTimestamp - lhs.lastMessageTimestamp);
			}
		});
		notifyWatchers();
	}

	public void listUpdated() {
		try {
			List<ContactItem> contacts = DatabaseHelper
					.getInstance(LiveApp.get()).getDao(ContactItem.class)
					.queryForAll();
			for (ContactItem item : contacts) {
				addedChats.put(item.username, item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initRoaster() {
		try {
			if (XMPP.getInstance().getRoaster() == null) {
				return;
			}
			Collection<RosterEntry> rosters = XMPP.getInstance().getRoaster()
					.getEntries();
			for (RosterEntry roster : rosters) {
				ChatItem chatItem = new ChatItem();

				chatItem.jid = StringUtils.parseBareAddress(roster.getUser());

				chatItem.displayName = addedChats.containsKey(StringUtils
						.parseBareAddress(chatItem.jid)) ? addedChats
						.get(StringUtils.parseBareAddress(chatItem.jid)).displayName
						: DatabaseHelper
								.getInstance(LiveApp.get())
								.getDisplayName(
										LiveApp.get(),
										StringUtils
												.parseBareAddress(chatItem.jid));

				// Log.e("taggi", "values 2 ::" + chatItem.displayName + " , " +
				// chatItem.jid);

				if (addedChats.containsKey(StringUtils
						.parseBareAddress(chatItem.jid))) {
					byte[] img = addedChats.get(StringUtils
							.parseBareAddress(chatItem.jid)).imageByte;
					if (img == null) {
						VCard card = new VCard();
						try {
							card.load(
									XMPP.getInstance().getConnection(
											LiveApp.get()), chatItem.jid);
							addedChats.get(StringUtils
									.parseBareAddress(chatItem.jid)).imageByte = card
									.getAvatar();
							if (addedChats.get(StringUtils
									.parseBareAddress(chatItem.jid)).imageByte != null) {
								DatabaseHelper
										.getInstance(LiveApp.get())
										.updateContact(
												addedChats.get(StringUtils
														.parseBareAddress(chatItem.jid)));
							}
							img = addedChats.get(StringUtils
									.parseBareAddress(chatItem.jid)).imageByte;
						} catch (NoResponseException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					chatItem.imageByte = img;
				}
				chatItem.status = RosterManager.getLastActivity(LiveApp.get(),
						chatItem.jid, false);
				if (lastMessages.containsKey(StringUtils
						.parseBareAddress(chatItem.jid))) {
					chatItem.lastMessage = lastMessages.get(StringUtils
							.parseBareAddress(chatItem.jid)).message;
					chatItem.lastMessageTimestamp = lastMessages
							.get(StringUtils.parseBareAddress(chatItem.jid)).timestamp;
					chatItem.isNewMessages = lastMessages.get(StringUtils
							.parseBareAddress(chatItem.jid)).isNewMessage;
					boolean found = false;
					for (ChatItem chat : chats) {
						if (chat.jid.equals(chatItem.jid)) {
							found = true;
							break;
						}
					}
					if (!found) {
						chats.add(chatItem);
					}
				}
			}

			try {
				List<ChatItem> groups = DatabaseHelper
						.getInstance(LiveApp.get()).getDao(ChatItem.class)
						.queryForAll();
				boolean found = false;
				for (ChatItem chat : groups) {
					found = false;
					if (chat.isGroup && chat.photo != null) {
						chat.imageByte = Base64.decode(chat.photo);
					}
					for (ChatItem chati : chats) {
						if (chati.jid.equals(chat.jid)) {
							found = true;
							break;
						}
					}
					if (!found) {
						chats.add(chat);
					}
				}
				// chats.addAll(groups);
			} catch (SQLException e) {
			}

			sortChats();
			// Log.e("tag", "DataProvider chats initRoaster ::" + chats);

			for (int i = 0; i < chats.size(); i++) {
				if (!chats.get(i).isGroup) {
					Iterator<Presence> pr = XMPP.getInstance()
							.getConnection(LiveApp.get()).getRoster()
							.getPresences(((ChatItem) chats.get(i)).jid)
							.iterator();
					while (pr.hasNext()) {
						if (((Presence) pr.next()).getType() == Presence.Type.available) {
							((ChatItem) chats.get(i)).status = "Online";
						}
					}
				}
			}

			// root.post(new Runnable() {
			// public void run() {
			// if (chats.size() == 0) {
			// viewEmpty.setText("No contacts added!");
			// }
			// adapter = new ChatListAdapter(getActivity(), chats);
			//
			// listChats.setAdapter(adapter);
			// listChats.setOnItemClickListener(new
			// AdapterView.OnItemClickListener() {
			//
			// public void onItemClick(AdapterView<?> paramAnonymousAdapterView,
			// View paramAnonymousView, int pos, long paramAnonymousLong) {
			// FragmentManager fm = getFragmentManager();
			// adapter.getItem(pos).isNewMessages = false;
			// adapter.notifyDataSetChanged();
			//
			// fm.beginTransaction().replace(R.id.main,
			// AbstractChatFragment.getInstance(adapter.getItem(pos))).addToBackStack(null).commit();
			//
			// // if (adapter.getItem(pos).isGroup) {
			// // fm.beginTransaction().replace(R.id.main,
			// // MucChatFragment.getInstance(adapter.getItem(pos),
			// // false)).addToBackStack(null).commit();
			// // } else {
			// // fm.beginTransaction().replace(R.id.main,
			// //
			// ChatFragment.getInstance(adapter.getItem(pos))).addToBackStack(null).commit();
			// // }
			// }
			// });
			// adapter.notifyDataSetChanged();
			// }
			// });
		} catch (XMPPException localXMPPException) {
		}

	}

	private void parseMessage(final Message message) {
		Log.e("tai", "message.getType() ::" + message.getType());
		Log.e("tai",
				"StringUtils.getType() ::"
						+ StringUtils.parseResource(message.getFrom()));
		Log.e("tai", "XMPP.getInstance() ::"
				+ XMPP.getInstance().getConnection(LiveApp.get()).getUser());

		if (message.getType() == Message.Type.groupchat
				&& StringUtils.parseResource(message.getFrom()).length() == 0) {
			return;
		}

		// StringUtils.parseResource(message.getTo()) == "android1") {
		// return;
		// }

		for (PacketExtension extension : message.getExtensions()) {
			if (extension instanceof ChatStateExtension) {
				String typing = ((ChatStateExtension) extension)
						.getElementName();
				for (ChatItem chat : chats) {
					if (StringUtils.parseBareAddress(chat.jid).equals(
							StringUtils.parseBareAddress(message.getFrom()))) {
						if (typing.equals("composing")) {
							chat.typing = "is typing...";
						} else {
							chat.typing = "";
						}
						notifyWatchers();
						break;
					}
				}
			}
		}

		// if (message.getBody() != null && message.getBody().equals("ANONYMOUS
		// REQUEST")) {
		// ContactItem contact = new ContactItem();
		// contact.username = StringUtils.parseName(message.getFrom());
		// VCard card = new VCard();
		// try {
		// card.load(XMPP.getInstance().getConnection(LiveApp.get()),
		// StringUtils.parseBareAddress(message.getFrom()));
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
		// DatabaseHelper.getInstance(LiveApp.get()).updateContact(contact);
		// // return;
		// }

		// if (message.getBody() != null && message.getBody().equals("New group
		// created")) {
		// final MessageItem m = new MessageItem();
		// m.message = message.getBody();
		// m.outMessage = false;
		// m.timestamp = Calendar.getInstance().getTimeInMillis();
		// m.opponent = StringUtils.parseBareAddress(message.getFrom());
		// m.isNewMessage = true;
		// List<MessageItem> messages = getMessages(message.getFrom());
		// if (messages.size() > 0 && (messages.get(messages.size() -
		// 1).message.equals(message.getBody())
		// || messages.get(0).message.equals(message.getBody()))) {
		// return;
		// }
		// lastMessages.put(StringUtils.parseBareAddress(message.getFrom()), m);
		// new Handler(Looper.getMainLooper()).post(new Runnable() {
		//
		// @Override
		// public void run() {
		// getMessages(message.getFrom()).add(m);
		// notifyWatchers();
		// }
		// });
		//
		// m.groupSender = StringUtils.parseResource(message.getFrom());
		// if
		// (m.groupSender.equals(XMPP.getInstance().getConnection(LiveApp.get()).getUser()))
		// {
		// m.outMessage = true;
		// m.isNewMessage = false;
		// } else {
		// m.groupSender =
		// DatabaseHelper.getInstance(LiveApp.get()).getDisplayName(LiveApp.get(),
		// StringUtils.parseBareAddress(m.groupSender));
		// }
		// m.opponentDisplay = "System message";
		//
		// long time = Calendar.getInstance().getTimeInMillis();
		// // boolean newMessage = true;
		// for (PacketExtension ext : message.getExtensions()) {
		// if (ext instanceof DelayInfo) {
		// // newMessage = false;
		// DelayInfo delay = (DelayInfo) ext;
		// time = delay.getStamp().getTime();
		// }
		// }
		// try {
		// DatabaseHelper.getInstance(LiveApp.get()).getDao(MessageItem.class).create(m);
		// return;
		// } catch (SQLException localSQLException) {
		// localSQLException.printStackTrace();
		// }
		// }

		if (message.getBody() != null && message.getBody().length() > 0) {

			boolean chatFound = false;
			final MessageItem m = new MessageItem();
			m.message = message.getBody();
			m.outMessage = false;
			m.timestamp = Calendar.getInstance().getTimeInMillis();
			m.opponent = StringUtils.parseBareAddress(message.getFrom());
			m.isNewMessage = true;

			if (message.getType().equals(Message.Type.groupchat)) {
				SettingsUtil.setLastHistory(LiveApp.get(),
						StringUtils.parseBareAddress(m.opponent));
				for (ChatItem chat : chats) {
					if (StringUtils.parseBareAddress(chat.jid).equals(
							m.opponent)) {
						m.groupSender = StringUtils.parseResource(message
								.getFrom());
						if (m.groupSender.equals(XMPP.getInstance()
								.getConnection(LiveApp.get()).getUser())) {
							m.outMessage = true;
							m.isNewMessage = false;
						} else {
							m.groupSender = DatabaseHelper
									.getInstance(LiveApp.get())
									.getDisplayName(
											LiveApp.get(),
											StringUtils
													.parseBareAddress(m.groupSender));
						}
						m.opponentDisplay = m.groupSender;

						long time = Calendar.getInstance().getTimeInMillis();
						// boolean newMessage = true;
						for (PacketExtension ext : message.getExtensions()) {
							if (ext instanceof DelayInfo) {
								// newMessage = false;
								DelayInfo delay = (DelayInfo) ext;
								time = delay.getStamp().getTime();
								return;
							}
						}
						chat.isGroup = true;
						chat.lastMessage = message.getBody();
						chat.isNewMessages = time >= AppSettings
								.getGroupLastRead(LiveApp.get(), chat.groupID);
						chat.lastMessageTimestamp = time;

						Log.e("tai", "boolean chatFound  :: sort chat");
						sortChats();
						break;
					}
				}

			} else {

				for (ChatItem chat : chats) {
					if (StringUtils.parseBareAddress(chat.jid).equals(
							m.opponent)) {
						chatFound = true;
						chat.lastMessage = message.getBody();
						chat.isNewMessages = true;
						chat.lastMessageTimestamp = Calendar.getInstance()
								.getTimeInMillis();
						sortChats();

						chat.chatcount += 1;
						// Log.e("tai", "boolean chatFound ::under sort chat");

						break;
					}

				}

				if (!chatFound) {
					ChatItem chat = new ChatItem();
					chat.status = RosterManager.getLastActivity(LiveApp.get(),
							StringUtils.parseBareAddress(message.getFrom()),
							false);
					chat.displayName = DatabaseHelper.getInstance(
							LiveApp.get()).getDisplayName(LiveApp.get(),
							StringUtils.parseBareAddress(message.getFrom()));
					chat.thread = message.getThread();
					chat.jid = StringUtils.parseBareAddress(message.getFrom());
					chat.lastMessage = message.getBody();
					chat.isNewMessages = true;
					chat.lastMessageTimestamp = Calendar.getInstance()
							.getTimeInMillis();

					Presence pr = new Presence(Type.subscribe);
					pr.setFrom(XMPP.getInstance().getConnection(LiveApp.get())
							.getUser());
					pr.setTo(chat.jid);
					try {
						XMPP.getInstance().getConnection(LiveApp.get())
								.sendPacket(pr);
					} catch (NotConnectedException e) {
						e.printStackTrace();
					}
					Log.e("tag", "DataProvider chats parseMessage11 ::" + chats);
					chats.add(chat);
					sortChats();
					chat.chatcount += 1;

				}

			}

			lastMessages
					.put(StringUtils.parseBareAddress(message.getFrom()), m);

			new Handler(Looper.getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					getMessages(message.getFrom()).add(m);
					notifyWatchers();
				}
			});
			try {
				DatabaseHelper.getInstance(LiveApp.get())
						.getDao(MessageItem.class).create(m);
				return;
			} catch (SQLException localSQLException) {
				localSQLException.printStackTrace();
			}

		}
	}
}
