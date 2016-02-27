package com.xmpp.chat.framework;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;

import com.example.xmppsample.R;
import com.xmpp.chat.dao.ContactItem;
import com.xmpp.chat.data.DatabaseHelper;
import com.xmpp.chat.xmpp.XMPP;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

public class LiveUtil {

	private static int[] ICONS = new int[] { R.drawable.mood_1, R.drawable.mood_2, R.drawable.mood_3, R.drawable.mood_4,
			R.drawable.mood_5, R.drawable.mood_6 };

	public static int getMoodCount() {
		return ICONS.length;
	}

	public static int getMoodRes(int mood) {
		int res = mood == -1 ? 0 : mood;
		return ICONS[res];
	}

	public static String getRandomHash(Context context) {
		String digest = XMPP.getInstance().getConnection(context).getUser() + Calendar.getInstance().getTimeInMillis();
		String chatId = null;
		try {
			chatId = new BigInteger(1, MessageDigest.getInstance("MD5").digest(digest.getBytes())).toString(16);
		} catch (NoSuchAlgorithmException e) {
		}
		return chatId;
	}

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

//	public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height)
//			throws WriterException {
//		String contentsToEncode = contents;
//		if (contentsToEncode == null) {
//			return null;
//		}
//		Map<EncodeHintType, Object> hints = null;
//		String encoding = guessAppropriateEncoding(contentsToEncode);
//		if (encoding != null) {
//			hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
//			hints.put(EncodeHintType.CHARACTER_SET, encoding);
//		}
//		MultiFormatWriter writer = new MultiFormatWriter();
//		BitMatrix result;
//		try {
//			result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
//		} catch (IllegalArgumentException iae) {
//			// Unsupported format
//			return null;
//		}
//		int width = result.getWidth();
//		int height = result.getHeight();
//		int[] pixels = new int[width * height];
//		for (int y = 0; y < height; y++) {
//			int offset = y * width;
//			for (int x = 0; x < width; x++) {
//				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
//			}
//		}
//
//		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//		return bitmap;
//	}

	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

	public static HashMap<String, ContactItem> syncContacts(Context context) {
		HashMap<String, ContactItem> allContacts = getContacts(context);
		boolean firstRun = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("firstSync", true);
		if (firstRun) {
			PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("firstSync", false).commit();
			DatabaseHelper.getInstance(context).updateContacts(allContacts.values());
		}
		return allContacts;
	}

	public static Bitmap decodeFile(File f, int maxSize) {
		// decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f.getAbsolutePath(), o);
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < maxSize || height_tmp / 2 < maxSize)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale++;
		}

		// decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
	}

	public static HashMap<String, ContactItem> getContacts(Context activity) {
		HashMap<String, ContactItem> alContacts = new HashMap<String, ContactItem>();
		Cursor cursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
				null);
		if (cursor.moveToFirst()) {
			do {
				String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

				if (Integer.parseInt(
						cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = activity.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String contactNumber = pCur
								.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String displayName = pCur
								.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						contactNumber = "p" + contactNumber.replaceAll("[^\\d]", "");
						ContactItem contact = new ContactItem();
						contact.username = contactNumber;
						contact.displayName = displayName;
						contact.isRegistered = false;
						contact.isShowHome = true;
						alContacts.put(contact.username, contact);
						break;
					}
					pCur.close();
				}

			} while (cursor.moveToNext());
		}
		return alContacts;
	}
}
