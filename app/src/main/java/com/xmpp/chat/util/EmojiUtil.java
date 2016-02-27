package com.xmpp.chat.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import com.xmpp.chat.dao.EmojiItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.webkit.MimeTypeMap;

public class EmojiUtil {

	public static String getFileType(File file) {
		String mime = MimeTypeMap.getSingleton()
				.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));

		if (mime != null) {
			if (mime.contains("image")) {
				return "Image file";
			} else if (mime.contains("audio")) {
				return "Audio file";
			} else if (mime.contains("video")) {
				return "Video file";
			}
		}
		return "Image file";
	}

	private static final String EMOJI_INDEX = "😄-😊-😃-☺-😉-😍-😘-😚-😳-😌-😁-😜-😝-😒-😏-😓-😔-😞-😖-😥-😰\n"
			+ "😨-😣-😢-😭-😂-😲-😱-😠-😡-😪-😷-👿-👽-💛-💙-💜-💗-💚-❤-💔-💓\n"
			+ "💘-✨-🌟-💢-❕-❔-💤-💨-💦-🎶-🎵-🔥-💩-👍-👎-👌-👊-✊-✌-👋-✋\n"
			+ "👐-👆-👇-👉-👈-🙌-🙏-☝-👏-💪-🚶-🏃-👫-💃-👯-🙆-🙅-💁-🙇-💏-💑\n"
			+ "💆-💇-💅-👦-👧-👩-👨-👶-👵-👴-👱-👲-👳-👷-👮-👼-👸-💂-💀-👣-💋\n"
			+ "👄-👂-👀-👃-❌-😆-😎-😟-😩-😕-😇-😮-😴-😈-😋-😐\n"
			+ "☀-☔-☁-⛄-🌙-⚡-🌀-🌊-🐱-🐶-🐭-🐹-🐰-🐺-🐸-🐯-🐨-🐻-🐷-🐮-🐗\n"
			+ "🐵-🐒-🐴-🐎-🐫-🐑-🐘-🐍-🐦-🐤-🐔-🐧-🐛-🐙-🐠-🐟-🐳-🐬-💐-🌸-🌷\n" + "🍀-🌹-🌻-🌺-🍁-🍃-🍂-🌴-🌵-🌾-🐚\n"
			+ "🎍-💝-🎎-🎒-🎓-🎏-🎆-🎇-🎐-🎑-🎃-👻-🎅-🎄-🎁-🔔-🎉-🎈-💿-📀-📷\n"
			+ "🎥-💻-📺-📱-📠-☎-💽-📼-🔊-📢-📣-📻-📡-➿-🔍-🔓-🔒-🔑-✂-🔨-💡\n"
			+ "📲-📩-📫-📮-🛀-🚽-💺-💰-🔱-🚬-💣-🔫-💊-💉-🏈-🏀-⚽-⚾-🎾-⛳-🎱\n"
			+ "🏊-🏄-🎿-♠-♥-♣-♦-🏆-👾-🎯-🀄-🎬-📝-📖-🎨-🎤-🎧-🎺-🎷-🎸-〽\n"
			+ "👟-👡-👠-👢-👕-👔-👗-👘-👙-🎀-🎩-👑-👒-🌂-💼-👜-💄-💍-💎-☕-🍵\n"
			+ "🍺-🍻-🍸-🍶-🍴-🍔-🍟-🍝-🍛-🍱-🍣-🍙-🍘-🍚-🍜-🍲-🍞-🍳-🍢-🍡-🍦\n" + "🍧-🎂-🍰-🍎-🍊-🍉-🍓-🍆-🍅\n"
			+ "🏠-🏫-🏢-🏣-🏥-🏦-🏪-🏩-🏨-💒-⛪-🏬-🌇-🌆-🏧-🏯-🏰-⛺-🏭-🗼-🗻\n"
			+ "🌄-🌅-🌃-🗽-🌈-🎡-⛲-🎢-🚢-🚤-⛵-✈-🚀-🚲-🚙-🚗-🚕-🚌-🚓-🚒-🚑\n"
			+ "🚚-🚃-🚉-🚄-🚅-🎫-⛽-🚥-⚠-🚧-🔰-🎰-🚏-💈-♨-🏁-🎌-🇯-🇵-🇰-🇷-🇨-🇳-🇺🇸\n"
			+ "🇫-🇷-🇪-🇸-🇮-🇹-🇷-🇺-🇬-🇧-🇩-🇪\n"
			+ "1-⃣-2-⃣-3-⃣-4-⃣-5-⃣-6-⃣-7-⃣-8-⃣-9-⃣-0-⃣-#-⃣-⬆-⬇-⬅-➡-↗-↖-↘-↙-◀-▶\n"
			+ "⏪-⏩-🆗-🆕-🔝-🆙-🆒-🎦-🈁-📶-🚻-🚹-🚺-🚼-🚭-🅿-♿-🚇-🚾-🔞-🆔\n"
			+ "✴-💟-🆚-📳-📴-💹-💱-♈-♉-♊-♋-♌-♍-♎-♏-♐-♑-♒-♓-⛎\n"
			+ "🔯-🅰-🅱-🆎-🅾-🔲-🔴-🔳-🕛-🕐-🕑-🕒-🕓-🕔-🕕-🕖-🕗-🕘-🕙-🕚-⭕";

	private HashMap<String, SoftReference<Drawable>> emojiDrawables;

	public ArrayList<EmojiItem> allEmojis;

	public ArrayList<EmojiItem> getAllEmojis() {
		if (allEmojis == null) {
			allEmojis = new ArrayList<EmojiItem>();

			String[] index = EMOJI_INDEX.split("\n");
			int id = 0;
			int group = 0;
			for (int i = 0; i < index.length; i++) {
				String[] emojis = index[i].split("-");
				if (i == 6 || i == 9 || i == 16 || i == 20)
					group++;
				for (int j = 0; j < emojis.length; j++) {
					EmojiItem emoji = new EmojiItem();
					emoji.emojiText = emojis[j];
					emoji.id = id++;
					emoji.emojiGroup = group;
					RegionDrawable drawable = new RegionDrawable(emojiImages, (Rect) emojiRects.get(emojis[j]));
					drawable.setBounds(0, 0, 30, 30);
					emoji.emojiDrawable = drawable;
					allEmojis.add(emoji);
				}
			}
		}
		return allEmojis;
	}

	private Bitmap emojiImages;
	private HashMap<String, Rect> emojiRects;
	private boolean isHdpi = false;
	private float density;
	private Context context;

	private EmojiUtil(Context context) {
		this.context = context;
		density = context.getResources().getDisplayMetrics().density;
		try {
			if (density >= 1.5f) {
				this.isHdpi = true;
				InputStream localInputStream = context.getAssets().open("emoji/emoji_2x.png");
				Options opts = new Options();
				opts.inPurgeable = true;
				opts.inInputShareable = true;
				emojiImages = BitmapFactory.decodeStream(localInputStream, null, opts);
			}
			String[] index = EMOJI_INDEX.split("\n");
			emojiRects = new HashMap<String, Rect>();
			emojiDrawables = new HashMap<String, SoftReference<Drawable>>();
			for (int i = 0; i < index.length; i++) {
				String[] emojis = index[i].split("-");
				for (int j = 0; j < emojis.length; j++) {
					emojiRects.put(emojis[j], new Rect(j * 40, i * 40, 40 * (j + 1), 40 * (i + 1)));
				}
			}
		} catch (IOException localIOException) {

		}
	}

	private static EmojiUtil instance;

	public static EmojiUtil getInstance(Context context) {
		if (instance == null)
			instance = new EmojiUtil(context);
		return instance;
	}

	public Spanned processEmoji(String paramString, final float size) {
		return processEmoji(paramString, (int) size, false);
	}

	public Spanned processEmoji(String paramString, final int size) {
		return processEmoji(paramString, size, true);
	}

	public Spanned processEmoji(String paramString, final int size, final boolean useDensity) {

		if (emojiImages == null || emojiImages.isRecycled()) {
			InputStream localInputStream;
			try {
				localInputStream = context.getAssets().open("emoji/emoji_2x.png");
				Options opts = new Options();
				opts.inPurgeable = true;
				opts.inInputShareable = true;
				emojiImages = BitmapFactory.decodeStream(localInputStream, null, opts);
			} catch (IOException e) {
				return Html.fromHtml(paramString);
			}
		}
		// String str =
		// "😄😊😃☺😉😍😘😚😳😌😁😜😝😒😏😓😔😞😖😥😰😨😣😢😭😂😲😱😠😡😪😷👿👽💛💙💜💗💚❤💔💓💘✨🌟💢❕❔💤💨💦🎶🎵🔥💩👍👎👌👊✊✌👋✋👐👆👇👉👈🙌🙏☝👏💪🚶🏃👫💃👯🙆🙅💁🙇💏💑💆💇💅👦👧👩👨👶👵👴👱👲👳👷👮👼👸💂💀👣💋👄👂👀👃❌😆😎😟😩😕😇😮😴😈😋😐☀☔☁⛄🌙⚡🌀🌊🐱🐶🐭🐹🐰🐺🐸🐯🐨🐻🐷🐮🐗🐵🐒🐴🐎🐫🐑🐘🐍🐦🐤🐔🐧🐛🐙🐠🐟🐳🐬💐🌸🌷🍀🌹🌻🌺🍁🍃🍂🌴🌵🌾🐚🎍💝🎎🎒🎓🎏🎆🎇🎐🎑🎃👻🎅🎄🎁🔔🎉🎈💿📀📷🎥💻📺📱📠☎💽📼🔊📢📣📻📡➿🔍🔓🔒🔑✂🔨💡📲📩📫📮🛀🚽💺💰🔱🚬💣🔫💊💉🏈🏀⚽⚾🎾⛳🎱🏊🏄🎿♠♥♣♦🏆👾🎯🀄🎬📝📖🎨🎤🎧🎺🎷🎸〽👟👡👠👢👕👔👗👘👙🎀🎩👑👒🌂💼👜💄💍💎☕🍵🍺🍻🍸🍶🍴🍔🍟🍝🍛🍱🍣🍙🍘🍚🍜🍲🍞🍳🍢🍡🍦🍧🎂🍰🍎🍊🍉🍓🍆🍅🏠🏫🏢🏣🏥🏦🏪🏩🏨💒⛪🏬🌇🌆🏧🏯🏰⛺🏭🗼🗻🌄🌅🌃🗽🌈🎡⛲🎢🚢🚤⛵✈🚀🚲🚙🚗🚕🚌🚓🚒🚑🚚🚃🚉🚄🚅🎫⛽🚥⚠🚧🔰🎰🚏💈♨🏁🎌⬆⬇⬅➡↗↖↘↙◀▶⏪⏩🆗🆕🔝🆙🆒🎦🈁📶🚻🚹🚺🚼🚭🅿♿🚇🚾🔞🆔✴💟🆚📳📴💹💱♈♉♊♋♌♍♎♏♐♑♒♓⛎🔯🅰🅱🆎🅾🔲🔴🔳🕛🕐🕑🕒🕓🕔🕕🕖🕗🕘🕙🕚⭕😋🙍🔁🌝";
		// SpannableStringBuilder sb = new SpannableStringBuilder(paramString);
		// for(int i = 0;i<sb.length();i++) {
		// int start = 0;
		// int index = 0;
		// while((index=str.indexOf(sb.charAt(i), start))>=0) {
		// start = start+1;
		// String str1 = str.substring(index, index+1);
		// Drawable d = getDrawable(str1, useDensity, size);
		// if(d!=null) {
		// sb.setSpan(new ImageSpan(d), i, i+1,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// }
		// }
		// }
		// return sb;
		String emojiString = paramString.replaceAll("\n", "<br/>").replaceAll(
				"([😄😊😃☺😉😍😘😚😳😌😁😜😝😒😏😓😔😞😖😥😰😨😣😢😭😂😲😱😠😡😪😷👿👽💛💙💜💗💚❤💔💓💘✨🌟💢❕❔💤💨💦🎶🎵🔥💩👍👎👌👊✊✌👋✋👐👆👇👉👈🙌🙏☝👏💪🚶🏃👫💃👯🙆🙅💁🙇💏💑💆💇💅👦👧👩👨👶👵👴👱👲👳👷👮👼👸💂💀👣💋👄👂👀👃❌😆😎😟😩😕😇😮😴😈😋😐☀☔☁⛄🌙⚡🌀🌊🐱🐶🐭🐹🐰🐺🐸🐯🐨🐻🐷🐮🐗🐵🐒🐴🐎🐫🐑🐘🐍🐦🐤🐔🐧🐛🐙🐠🐟🐳🐬💐🌸🌷🍀🌹🌻🌺🍁🍃🍂🌴🌵🌾🐚🎍💝🎎🎒🎓🎏🎆🎇🎐🎑🎃👻🎅🎄🎁🔔🎉🎈💿📀📷🎥💻📺📱📠☎💽📼🔊📢📣📻📡➿🔍🔓🔒🔑✂🔨💡📲📩📫📮🛀🚽💺💰🔱🚬💣🔫💊💉🏈🏀⚽⚾🎾⛳🎱🏊🏄🎿♠♥♣♦🏆👾🎯🀄🎬📝📖🎨🎤🎧🎺🎷🎸〽👟👡👠👢👕👔👗👘👙🎀🎩👑👒🌂💼👜💄💍💎☕🍵🍺🍻🍸🍶🍴🍔🍟🍝🍛🍱🍣🍙🍘🍚🍜🍲🍞🍳🍢🍡🍦🍧🎂🍰🍎🍊🍉🍓🍆🍅🏠🏫🏢🏣🏥🏦🏪🏩🏨💒⛪🏬🌇🌆🏧🏯🏰⛺🏭🗼🗻🌄🌅🌃🗽🌈🎡⛲🎢🚢🚤⛵✈🚀🚲🚙🚗🚕🚌🚓🚒🚑🚚🚃🚉🚄🚅🎫⛽🚥⚠🚧🔰🎰🚏💈♨🏁🎌⬆⬇⬅➡↗↖↘↙◀▶⏪⏩🆗🆕🔝🆙🆒🎦🈁📶🚻🚹🚺🚼🚭🅿♿🚇🚾🔞🆔✴💟🆚📳📴💹💱♈♉♊♋♌♍♎♏♐♑♒♓⛎🔯🅰🅱🆎🅾🔲🔴🔳🕛🕐🕑🕒🕓🕔🕕🕖🕗🕘🕙🕚⭕😋🙍🔁🌝]|[🇯🇵🇰🇷🇨🇳🇺🇸🇫🇷🇪🇸🇮🇹🇷🇺🇬🇧🇩🇪]{2}|[0-9#]⃣)",
				"<img src='$1'/>");
		return Html.fromHtml(emojiString, new Html.ImageGetter() {
			public Drawable getDrawable(String paramAnonymousString) {
				try {
					if (emojiRects.containsKey(paramAnonymousString)) {
						if (emojiDrawables.containsKey(paramAnonymousString)
								&& emojiDrawables.get(paramAnonymousString).get() != null) {
							RegionDrawable drawable = (RegionDrawable) emojiDrawables.get(paramAnonymousString).get();
							if (drawable.source == null || drawable.source.isRecycled()) {
								drawable.source = emojiImages;
							}
							if (useDensity)
								drawable.setBounds(0, 0, (int) (size * density), (int) (size * density));
							else
								drawable.setBounds(0, 0, (int) (size), (int) (size));
							return emojiDrawables.get(paramAnonymousString).get();
						} else {
							RegionDrawable drawable = new RegionDrawable(emojiImages,
									(Rect) emojiRects.get(paramAnonymousString));
							if (useDensity)
								drawable.setBounds(0, 0, (int) (size * density), (int) (size * density));
							else
								drawable.setBounds(0, 0, (int) (size), (int) (size));
							emojiDrawables.put(paramAnonymousString, new SoftReference<Drawable>(drawable));
							return drawable;
						}
					}
				} catch (Exception e) {
				}
				return null;
			}

		}, null);

	}

	public Drawable getDrawable(String paramAnonymousString, boolean useDensity, int size) {
		try {
			if (emojiRects.containsKey(paramAnonymousString)) {
				if (emojiDrawables.containsKey(paramAnonymousString)
						&& emojiDrawables.get(paramAnonymousString).get() != null) {
					RegionDrawable drawable = (RegionDrawable) emojiDrawables.get(paramAnonymousString).get();
					if (drawable.source == null || drawable.source.isRecycled()) {
						drawable.source = emojiImages;
					}
					if (useDensity)
						drawable.setBounds(0, 0, (int) (size * density), (int) (size * density));
					else
						drawable.setBounds(0, 0, (int) (size), (int) (size));
					return emojiDrawables.get(paramAnonymousString).get();
				} else {
					RegionDrawable drawable = new RegionDrawable(emojiImages,
							(Rect) emojiRects.get(paramAnonymousString));
					if (useDensity)
						drawable.setBounds(0, 0, (int) (size * density), (int) (size * density));
					else
						drawable.setBounds(0, 0, (int) (size), (int) (size));
					emojiDrawables.put(paramAnonymousString, new SoftReference<Drawable>(drawable));
					return drawable;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private class RegionDrawable extends Drawable {
		private Paint bitmapPaint = new Paint();
		public Bitmap source;
		private Rect sourceRect;

		public RegionDrawable(Bitmap paramRect, Rect outRect) {
			this.source = paramRect;
			this.sourceRect = outRect;
			bitmapPaint = new Paint();
		}

		public void draw(Canvas paramCanvas) {
			try {
				paramCanvas.drawBitmap(this.source, this.sourceRect, getBounds(),
						this.bitmapPaint == null ? new Paint() : this.bitmapPaint);
			} catch (Exception e) {

			}
		}

		public int getOpacity() {
			return 0;
		}

		public void setAlpha(int paramInt) {
		}

		public void setColorFilter(ColorFilter paramColorFilter) {
		}
	}
}