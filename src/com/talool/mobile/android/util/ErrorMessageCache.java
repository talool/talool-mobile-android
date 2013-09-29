package com.talool.mobile.android.util;

import java.util.HashMap;
import java.util.Map;

import com.talool.api.thrift.ErrorCode_t;

/**
 * 
 * @author clintz
 * 
 */
public final class ErrorMessageCache
{
	private static final Map<ErrorCode_t, String> ERR_MAP = new HashMap<ErrorCode_t, String>();
	private static final String UNMAPPED_CODE_MSG = "An unknown error has occured";

	static
	{
		ERR_MAP.put(ErrorCode_t.ACTIVIATION_CODE_ALREADY_ACTIVATED, "Activation code is already activated");
		ERR_MAP.put(ErrorCode_t.UNKNOWN, "An unknown error has occured");
		ERR_MAP.put(ErrorCode_t.VALID_EMAIL_REQUIRED, "A valid email is required");
		ERR_MAP.put(ErrorCode_t.PASS_REQUIRED, "Password is required");
		ERR_MAP.put(ErrorCode_t.PASS_CONFIRM_MUST_MATCH, "Confirm password must match password");
		ERR_MAP.put(ErrorCode_t.PASS_RESET_CODE_REQUIRED, "Password reset code is required");
		ERR_MAP.put(ErrorCode_t.PASS_RESET_CODE_EXPIRED, "Password reset code expired");
		ERR_MAP.put(ErrorCode_t.PASS_RESET_CODE_INVALID, "Password reset code is invalid");
		ERR_MAP.put(ErrorCode_t.EMAIL_ALREADY_TAKEN, "Email is already taken");
		ERR_MAP.put(ErrorCode_t.INVALID_USERNAME_OR_PASSWORD, "Invalid username or password");

		ERR_MAP.put(ErrorCode_t.CUSTOMER_DOES_NOT_OWN_DEAL, "Deal is not owned by you");
		ERR_MAP.put(ErrorCode_t.DEAL_ALREADY_REDEEMED, "The deal has already been redeemed");
		ERR_MAP.put(ErrorCode_t.GIFTING_NOT_ALLOWED, "Gifting is not allowed");
		ERR_MAP.put(ErrorCode_t.CUSTOMER_NOT_FOUND, "Account not found");
		ERR_MAP.put(ErrorCode_t.EMAIL_REQUIRED, "Email is required");
		ERR_MAP.put(ErrorCode_t.ACTIVIATION_CODE_NOT_FOUND, "Activation code not found");

	}

	public static String getMessage(ErrorCode_t errorCode)
	{
		String msg = ERR_MAP.get(errorCode);
		return msg == null ? UNMAPPED_CODE_MSG : msg;
	}

	public static String getNetworkIssueMessage()
	{
		return "Service connection issue";
	}

	public static String getServiceErrorMessage()
	{
		return "There was a network connection issue";
	}

	/**
	 * This should never happen, but create a user friend message
	 * 
	 * @param identified
	 * @return
	 */
	public static String getNotFoundMessage(String identifier, String key)
	{
		if (key == null)
		{
			return "An general error has occured";
		}
		else
		{
			return key + " was not found";
		}

	}
}
