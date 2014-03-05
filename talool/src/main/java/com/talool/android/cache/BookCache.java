package com.talool.android.cache;

import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Deal_t;

import java.util.ArrayList;
import java.util.List;

public class BookCache {
	private DealOffer_t closestBook = null;
	private List<Deal_t> dealsInBook = new ArrayList<Deal_t>();
	private static BookCache instance = new BookCache();
	private int numberOfMerchants = 0;
	private int totalHeight = 0;

	public int getTotalHeight() {
		return totalHeight;
	}

	public void setTotalHeight(int totalHeight) {
		this.totalHeight = totalHeight;
	}

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

	public int getNumberOfMerchants() {
		return numberOfMerchants;
	}

	public void setNumberOfMerchants(int numberOfMerchants) {
		this.numberOfMerchants = numberOfMerchants;
	}
}
