package com.talool.mobile.android.util;

/**
 * 
 * @author clintz
 * 
 */
public final class Constants
{
	// ****** REMEMBER TO CHANGE DEAL OFFERS WHEN PUSHING TO PROD ALSO ******* //
	// public static final String API_URL = "http://10.0.1.3:8082/1.1";
	public static final String API_URL = "http://dev-api.talool.com/1.1";
	//public static final String API_URL = "http://api.talool.com/1.1";

	public static final String OG_GIFT_PAGE = "http://dev-www.talool.com/gift";
	public static final String OG_DEAL_PAGE = "http://dev-www.talool.com/deal";
	public static final String OG_OFFER_PAGE = "http://dev-www.talool.com/offer";
	public static final String MERCHANT_PAGE = "http://dev-www.talool.com/location";
	
	public static final String FORMAT_GENERAL_DATE_TIME = "MM/dd/yyyy hh:mm:ss a";
	public static final String FORMAT_DECIMAL_MONEY = "$#,###.00";
	public static final String FORMAT_MONTH_DAY_YEAR = "MM/dd/yyyy";

	public static final int FOOD_CATEGORY_ID = 1;
	public static final int SHOPPING_CATEGORY_ID = 2;
	public static final int FUN_CATEGORY_ID = 3;

	public static final String DATABASE_NAME = "talool";
	public static final int DATABASE_VERSION = 1;

	public static final String BRAINTREE_MERCHANT_ID = "mkf3rwysqz6w9x44";
	public static final String BRAINTREE_MERCHANT_KEY = "MIIBCgKCAQEA2CWCSS/z/FrWMJqPb8ysca5+N7edz3Kiz9EpNwZFQ4Rx9lS02mXXLG0jHWFC41y8IFKDjzKk01OGB6Li0VL/RcB88ASdJALBpiuyTkIiiFSTFLzcGehagmfuozv7TQOnd8biYOOKvJ692laOdr7rdqLi3zFvncgg49JTnKewXZF8RRLHObpFHSj7r7O7o4Boy6aVaD06wuytf9mKxUYqp2juqVT4UgG4uhuc4EcgRYHfW5GZ0OtotKev1SsrzEC4s5N1QSBkkEeyagzGxdrp5apJkdIQLjIcx++N76SMR9Ybce2ApiScK14st96bZ760QBPMSXrMAVfYvYAEkR1B5QIDAQAB";

	public static final boolean BRAINTREE_USE_SANDBOX = true;
	public static final int BRAINTREE_REQUEST_PAYMENT = 1;
	public static final String VENMO_SDK_SESSION = "venmo_sdk_session";

	public static final String DEAL_OFFER_ID_PAYBACK_BOULDER = "e11dcc50-3ee1-477d-9e2d-ab99f0c28675";

	
	
	// private static final String DEAL_OFFER_ID_PAYBACK_VANCOUVER =
	// "a067de54-d63d-4613-8d60-9d995765cd52";

	// private static final String DEAL_OFFER_ID_PAYBACK_BOULDER =
	// "4d54d8ef-febb-4719-b9f0-a73578a41803";

	public static final String DEAL_OFFER_ID_PAYBACK_VANCOUVER = "231d6a36-1a40-44c6-ba25-402f42d05f6d";

}
