package com.talool.mobile.android.cache;

import java.util.ArrayList;
import java.util.List;

import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Deal_t;

public class BookCache {
	private DealOffer_t closestBook = null;
	private List<Deal_t> dealsInBook = new ArrayList<Deal_t>();
	private static BookCache instance = new BookCache();

	public static BookCache get()
	{
		return instance;
	}

	public DealOffer_t getClosestBook()
	{
		return closestBook;
	}

	public void setClosestBook(final DealOffer_t closestBook)
	{
		this.closestBook = closestBook;
	}
	
	public List<Deal_t> getDealsInBook() {
		return dealsInBook;
	}

	public void setDealsInBook(List<Deal_t> dealsInBook) {
		this.dealsInBook = dealsInBook;
	}
}
