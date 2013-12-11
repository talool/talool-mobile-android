package com.talool.android.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

	// TODO - favorties will be refactored into SqlLite - for now lets just
	// compare
	private static final Comparator<Merchant_t> ascNameComparator = new Comparator<Merchant_t>()
	{

		@Override
		public int compare(final Merchant_t m1, final Merchant_t m2)
		{
			return m1.getName().compareTo(m2.getName());
		}

	};

	public static FavoriteMerchantCache get()
	{
		return instance;
	}

	public List<Merchant_t> getMerchants()
	{
		final List<Merchant_t> mercs = new ArrayList<Merchant_t>();
		mercs.addAll(merchants.values());

		Collections.sort(mercs, ascNameComparator);

		return mercs;

	}

	public boolean isFavorite(final String merchantId)
	{
		return merchants.containsKey(merchantId);
	}

	public void clear()
	{
		merchants.clear();
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
