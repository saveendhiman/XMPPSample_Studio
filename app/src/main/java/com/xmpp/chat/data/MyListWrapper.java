package com.xmpp.chat.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MyListWrapper<T> extends ArrayList<T> {

	public MyListWrapper()
	{
		super();
	}
	
	public MyListWrapper(List<T> list) {
		super();
		addAll(list);
	}
	
	@Override
	public boolean addAll(final Collection<? extends T> collection) {
		return super.addAll(collection);
	}
	@Override
	public boolean add(T object) {
		return super.add(object);
	}
}
