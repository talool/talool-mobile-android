package com.talool.mobile.android.util;

/**
 * 
 * @author clintz
 * 
 */
public final class Constants
{
	// TODO: MAKE SURE THIS IS FALSE FOR PRODUCTION DEPLOYS
	public static boolean IS_DEVELOPMENT_MODE = false;

	public static final String FORMAT_GENERAL_DATE_TIME = "MM/dd/yyyy hh:mm:ss a";
	public static final String FORMAT_DECIMAL_MONEY = "$#,###.00";
	public static final String FORMAT_MONTH_DAY_YEAR = "MM/dd/yyyy";

	public static final int FOOD_CATEGORY_ID = 1;
	public static final int SHOPPING_CATEGORY_ID = 2;
	public static final int FUN_CATEGORY_ID = 3;

	public static final String DATABASE_NAME = "talool";
	public static final int DATABASE_VERSION = 1;

	public static final String DEAL_OFFER_ID_PAYBACK_VANCOUVER = "a067de54-d63d-4613-8d60-9d995765cd52";
	public static final String DEAL_OFFER_ID_PAYBACK_BOULDER = "4d54d8ef-febb-4719-b9f0-a73578a41803";

	public static final int BRAINTREE_REQUEST_PAYMENT = 1;
	public static final String VENMO_SDK_SESSION = "venmo_sdk_session";

	// ENVIRONMENT SPECIFIC VALUES
	private static final String API_URL_DEV = "http://dev-api.talool.com/1.1";
	private static final String API_URL_PROD = "http://api.talool.com/1.1";

	private static final String OG_GIFT_PAGE_DEV = "http://dev-www.talool.com/gift";
	private static final String OG_DEAL_PAGE_DEV = "http://dev-www.talool.com/deal";
	private static final String OG_OFFER_PAGE_DEV = "http://dev-www.talool.com/offer";
	private static final String MERCHANT_PAGE_DEV = "http://dev-www.talool.com/location";

	private static final String OG_GIFT_PAGE_PROD = "http://www.talool.com/gift";
	private static final String OG_DEAL_PAGE_PROD = "http://www.talool.com/deal";
	private static final String OG_OFFER_PAGE_PROD = "http://www.talool.com/offer";
	private static final String MERCHANT_PAGE_PROD = "http://www.talool.com/location";

	private static final String BRAINTREE_MERCHANT_ID_DEV = "mkf3rwysqz6w9x44";
	private static final String BRAINTREE_MERCHANT_KEY_DEV = "MIIBCgKCAQEA2CWCSS/z/FrWMJqPb8ysca5+N7edz3Kiz9EpNwZFQ4Rx9lS02mXXLG0jHWFC41y8IFKDjzKk01OGB6Li0VL/RcB88ASdJALBpiuyTkIiiFSTFLzcGehagmfuozv7TQOnd8biYOOKvJ692laOdr7rdqLi3zFvncgg49JTnKewXZF8RRLHObpFHSj7r7O7o4Boy6aVaD06wuytf9mKxUYqp2juqVT4UgG4uhuc4EcgRYHfW5GZ0OtotKev1SsrzEC4s5N1QSBkkEeyagzGxdrp5apJkdIQLjIcx++N76SMR9Ybce2ApiScK14st96bZ760QBPMSXrMAVfYvYAEkR1B5QIDAQAB";
	private static final String BRAINTREE_MERCHANT_ID_PROD = "pyrxcmxsgyty4f3x";
	private static final String BRAINTREE_MERCHANT_KEY_PROD = "MIIBCgKCAQEA1zcxXmCZcOcCCedQQ5sqtIZTYhvwKTmnc7n4tvKAyGxMsyBdqJhLdtoLkrata/PwGtVX4YtAdaBJe7zpQ/b8YiI4I+ibWNXZ1x/p51BTuirSKO9FwjyeNjY1rYqFBQNLO0dZrPJn3sJM1SJABji8fA1NJB2ZVLglqznd56exKjBhzZaSLHWuK5kMEWlxPmiQfF9WzLGg3t+eXtmqmuVQBkpzN7SXsmxlp5xQHjvkSPiLC7cUMbHlIRxx0YoZ08HmXY9IwPfxTW4LtLJEUDVVkH2SuRfyGEOHSKpceshELwMbBuwvTWVEeXSGOmQgVEJWAgruwXmr+QpzAhdN2fx4XwIDAQAB";

	public static String getApiUrl()
	{
		return (IS_DEVELOPMENT_MODE) ? API_URL_DEV : API_URL_PROD;
	}

	public static String getOGGiftPage()
	{
		return (IS_DEVELOPMENT_MODE) ? OG_GIFT_PAGE_DEV : OG_GIFT_PAGE_PROD;
	}

	public static String getOGDealPage()
	{
		return (IS_DEVELOPMENT_MODE) ? OG_DEAL_PAGE_DEV : OG_DEAL_PAGE_PROD;
	}

	public static String getOGOfferPage()
	{
		return (IS_DEVELOPMENT_MODE) ? OG_OFFER_PAGE_DEV : OG_OFFER_PAGE_PROD;
	}

	public static String getOGMerchantPage()
	{
		return (IS_DEVELOPMENT_MODE) ? MERCHANT_PAGE_DEV : MERCHANT_PAGE_PROD;
	}

	public static String getBTMerchantId()
	{
		return (IS_DEVELOPMENT_MODE) ? BRAINTREE_MERCHANT_ID_DEV : BRAINTREE_MERCHANT_ID_PROD;
	}

	public static String getBTMerchantKey()
	{
		return (IS_DEVELOPMENT_MODE) ? BRAINTREE_MERCHANT_KEY_DEV : BRAINTREE_MERCHANT_KEY_PROD;
	}

}
