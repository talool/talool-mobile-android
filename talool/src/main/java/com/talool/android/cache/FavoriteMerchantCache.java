package com.talool.android.cache;

import com.talool.android.TaloolApplication;
import com.talool.android.persistence.FavoritesDao;
import com.talool.api.thrift.Merchant_t;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Favorite Merchant cache
 * 
 * @author clintz
 * 
 */
public final class FavoriteMerchantCache
{
	private Map<String, Merchant_t> merchants = new HashMap<String, Merchant_t>();
    private FavoritesDao dao = new FavoritesDao(TaloolApplication.getAppContext());

	private static FavoriteMerchantCache instance = new FavoriteMerchantCache();

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
       if(merchants.isEmpty())
       {
           loadFromDb();
       }

		final List<Merchant_t> mercs = new ArrayList<Merchant_t>();
		mercs.addAll(merchants.values());
		Collections.sort(mercs, ascNameComparator);
		return mercs;
	}

	public boolean isFavorite(final String merchantId)
	{
        if(merchants.isEmpty())
        {
            loadFromDb();
        }
		return merchants.containsKey(merchantId);
	}

    private void loadFromDb()
    {
        List<Merchant_t> daoMerchants = dao.getMerchants(null);
        for (Merchant_t merchant : daoMerchants)
        {
            merchants.put(merchant.getMerchantId(),merchant);
        }
    }

    /**
     * Clears all entries out of the FavoriteMerchantCache and the SQLLite database
     */
	public void clear()
	{
		merchants.clear();
        dao.deleteRows(null);
    }

	public Merchant_t getMerchant(final String merchantId)
	{
        if(merchants.isEmpty())
        {
            loadFromDb();
        }
		return merchants.get(merchantId);
	}

	public void removeMerchant(final Merchant_t merchant)
	{
		merchants.remove(merchant.getMerchantId());
        dao.deleteRows(merchant.getMerchantId());
	}

	public void addMerchant(final Merchant_t merchant)
	{
		merchants.put(merchant.getMerchantId(), merchant);
        dao.saveMerchant(merchant);
	}
}
