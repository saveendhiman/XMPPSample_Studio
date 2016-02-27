package com.xmpp.chat.adapter;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import com.example.xmppsample.R;
import com.xmpp.chat.dao.ChatItem;
import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.data.app.LiveApp;
import com.xmpp.chat.framework.LiveUtil;
import com.xmpp.chat.util.EmojiUtil;
import com.xmpp.chat.xmpp.XMPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LiveChatAdapter extends BaseAdapter {
	private Activity context;
	private List<MessageItem> items;
	private String displayName;
	private boolean groupChat;
	ChatItem chatItem;

	public LiveChatAdapter(Activity paramContext, ChatItem chatItem, List<MessageItem> paramList, String displayName,
			boolean groupChat) {
		this.items = paramList;
		this.context = paramContext;
		this.displayName = displayName;
		this.groupChat = groupChat;
		this.chatItem = chatItem;
	}

	private int[] COLORS = new int[] { 0xFFFF0000, 0xFFcc8800, 0xFFFF00FF, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF };
	HashMap<String, Integer> colorsFrom = new HashMap<String, Integer>();
	int currentColor = 0;

	public void notifyDataSetChangedOnUI() {
		context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

	public void add(final MessageItem paramMessageItem) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				items.add(paramMessageItem);
				notifyDataSetChanged();
			}
		});

	}

	public int getCount() {
		return this.items.size();
	}

	public MessageItem getItem(int paramInt) {
		return (MessageItem) this.items.get(paramInt);
	}

	public long getItemId(int paramInt) {
		return 0L;
	}

	public View getView(final int pos, View view, ViewGroup paramViewGroup) {
		if (view == null) {
			final View v = LayoutInflater.from(this.context).inflate(R.layout.item_livechat, null);
			view = v;
			final ImageView imageDone = (ImageView) view.findViewById(R.id.imageProgressDone);
			view.findViewById(R.id.imageProgressResend).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					final MessageItem item = (MessageItem) arg0.getTag();
					if (item != null && item.file != null) {
						final File file = new File(item.file);
						// if (file.exists())
						// return;
						final FileTransferManager fileManager = new FileTransferManager(
								XMPP.getInstance().getConnection(context));
						item.progress = 0;
						notifyDataSetChanged();
						final ArrayList<String> membersSent = new ArrayList<String>();
						new Thread(new Runnable() {

							@Override
							public void run() {
								OutgoingFileTransfer transfer = fileManager
										.createOutgoingFileTransfer(chatItem.jid + "/Smack");
								if (chatItem.isGroup) {
									
								} else {
									LiveApp.get().putFileTransferOut(item.file, transfer);
									try {
										transfer.sendFile(new File(item.file), "");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								while (!transfer.isDone()) {
									if (transfer.getStatus().equals(Status.error)) {
										System.out.println("ERROR!!! " + transfer.getError());
									} else if (transfer.getStatus().equals(Status.cancelled)
											|| transfer.getStatus().equals(Status.refused)) {
										System.out.println("Cancelled!!! " + transfer.getError());
									}
									Status finalStatus = transfer.getStatus();
									try {
										Thread.sleep(1000L);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									final int progress = transfer.getFileSize() <= 0 ? -1
											: (int) (transfer.getAmountWritten() * 100 / transfer.getFileSize());
									if (progress != item.progress) {
										item.progress = progress;
										notifyDataSetChangedOnUI();
										try {
											DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(item);
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
								}

								if (transfer.getStatus() == Status.complete || chatItem.isGroup) {

									if (context != null)
										context.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												Toast.makeText(context, "File sent!", Toast.LENGTH_SHORT).show();
											}
										});
									item.progress = 100;
									try {
										DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(item);
									} catch (SQLException e) {
										e.printStackTrace();
									}
									notifyDataSetChangedOnUI();
								} else {
									if (context != null)
										context.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												try {
													Toast.makeText(context, "Sending failed!", Toast.LENGTH_SHORT)
															.show();
												} catch (Exception e) {
												}
											}
										});
									item.progress = -1;
									notifyDataSetChangedOnUI();
									try {
										DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(item);
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}

								ServiceDiscoveryManager sdm = ServiceDiscoveryManager
										.getInstanceFor(XMPP.getInstance().getConnection(context));
								sdm.addFeature("jabber.org/protocol/si");
								sdm.addFeature("http://jabber.org/protocol/si");
								sdm.addFeature("http://jabber.org/protocol/disco#info");
								sdm.addFeature("jabber:iq:privacy");

								FileTransferNegotiator.setServiceEnabled(XMPP.getInstance().getConnection(context),
										true);
								FileTransferNegotiator.IBB_ONLY = true;

								Status finalStatus = transfer.getStatus();
								System.out.println(finalStatus.toString());
							}
						}).start();
					}
				}
			});
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View arg0) {
					final MessageItem item = (MessageItem) arg0.getTag();
					if (item.file != null) {
						final File file = new File(item.file);
						if (item.outFile && item.progress != 100) {
							AlertDialog.Builder dialog = new AlertDialog.Builder(context);
							dialog.setMessage("Are you sure you want to cancel transfer?")
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Toast.makeText(context, "Transfer canceled!", Toast.LENGTH_LONG).show();

									LiveApp.get().getFileTransferOut(item.file).cancel();

									new Handler(Looper.getMainLooper()).post(new Runnable() {

										@Override
										public void run() {
											items.remove(item);
											notifyDataSetChanged();
										}
									});
									notifyDataSetChanged();
								}
							}).setNegativeButton("Cancel", null).show();
							return;
						}
						if (file.exists() && item.progress == 100) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							String mime = MimeTypeMap.getSingleton()
									.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(item.file));
							intent.setDataAndType(Uri.fromFile(file), mime);
							try {
								context.startActivity(intent);
							} catch (Exception e) {
								Toast.makeText(context, "No application found to open this file", Toast.LENGTH_LONG)
										.show();
							}
						}
					}
				}
			});
		}

		final View v = view;
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressDownload);
		final ImageView imageDone = (ImageView) view.findViewById(R.id.imageProgressDone);
		View background = view.findViewById(R.id.layoutMessage);
		final ImageView imageDownload = (ImageView) v.findViewById(R.id.imageDownload);
		final ImageView imagePlay = (ImageView) v.findViewById(R.id.imagePlay);
		final View layoutPlay = v.findViewById(R.id.layoutVideo);

		// ((View) background.getParent()).setBackgroundColor(
		// context.getResources().getColor(getItem(pos).outMessage ?
		// R.color.blue_light : R.color.white));

		// ((View) background.getParent()).setBackgroundResource(
		// context.getResources().getColor(getItem(pos).outMessage ?
		// R.color.blue_light : R.color.white));

		// ((View) background.getParent())
		// .setBackgroundResource(getItem(pos).outMessage ?
		// R.drawable.single_chat_background : R.color.white);
		((View) background.getParent()).setBackgroundResource(
				getItem(pos).outMessage ? R.drawable.single_chat_background : R.drawable.single_chat_background);

		((View) view.findViewById(R.id.imageDownload).getParent()).setBackgroundColor(
				context.getResources().getColor(getItem(pos).outMessage ? R.color.blue_light : R.color.white));
		view.findViewById(R.id.arrowLeft).setVisibility(getItem(pos).outMessage ? View.VISIBLE : View.INVISIBLE);
		view.findViewById(R.id.arrowRight).setVisibility(getItem(pos).outMessage ? View.INVISIBLE : View.VISIBLE);

		// imagePlay.setVisibility(View.GONE);
		layoutPlay.setVisibility(View.GONE);
		if (getItem(pos).outFile && getItem(pos).progress == -1) {
			view.findViewById(R.id.progressDownload).setVisibility(View.GONE);
			view.findViewById(R.id.imageProgressResend).setVisibility(View.VISIBLE);
			imageDone.setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.progressDownload).setVisibility(View.VISIBLE);
			view.findViewById(R.id.imageProgressResend).setVisibility(View.GONE);
			imageDone.setVisibility(View.VISIBLE);
		}

		if (getItem(pos).file != null) {
			imageDownload.setVisibility(View.VISIBLE);
			final File file = new File(getItem(pos).file);
			if (file.exists() && !getItem(pos).outFile) {
				imageDone.setVisibility(View.VISIBLE);
				imageDone.setBackgroundColor(0xFF00aa00);
				imageDone.setImageResource(android.R.drawable.ic_input_add);
				progressBar.setProgress(100);
				progressBar.setVisibility(View.GONE);
				getItem(pos).progress = 100;
				imageDone.setVisibility(View.GONE);
				try {
					DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(getItem(pos));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (getItem(pos).outFile && getItem(pos).progress == -1) {
				imageDone.setVisibility(View.VISIBLE);
				imageDone.setBackgroundColor(0xFFaa0000);
				imageDone.setImageResource(android.R.drawable.ic_delete);
			} else if (getItem(pos).outFile && getItem(pos).progress == 100) {
				imageDone.setVisibility(View.VISIBLE);
				imageDone.setBackgroundColor(0xFF00aa00);
				imageDone.setImageResource(android.R.drawable.ic_input_add);
				imageDone.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
			}
			if (file.exists() || !getItem(pos).outFile) {
				if (getItem(pos).progress == 100) {
					v.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
				} else {
					v.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
				}
			}

			if (file.exists() && (getItem(pos).outFile || getItem(pos).progress == 100)
					&& (file.getName().endsWith("jpg") || file.getName().endsWith("png")
							|| file.getName().endsWith("bmp"))) {
				if (getItem(pos).bitmap == null) {
					getItem(pos).bitmap = LiveUtil.decodeFile(file, 32);
				}
				if (getItem(pos).bitmap == null) {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				} else {
					imageDownload.setImageBitmap(getItem(pos).bitmap);
				}
			} else {
				String mime = MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));
				if (mime == null) {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				} else if (mime.contains("audio")) {
					imageDownload.setImageResource(R.drawable.ic_audio);
				} else if (mime.contains("video")) {
					imageDownload.setImageResource(R.drawable.ic_video);
					if (getItem(pos).bitmap == null) {
						Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), 0);
						if (thumbnail != null) {
							getItem(pos).bitmap = thumbnail;
						}
					}
					if (getItem(pos).bitmap != null) {
						imageDownload.setImageBitmap(getItem(pos).bitmap);
						MediaMetadataRetriever retriever = new MediaMetadataRetriever();
						retriever.setDataSource(getItem(pos).file);
						String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
						long timeInmillisec = Long.parseLong(time);
						long duration = timeInmillisec / 1000;
						long hours = duration / 3600;
						long minutes = (duration - hours * 3600) / 60;
						long seconds = duration - (hours * 3600 + minutes * 60);
						TextView textLength = (TextView) view.findViewById(R.id.videoLength);
						textLength.setText(String.format("%02d:%02d", hours, minutes, seconds));
					}
					// imagePlay.setVisibility(View.VISIBLE);
					layoutPlay.setVisibility(View.VISIBLE);
				} else {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				}
				view.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
			}
		} else {
			view.findViewById(R.id.imageDownload).setVisibility(View.GONE);
			view.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
		}
		TextView textMessage = (TextView) view.findViewById(R.id.text1);
		TextView textWhoSent = (TextView) view.findViewById(R.id.textWhoSent);
		TextView textTime = (TextView) view.findViewById(R.id.textTime);
		textMessage.setText(getItem(pos).file != null ? ""
				: EmojiUtil.getInstance(context).processEmoji(getItem(pos).message, textMessage.getTextSize()));
		String who = getItem(pos).outMessage ? "You" : getItem(pos).opponentDisplay;
		((LinearLayout) view.findViewById(R.id.layoutTop))
				.setGravity(getItem(pos).outMessage ? Gravity.LEFT : Gravity.RIGHT);
		// LinearLayout.LayoutParams params = (LayoutParams)
		// textMessage.getLayoutParams();
		// params.gravity = Gravity.RIGHT;
		// textMessage.setLayoutParams(params);
		textTime.setText(DateFormat.getTimeFormat(this.context).format(new Date(getItem(pos).timestamp)));
		textWhoSent.setVisibility(groupChat ? View.VISIBLE : View.GONE);
		textWhoSent.setText((groupChat ? (who) : ""));
		int color = 0;
		if (getItem(pos).outMessage) {
			color = 0xff00a0e3;
		} else {
			if (colorsFrom.containsKey(getItem(pos).opponentDisplay)) {
				color = colorsFrom.get(getItem(pos).opponentDisplay);
			} else {
				color = COLORS[currentColor++ % COLORS.length];
				colorsFrom.put(getItem(pos).opponentDisplay, color);
			}
		}

		textWhoSent.setTextColor(color);
		view.setTag(getItem(pos));
		view.findViewById(R.id.imageProgressResend).setTag(getItem(pos));
		progressBar.setProgress(getItem(pos).progress);

		if (getItem(pos).file != null) {
			final File file = new File(getItem(pos).file);
			final IncomingFileTransfer request = LiveApp.get().getFileTransfer(getItem(pos).file);
			imageDownload.setVisibility(View.VISIBLE);
			if (getItem(pos).outFile) {
				imageDone.setVisibility(View.VISIBLE);
				imageDone.setBackgroundColor(getItem(pos).progress == 100 ? 0xFF00aa00 : 0xFFaa0000);
				imageDone.setImageResource(
						getItem(pos).progress == 100 ? android.R.drawable.ic_input_add : android.R.drawable.ic_delete);
				if (getItem(pos).progress == 100) {
					progressBar.setProgress(100);
					progressBar.setVisibility(View.GONE);
					imageDone.setVisibility(View.GONE);
					getItem(pos).progress = 100;
					try {
						DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(getItem(pos));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (getItem(pos).progress == -1) {
					// Toast.makeText(context, "Sending failed!",
					// Toast.LENGTH_SHORT).show();
					progressBar.setProgress(-1);
					v.findViewById(R.id.progressDownload).setVisibility(View.GONE);
					v.findViewById(R.id.imageProgressResend).setVisibility(View.VISIBLE);
					imageDone.setVisibility(View.GONE);
					getItem(pos).progress = -1;
					try {
						DatabaseHelper.getInstance(context).getDao(MessageItem.class).update(getItem(pos));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else if (request != null || getItem(pos).progress != 100) {

				v.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
				if (request != null) {
					final int progress = request == null ? getItem(pos).progress
							: (int) (request.getAmountWritten() * 100 / request.getFileSize());
					imageDone.setVisibility(View.VISIBLE);
					imageDone.setBackgroundColor(progress == 100 ? 0xFF00aa00 : 0xFFaa0000);
					imageDone.setImageResource(
							progress == 100 ? android.R.drawable.ic_input_add : android.R.drawable.ic_delete);
					if (progress == 100) {
						v.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
					}

					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(progress);
					if (request.getStatus() == Status.complete || getItem(pos).progress == 100) {
						imageDone.setVisibility(View.GONE);
					} else if (request.getStatus() == Status.cancelled) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {

							@Override
							public void run() {
								items.remove(getItem(pos));
								notifyDataSetChanged();
							}
						});
					}
				} else if (request == null && getItem(pos).progress != 100) {
					imageDone.setVisibility(View.VISIBLE);
					imageDone.setBackgroundColor(0xFFaa0000);
					imageDone.setImageResource(android.R.drawable.ic_delete);
				}
			} else if (file.exists() && getItem(pos).progress == 100 && (file.getName().endsWith("jpg")
					|| file.getName().endsWith("png") || file.getName().endsWith("bmp"))) {
				if (getItem(pos).bitmap == null) {
					getItem(pos).bitmap = LiveUtil.decodeFile(file, 32);
				}
				progressBar.setVisibility(View.GONE);
				v.findViewById(R.id.layoutProgress).setVisibility(View.GONE);
				if (getItem(pos).bitmap != null) {
					imageDownload.setImageBitmap(getItem(pos).bitmap);
				} else {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				}
			} else {
				String mime = MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));
				if (mime == null) {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				} else if (mime.contains("audio")) {
					imageDownload.setImageResource(R.drawable.ic_audio);
				} else if (mime.contains("video")) {
					imageDownload.setImageResource(R.drawable.ic_video);
					if (getItem(pos).bitmap == null) {
						Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), 0);
						if (thumbnail != null) {
							getItem(pos).bitmap = thumbnail;
						}
					}
					if (getItem(pos).bitmap != null) {
						imageDownload.setImageBitmap(getItem(pos).bitmap);
						MediaMetadataRetriever retriever = new MediaMetadataRetriever();
						retriever.setDataSource(getItem(pos).file);
						String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
						long timeInmillisec = Long.parseLong(time);
						long duration = timeInmillisec / 1000;
						long hours = duration / 3600;
						long minutes = (duration - hours * 3600) / 60;
						long seconds = duration - (hours * 3600 + minutes * 60);
						TextView textLength = (TextView) view.findViewById(R.id.videoLength);
						textLength.setText(String.format("%02d:%02d", hours, minutes));
					}
					// imagePlay.setVisibility(View.VISIBLE);
					layoutPlay.setVisibility(View.VISIBLE);
				} else {
					imageDownload.setImageResource(android.R.drawable.ic_menu_save);
				}
				view.findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
			}
		}
		return view;
	}
}