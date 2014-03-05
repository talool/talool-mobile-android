package com.talool.android.cache;

import com.talool.api.thrift.DealOffer_t;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Deal Offer Cache
 * 
 * @author clintz
 * 
 */
public final class DealOfferCache
{
	private Map<String, DealOffer_t> dealOffers = new HashMap<String, DealOffer_t>();

	private static DealOfferCache instance = new DealOfferCache();

	public static DealOfferCache get()
	{
		return instance;
	}

	public DealOffer_t getDealOffer(final String dealOfferId)
	{
		return dealOffers.get(dealOfferId);
	}

	public void setDealOffer(final DealOffer_t dealOffer)
	{
		dealOffers.put(dealOffer.getDealOfferId(), dealOffer);
	}
}
