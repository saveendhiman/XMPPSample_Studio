package com.xmpp.chat.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.xmpp.chat.dao.ChatItem;
import com.xmpp.chat.dao.ContactItem;
import com.xmpp.chat.dao.MessageItem;
import com.xmpp.chat.xmpp.XMPP;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "Tutorapp.db";
	private static final int DATABASE_VERSION = 1;
	private static final Class[] daoList = { ChatItem.class, MessageItem.class,
			ContactItem.class };

	private static DatabaseHelper instance;

	private DatabaseHelper(Context paramContext) {
		super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseHelper getInstance(Context paramContext) {
		if (instance == null) {
			instance = new DatabaseHelper(paramContext);
		}
		return instance;
	}

	public String getDisplayName(Context context, String bareAddress) {
		List<ContactItem> chats = null;
		try {
			chats = getDao(ContactItem.class).queryForEq("username",
					StringUtils.parseName(bareAddress));
		} catch (SQLException e) {
		}
		if (chats != null && chats.size() > 0) {
			return chats.get(0).displayName;
		} else {
			VCard card = new VCard();
			try {
				card.load(XMPP.getInstance().getConnection(context),
						bareAddress);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (card == null || (card.getNickName() == null)) {
				// return "Anonymous user";
				return "Live user";
			} else {
				return card.getNickName();
			}
		}
		// return username;
	}

	public void onCreate(SQLiteDatabase paramSQLiteDatabase,
			ConnectionSource paramConnectionSource) {
		for (int i = 0; i < daoList.length; i++) {
			try {
				TableUtils.createTable(paramConnectionSource, daoList[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase,
			ConnectionSource paramConnectionSource, int paramInt1, int paramInt2) {
		for (int i = 0; i < daoList.length; i++) {
			try {
				TableUtils.dropTable(paramConnectionSource, daoList[i], true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < daoList.length; i++) {
			try {
				TableUtils.createTable(paramConnectionSource, daoList[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateContact(final ContactItem contact) {
		try {
			TransactionManager.callInTransaction(getConnectionSource(),
					new Callable<Void>() {
						public Void call() throws Exception {
							List<ContactItem> contacts = DatabaseHelper.this
									.getDao(ContactItem.class).queryForEq(
											"username", contact.username);
							if (contacts != null)
								DatabaseHelper.this.getDao(ContactItem.class)
										.createOrUpdate(contact);
							return null;
						}
					});
			return;
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
		}
	}

	public void updateContactStatus(final String jid, final String status,
			final int mood) {
		try {
			TransactionManager.callInTransaction(getConnectionSource(),
					new Callable<Void>() {
						public Void call() throws Exception {

							UpdateBuilder<ContactItem, ?> builder = getDao(
									ContactItem.class).updateBuilder();
							builder.updateColumnValue("status", status);
							builder.updateColumnValue("mood", mood);
							builder.where().eq("username",
									StringUtils.parseName(jid));
							getDao(ContactItem.class).update(builder.prepare());

							return (Void) null;
						}
					});
			return;
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
		}
	}

	public ContactItem getContact(String jid) {
		try {
			List<ContactItem> list = getDao(ContactItem.class).queryForEq(
					"username", StringUtils.parseName(jid));
			if (list.size() > 0) {
				return list.get(0);
			}
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
		}
		return null;
	}

	public void updateContacts(final Collection<ContactItem> contactList) {
		if (contactList == null || contactList.size() == 0)
			return;
		try {
			TransactionManager.callInTransaction(getConnectionSource(),
					new Callable<Void>() {
						public Void call() throws Exception {
							Dao<ContactItem, ?> localDao = DatabaseHelper.this
									.getDao(ContactItem.class);
							for (ContactItem contact : contactList) {
								localDao.createOrUpdate(contact);
							}
							return null;
						}
					});
			return;
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();
		}
	}

	public void drop() {
		for (int i = 0; i < daoList.length; i++) {
			try {
				TableUtils.dropTable(getConnectionSource(), daoList[i], true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeChat(ChatItem chat) {
		try {
			getDao(ChatItem.class).delete(chat);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}