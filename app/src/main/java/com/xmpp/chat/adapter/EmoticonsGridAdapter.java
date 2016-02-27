package com.xmpp.chat.adapter;

import java.io.InputStream;
import java.util.ArrayList;

import com.example.xmppsample.R;
import com.xmpp.chat.dao.EmojiItem;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class EmoticonsGridAdapter extends BaseAdapter {

	private ArrayList<EmojiItem> emojis;
	private int pageNumber;
	Context mContext;

	KeyClickListener mListener;

	public EmoticonsGridAdapter(Context context, ArrayList<EmojiItem> emojis, int pageNumber,
			KeyClickListener listener) {
		this.mContext = context;
		this.emojis = emojis;
		this.pageNumber = pageNumber;
		this.mListener = listener;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item_emoticon, null);
			ImageView image = (ImageView) v.findViewById(R.id.item);
			image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.keyClickedIndex((EmojiItem) v.getTag());
				}
			});
		}

		// final String path = paths.get(position);

		ImageView image = (ImageView) v.findViewById(R.id.item);
		// image.setImageBitmap(getImage(path));
		image.setImageDrawable(emojis.get(position).emojiDrawable);
		image.setTag(emojis.get(position));

		return v;
	}

	@Override
	public int getCount() {
		return emojis.size();
	}

	@Override
	public Drawable getItem(int position) {
		return emojis.get(position).emojiDrawable;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	private Bitmap getImage(String path) {
		AssetManager mngr = mContext.getAssets();
		InputStream in = null;

		try {
			in = mngr.open("emoticons/" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Bitmap temp = BitmapFactory.decodeStream(in, null, null);
		return temp;
	}

	public interface KeyClickListener {

		public void keyClickedIndex(EmojiItem index);
	}
}