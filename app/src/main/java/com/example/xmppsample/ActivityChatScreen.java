package com.example.xmppsample;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.j256.ormlite.stmt.UpdateBuilder;
import com.xmpp.chat.adapter.EmoticonsGridAdapter.KeyClickListener;
import com.xmpp.chat.adapter.EmoticonsPagerAdapter;
import com.xmpp.chat.adapter.LiveChatAdapter;
import com.xmpp.chat.dao.ChatItem;
import com.xmpp.chat.dao.EmojiItem;
import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.dao.StatusItem;
import com.xmpp.chat.data.AppSettings;
import com.xmpp.chat.data.DataProvider;
import com.xmpp.chat.data.DataProvider.OnDataChanged;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.data.app.ErrorDialog;
import com.xmpp.chat.framework.LiveUtil;
import com.xmpp.chat.framework.Notifications;
import com.xmpp.chat.service.RosterManager;
import com.xmpp.chat.util.EmojiUtil;
import com.xmpp.chat.util.SettingsUtil;
import com.xmpp.chat.xmpp.XMPP;

public class ActivityChatScreen extends Activity implements OnDataChanged {

	// ActionBar actionBar;
	String id;
	Activity acitiviy = ActivityChatScreen.this;
	Context context = ActivityChatScreen.this;
	EditText editMessage;
	String photoOutput;
	protected ChatItem chatItem;
	LiveChatAdapter adapter;
	ListView listChat;
	protected long lastComposing = 0;
	ChatManagerListener chatListener;
	MessageListener messageListener;
	RosterListener rosterListener;
	MenuItem menuItemMood;
	int sendFileTimes = 0;
	Chat chat;
	LinearLayout llfragment_livechat;
	ImageView imageEmoji;
	View layoutEmojis;
	View layoutStickers;
	View imageAttaches;
	View imageStickers;
	View imageEmojis;

	View attachTakePhoto;
	View attachChoosePhoto;

	private LinearLayout emoticonsCover;
	private int keyboardHeight;
	private View popUpView;
	private View simpleview;

	public static PopupWindow popupWindow;
	private boolean isKeyBoardVisible;

	private String nick = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setHomeButtonEnabled(true);
		setContentView(R.layout.activity_livechatscreen);
		chatItem = new ChatItem();

		// actionBar.setTitle(getIntent().getStringExtra("username"));
		id = getIntent().getStringExtra("id");
		chatItem.displayName = getIntent().getStringExtra("username");
		chatItem.jid = getIntent().getStringExtra("id");

		initialization();

		chatListener = new ChatManagerListener() {

			@Override
			public void chatCreated(Chat chatCreated, boolean local) {
				onChatCreated(chatCreated);
			}
		};

		rosterListener = new RosterListener() {

			@Override
			public void presenceChanged(Presence pr) {
				if (pr.isAvailable()) {
					StatusItem status = StatusItem.fromJSON(pr.getStatus());
					DatabaseHelper.getInstance(acitiviy).updateContactStatus(
							pr.getFrom(), status.status, status.mood);
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
							.getConnection(acitiviy).getRoster()
							.getPresences(jid).iterator();
					while (pr.hasNext()) {
						Presence p = ((Presence) pr.next());
						if (p.getType() == Presence.Type.available) {
							StatusItem statusItem = StatusItem.fromJSON(p
									.getStatus());
							String status = statusItem.status;
							if (statusItem.mood > 0) {
								DatabaseHelper.getInstance(acitiviy)
										.updateContactStatus(jid,
												statusItem.status,
												statusItem.mood);
								if (menuItemMood != null
										&& chatItem != null
										&& StringUtils.parseBareAddress(
												chatItem.jid).equals(
												StringUtils
														.parseBareAddress(jid)))
									menuItemMood.setIcon(LiveUtil
											.getMoodRes(statusItem.mood));
							}
						}
					}
				}
				Log.d("LOL", "Test");
			}
		};

		ChatManager.getInstanceFor(XMPP.getInstance().getConnection(acitiviy))
				.addChatListener(chatListener);
		XMPP.getInstance().getConnection(acitiviy).getRoster()
				.addRosterListener(rosterListener);
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(XMPP.getInstance().getConnection(acitiviy));

		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("jabber:iq:privacy");

		// ******************************************************************************************//
		try {

			String addr1 = StringUtils.parseBareAddress(XMPP.getInstance()
					.getConnection(acitiviy).getUser());

			String addr2 = StringUtils.parseBareAddress(chatItem.jid);

			if (addr1.compareTo(addr2) > 0) {
				String addr3 = addr2;
				addr2 = addr1;
				addr1 = addr3;
			}

			chat = ChatManager.getInstanceFor(
					XMPP.getInstance().getConnection(acitiviy)).getThreadChat(
					addr1 + "-" + addr2);
			Log.e("tag", "chat value single chat :" + chat);
			if (chat == null) {
				chat = ChatManager.getInstanceFor(
						XMPP.getInstance().getConnection(acitiviy)).createChat(
						chatItem.jid, addr1 + "-" + addr2, messageListener);
			} else {
				chat.addMessageListener(messageListener);
			}

			if (chatItem.anonymous) {
				try {
					chat.sendMessage("ANONYMOUS REQUEST");
				} catch (NotConnectedException e) {
				} catch (XMPPException e) {
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	Handler composePaused = new Handler() {
		public void handleMessage(android.os.Message mes) {
			if (System.currentTimeMillis() - lastComposing > 4000) {
				if (chat != null) {
					Message msg = new Message();

					msg.setTo(StringUtils.parseBareAddress(chatItem.jid));
					msg.setFrom(XMPP.getInstance().getConnection(acitiviy)
							.getUser());
					ChatStateExtension ext = new ChatStateExtension(
							ChatState.paused);
					msg.addExtension(ext);
					lastComposing = System.currentTimeMillis();
					try {
						chat.sendMessage(msg);
					} catch (NotConnectedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	};

	@Override
	public void onStart() {
		super.onStart();

		DataProvider.addWatcher(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		DataProvider.removeWatcher(this);
	}

	@Override
	public void onDataChange() {
		if (adapter != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}
	}

	void onChatCreated(Chat chatCreated) {
		if (chat != null) {
			if (StringUtils.parseBareAddress(chat.getParticipant()).equals(
					StringUtils.parseBareAddress(chatCreated.getParticipant()))) {
				chat.removeMessageListener(messageListener);
				chat = chatCreated;
				chat.addMessageListener(messageListener);
			}
		} else {
			chat = chatCreated;
			chat.addMessageListener(messageListener);
		}
	}

	void sendMessage(String message) {
		if (chat != null) {
			try {
				chat.sendMessage(message);
				Message msg = new Message();
				msg.setTo(StringUtils.parseBareAddress(chatItem.jid));
				msg.setFrom(XMPP.getInstance().getConnection(acitiviy)
						.getUser());

				ChatStateExtension ext = new ChatStateExtension(
						ChatState.paused);
				msg.addExtension(ext);
				lastComposing = System.currentTimeMillis();
				chat.sendMessage(msg);
				Log.e("tag", "sendMessage ::" + msg);
			} catch (NotConnectedException e) {
			} catch (Exception e) {
				ErrorDialog.showToast(acitiviy, "Message not sent!");
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {

		XMPP.getInstance().getConnection(acitiviy).getRoster()
				.removeRosterListener(rosterListener);
		ChatManager.getInstanceFor(XMPP.getInstance().getConnection(acitiviy))
				.removeChatListener(chatListener);
		if (chat != null && messageListener != null) {
			chat.removeMessageListener(messageListener);
		}

		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		if (emoticonsCover != null) {
			emoticonsCover.setVisibility(View.GONE);
		}

		// actionBar.setIcon(R.drawable.ic_launcher);

		super.onDestroy();
	}

	void onInputChanged() {
		if (chat != null) {
			Message msg = new Message();
			msg.setTo(StringUtils.parseBareAddress(chatItem.jid));
			msg.setFrom(XMPP.getInstance().getConnection(acitiviy).getUser());
			ChatStateExtension ext = new ChatStateExtension(ChatState.composing);
			msg.addExtension(ext);
			try {
				chat.sendMessage(msg);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
			composePaused.sendEmptyMessageDelayed(0, 5000);
			lastComposing = System.currentTimeMillis();
		}
	}

	void initialization() {

		llfragment_livechat = (LinearLayout) findViewById(R.id.llfragment_livechat);

		emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

		LayoutInflater inflater = LayoutInflater.from(acitiviy);
		popUpView = inflater.inflate(R.layout.emoticons_popup, null);

		imageEmoji = (ImageView) findViewById(R.id.attachImage);
		attachTakePhoto = (View) popUpView.findViewById(R.id.attachTakePhoto);
		attachChoosePhoto = (View) popUpView
				.findViewById(R.id.attachChoosePhoto);
	
		layoutEmojis = popUpView.findViewById(R.id.emoticons_pager);
		layoutStickers = popUpView.findViewById(R.id.stickers_pager);
		imageAttaches = popUpView.findViewById(R.id.imageAttachesList);
		imageEmojis = popUpView.findViewById(R.id.imageEmojisList);
		imageStickers = popUpView.findViewById(R.id.imageStickerList);

		imageAttaches.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				layoutEmojis.setVisibility(View.GONE);
				layoutStickers.setVisibility(View.GONE);
				imageAttaches.setBackgroundColor(0xFF808080);
				imageEmojis.setBackgroundColor(0x00000000);
				imageStickers.setBackgroundColor(0x00000000);
			}
		});
		imageEmojis.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			
				layoutStickers.setVisibility(View.GONE);
				layoutEmojis.setVisibility(View.VISIBLE);
				imageEmojis.setBackgroundColor(0xFF808080);
				imageAttaches.setBackgroundColor(0x00000000);
				imageStickers.setBackgroundColor(0x00000000);
			}
		});

		final float popUpheight = getResources().getDimension(
				R.dimen.keyboard_height);
		changeKeyboardHeight((int) popUpheight);

		findViewById(R.id.attachImage).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						showKeyboardPopup(popUpView, false);

					}
				});


		Notifications.cancelIncomingMesssageNotifications(acitiviy);


		new Thread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				final Presence pr = XMPP.getInstance().getConnection(acitiviy)
						.getRoster().getPresence(chatItem.jid);
				if (pr.getType() == Presence.Type.available) {
					runOnUiThread(new Runnable() {
						public void run() {
							if (menuItemMood != null) {
								String status = pr.getStatus();
								StatusItem stat = StatusItem.fromJSON(status);

								// actionBar.setSubtitle(stat.status);

								if (stat.mood > 0)
									menuItemMood.setIcon(LiveUtil
											.getMoodRes(stat.mood));
							}
						}
					});
				}

				final String la = RosterManager.getLastActivity(acitiviy,
						chatItem.jid, false);
				if (acitiviy == null) {
					return;
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {

							// actionBar.setSubtitle(la);
						} catch (Exception e) {
						}
					}
				});
				final VCard card = new VCard();
				try {
					card.load(XMPP.getInstance().getConnection(acitiviy),
							chatItem.jid);
					byte[] img = card.getAvatar();
					if (img != null) {
						final Bitmap bmp = BitmapFactory.decodeByteArray(img,
								0, img.length);
						try {
							runOnUiThread(new Runnable() {
								public void run() {

									// actionBar.setIcon(new
									// BitmapDrawable(getResources(), bmp));
								}
							});
						} catch (Exception e) {

							// actionBar.setIcon(R.drawable.ic_chat_person);
						}
					} else {

					}
				} catch (NoResponseException e1) {
					e1.printStackTrace();
				} catch (XMPPErrorException e1) {
					e1.printStackTrace();
				} catch (NotConnectedException e1) {
					e1.printStackTrace();
				}
			}
		}).start();

		listChat = (ListView) findViewById(R.id.listLiveChat);
	
		final TextView textTyping;

		Log.e("tag", "chatItem.jid :: " + chatItem.jid);
		List<MessageItem> messages = DataProvider.getMessages(chatItem.jid);

		adapter = new LiveChatAdapter(acitiviy, chatItem, messages,
				chatItem.displayName, chatItem.isGroup);
		listChat.setAdapter(adapter);
		listChat.setSelection(adapter.getCount() - 1);
		listChat.postDelayed(new Runnable() {

			@Override
			public void run() {
				listChat.setSelection(adapter.getCount() - 1);
			}
		}, 500);

		textTyping = (TextView) findViewById(R.id.textSupportTyping);

		editMessage = (EditText) findViewById(R.id.editMessage);
		editMessage.setText(AppSettings.getSaveMessage(acitiviy,
				StringUtils.parseBareAddress(chatItem.jid)));
		
		editMessage.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				onInputChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				AppSettings.setSaveMessage(acitiviy, StringUtils
						.parseBareAddress(chatItem.jid), editMessage.getText()
						.toString());
			}
		});

		findViewById(R.id.buttonSend).setOnClickListener(
				new View.OnClickListener() {

					public void onClick(View paramAnonymousView) {
						try {
							if (editMessage.getText().toString().trim()
									.length() == 0) {
								return;
							}
							sendMessage(editMessage.getText().toString().trim());
							MessageItem message = null;
							message = new MessageItem();
							message.message = editMessage.getText().toString()
									.trim();
							message.outMessage = true;
							message.timestamp = Calendar.getInstance()
									.getTimeInMillis();
							message.opponent = StringUtils
									.parseBareAddress(chatItem.jid);
							message.opponentDisplay = chatItem.displayName;

							if (!chatItem.isGroup)
								adapter.add(message);
							editMessage.setText("");
							listChat.smoothScrollToPosition(-1
									+ adapter.getCount());

							Log.e("tag", "value of chatting  :" + message);
							AppSettings.setSaveMessage(acitiviy,
									StringUtils.parseBareAddress(chatItem.jid),
									"");

							try {
								DatabaseHelper.getInstance(acitiviy)
										.getDao(MessageItem.class)
										.create(message);
							} catch (SQLException e) {
								e.printStackTrace();
							}

							if (chatItem.isGroup)
								return;

							Presence p = new Presence(Type.subscribe);
							p.setFrom(XMPP.getInstance()
									.getConnection(acitiviy).getUser());
							p.setTo(chatItem.jid);
							XMPP.getInstance().getConnection(acitiviy)
									.sendPacket(p);
							p = new Presence(Type.subscribed);
							p.setFrom(XMPP.getInstance()
									.getConnection(acitiviy).getUser());
							p.setTo(chatItem.jid);
							XMPP.getInstance().getConnection(acitiviy)
									.sendPacket(p);

						} catch (NotConnectedException e1) {
							e1.printStackTrace();
						}
					}
				});

		try {
			UpdateBuilder<MessageItem, ?> up = DatabaseHelper
					.getInstance(acitiviy).getDao(MessageItem.class)
					.updateBuilder();
			up.updateColumnValue("isNewMessage", false).where()
					.eq("opponent", StringUtils.parseBareAddress(chatItem.jid));
			DatabaseHelper.getInstance(acitiviy).getDao(MessageItem.class)
					.update(up.prepare());

		} catch (SQLException e1) {
		}


		messageListener = new MessageListener() {

			public void processMessage(Chat chatMes, final Message message) {

				Log.e("tag", "chat messages ::" + chatMes + message);

				runOnUiThread(new Runnable() {
					public void run() {
						for (PacketExtension extension : message
								.getExtensions()) {
							if (extension instanceof ChatStateExtension) {
								String typing = ((ChatStateExtension) extension)
										.getElementName();

								if (typing.equals("composing")) {

									// actionBar.setSubtitle("Online");
									textTyping.setVisibility(View.VISIBLE);
									chatItem.typing = " is typing...";
								} else {
									textTyping.setVisibility(View.INVISIBLE);
									chatItem.typing = "";
								}
								// actionBar.setTitle(chatItem.displayName +
								// chatItem.typing);
							}
						}

						if (message.getBody() != null
								&& message.getBody().length() > 0) {
							// MessageItem messageItem = new MessageItem();
							// messageItem.message = message.getBody();
							// messageItem.outMessage = false;
							// messageItem.timestamp =
							// Calendar.getInstance().getTimeInMillis();
							// messageItem.opponent = chatItem.name;
							// messageItem.opponentDisplay =
							// chatItem.displayName;
							//
							// ContactItem localContactItem = new ContactItem();
							// localContactItem.username = chatItem.name;
							// localContactItem.displayName =
							// displayName.trim().length() > 0 ? displayName
							// : chatItem.name;
							// localContactItem.isShowHome = true;
							// localContactItem.isRegistered = true;
							// if (displayName != null &&
							// displayName.trim().length() > 0) {
							// DatabaseHelper.getInstance(acitiviy).updateContact(localContactItem);
							// }
							// adapter.add(messageItem);
							// Log.e("tag", "message from spark ::" +
							// message.getBody() + message.getBody().length());

							adapter.notifyDataSetChangedOnUI();
							listChat.smoothScrollToPosition(adapter.getCount() - 1);
							// textTyping.setVisibility(View.INVISIBLE);
							((Vibrator) acitiviy.getSystemService("vibrator"))
									.vibrate(30L);
							// try {
							// DatabaseHelper.getInstance(acitiviy).getDao(MessageItem.class).create(messageItem);
							// return;
							// } catch (SQLException e) {
							// e.printStackTrace();
							// return;
							// }

						}
					}
				});
			}
		};

		enablePopUpView();
		checkKeyboardHeight();

	}

	@Override
	public void onResume() {
		super.onResume();

		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		if (imageEmoji != null) {
			imageEmoji.setImageResource(R.drawable.emoji_btn_normal);
		}
	}

	int previousHeightDiffrence = 0;

	private void checkKeyboardHeight() {

		llfragment_livechat.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {

						Rect r = new Rect();
						llfragment_livechat.getWindowVisibleDisplayFrame(r);
						int screenHeight = llfragment_livechat.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);

						if (Math.abs(previousHeightDiffrence - heightDifference) > 50) {
							popupWindow.dismiss();
							imageEmoji
									.setImageResource(R.drawable.emoji_btn_normal);
						}

						previousHeightDiffrence = heightDifference;
						if (heightDifference > 100) {
							isKeyBoardVisible = true;
							changeKeyboardHeight(heightDifference);
						} else {
							isKeyBoardVisible = false;
						}

					}
				});
	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 * 
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	private void changeKeyboardHeight(int height) {

		if (height > 100) {
			keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, keyboardHeight);
			emoticonsCover.setLayoutParams(params);
		}

	}

	EmoticonsPagerAdapter emojiAdapter;

	/**
	 * Defining all components of emoticons keyboard
	 */
	private void enablePopUpView() {

		final ViewPager pager = (ViewPager) popUpView
				.findViewById(R.id.emoticons_pager);
		pager.setOffscreenPageLimit(3);

		final ArrayList<EmojiItem> paths = EmojiUtil.getInstance(acitiviy)
				.getAllEmojis();
		final ArrayList<EmojiItem>[] groups = new ArrayList[5];
		for (EmojiItem emoji : paths) {
			if (groups[emoji.emojiGroup] == null) {
				groups[emoji.emojiGroup] = new ArrayList<EmojiItem>();
			}
			groups[emoji.emojiGroup].add(emoji);
		}
		final ArrayList<EmojiItem> history = new ArrayList<EmojiItem>();
		ArrayList<Integer> historyIds = SettingsUtil.getHistoryItems(acitiviy);
		for (Integer his : historyIds) {
			for (EmojiItem emoji : paths) {
				if (emoji.id == his) {
					history.add(emoji);
					break;
				}
			}
		}
		history.add(paths.get(0));

		final KeyClickListener onEmojiClick = new KeyClickListener() {

			@Override
			public void keyClickedIndex(EmojiItem index) {

				int cursorPosition = editMessage.getSelectionStart();
				editMessage.getText().insert(cursorPosition, index.emojiText);
				try {
					editMessage.getText().setSpan(
							new ImageSpan(index.emojiDrawable), cursorPosition,
							cursorPosition + 1,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {
				}
				if (history.get(0) != index)
					history.add(0, index);
				SettingsUtil.setHistoryItems(acitiviy, history);
				emojiAdapter.notifyDataSetChanged();
				pager.setAdapter(emojiAdapter);
			}
		};

		((ImageButton) popUpView.findViewById(R.id.emoji2))
				.setImageDrawable(groups[0].get(0).emojiDrawable);
		((ImageButton) popUpView.findViewById(R.id.emoji3))
				.setImageDrawable(groups[1].get(0).emojiDrawable);
		((ImageButton) popUpView.findViewById(R.id.emoji4))
				.setImageDrawable(groups[2].get(0).emojiDrawable);
		((ImageButton) popUpView.findViewById(R.id.emoji5))
				.setImageDrawable(groups[3].get(0).emojiDrawable);
		((ImageButton) popUpView.findViewById(R.id.emoji6))
				.setImageDrawable(groups[4].get(0).emojiDrawable);
		popUpView.findViewById(R.id.emoji1).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = history;
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});
		popUpView.findViewById(R.id.emoji2).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = groups[0];
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});
		popUpView.findViewById(R.id.emoji3).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = groups[1];
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});
		popUpView.findViewById(R.id.emoji4).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = groups[2];
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});
		popUpView.findViewById(R.id.emoji5).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = groups[3];
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});
		popUpView.findViewById(R.id.emoji6).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						emojiAdapter.emojis = groups[4];
						emojiAdapter.notifyDataSetChanged();
						pager.setAdapter(emojiAdapter);
					}
				});

		emojiAdapter = new EmoticonsPagerAdapter(acitiviy, groups[0],
				onEmojiClick);
		pager.setAdapter(emojiAdapter);

		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) keyboardHeight, false);

		View backSpace = (View) popUpView.findViewById(R.id.imageBackspace);
		backSpace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0,
						0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				editMessage.dispatchKeyEvent(event);
			}
		});

		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});

		ViewPager pagerStickers = (ViewPager) popUpView
				.findViewById(R.id.stickers_pager);
		pagerStickers.setOffscreenPageLimit(3);

	}

	private void showKeyboardPopup(View root, boolean attaches) {
		if (!popupWindow.isShowing()) {
			popupWindow.setHeight((int) (keyboardHeight));

			if (isKeyBoardVisible) {
				imageEmoji.setImageResource(R.drawable.emoji_kbd);
				emoticonsCover.setVisibility(LinearLayout.GONE);

			} else {
				imageEmoji.setImageResource(R.drawable.ic_down);
				emoticonsCover.setVisibility(LinearLayout.VISIBLE);
			}
			try {
				popupWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
			} catch (Exception e) {
			}
		} else {
			imageEmoji.setImageResource(R.drawable.emoji_btn_normal);
			popupWindow.dismiss();
			return;
		}

		imageAttaches.setBackgroundColor(attaches ? 0xFF808080 : 0x00000000);
		imageEmojis.setBackgroundColor(attaches ? 0x00000000 : 0xFF808080);
		imageStickers.setBackgroundColor(0x00000000);
		layoutEmojis.setVisibility(attaches ? View.GONE : View.VISIBLE);
		layoutStickers.setVisibility(View.GONE);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			try {
				finish();
			} catch (Exception e) {

			}
		}
		return false;

	}

}
