package com.xmpp.chat.adapter;

import java.util.ArrayList;

import com.example.xmppsample.R;
import com.xmpp.chat.adapter.EmoticonsGridAdapter.KeyClickListener;
import com.xmpp.chat.dao.EmojiItem;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.GridView;

public class EmoticonsPagerAdapter extends PagerAdapter {

	public ArrayList<EmojiItem> emojis;
	private static final int NO_OF_EMOTICONS_PER_PAGE = 15;
	Activity mActivity;
	KeyClickListener mListener;

	public EmoticonsPagerAdapter(Activity activity, ArrayList<EmojiItem> emojis, KeyClickListener listener) {
		this.emojis = emojis;
		this.mActivity = activity;
		this.mListener = listener;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

	}

	@Override
	public int getCount() {
		return (int) Math.ceil((double) emojis.size() / (double) NO_OF_EMOTICONS_PER_PAGE);
	}

	@Override
	public Object instantiateItem(View collection, int position) {

		View layout = mActivity.getLayoutInflater().inflate(R.layout.item_emotions_grid, null);

		int initialPosition = position * NO_OF_EMOTICONS_PER_PAGE;
		ArrayList<EmojiItem> emojisInAPage = new ArrayList<EmojiItem>();

		for (int i = initialPosition; i < initialPosition + NO_OF_EMOTICONS_PER_PAGE && i < emojis.size(); i++) {
			emojisInAPage.add(emojis.get(i));
		}

		GridView grid = (GridView) layout.findViewById(R.id.emoticons_grid);
		EmoticonsGridAdapter adapter = new EmoticonsGridAdapter(mActivity.getApplicationContext(), emojisInAPage,
				position, mListener);
		grid.setAdapter(adapter);

		((ViewPager) collection).addView(layout);

		return layout;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}