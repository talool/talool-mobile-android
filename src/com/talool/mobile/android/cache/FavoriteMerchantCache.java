package com.talool.mobile.android.cache;

import java.util.HashMap;
import java.util.Map;

import com.talool.api.thrift.Merchant_t;

/**
 * Favorite Merchant cache
 * 
 * @author clintz
 * 
 */
public final class FavoriteMerchantCache
{
	private Map<String, Merchant_t> merchants = new HashMap<String, Merchant_t>();

	private static FavoriteMerchantCache instance = new FavoriteMerchantCache();

	public static FavoriteMerchantCache get()
	{
		return instance;
	}

	public boolean isFavorite(final String merchantId)
	{
		return merchants.containsKey(merchantId);
	}

	public Merchant_t getMerchant(final String merchantId)
	{
		return merchants.get(merchantId);
	}

	public void removeMerchant(final Merchant_t merchant)
	{
		merchants.remove(merchant.getMerchantId());
	}

	public void addMerchant(final Merchant_t merchant)
	{
		merchants.put(merchant.getMerchantId(), merchant);
	}
}
