package com.talool.mobile.android.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author clintz
 * 
 */
public final class ErrorMessageCache
{
	private static final Map<Integer, String> ERR_MAP = new HashMap<Integer, String>();
	private static final String UNMAPPED_CODE_MSG = "An unknown error has occured";

	public enum Message
	{
		ProvideValidEmailAddr("Please provide a valid email address"),
		PasswordResetGeneral("There was a problem generating your password reset request"),
		EmailMessageFailureTitle("Email Failure"),
		ResetPasswordFailureTitle("Reset Password Failure"),
		AlreadyAcceptedGift("You have already accepted this gift");

		private String text;

		Message(String text)
		{
			this.text = text;
		}

		public String getText()
		{
			return text;
		}

	}

	static
	{
		ERR_MAP.put(0, "An unknown error has occured");
		ERR_MAP.put(3000, "Activation code not found");
		ERR_MAP.put(3001, "That activation code has already been used.  Codes can only be used once.");
		ERR_MAP.put(100, "A valid email is required");
		ERR_MAP.put(101, "Password is required");
		ERR_MAP.put(102, "Confirm password must match password");
		ERR_MAP.put(103, "Password reset code is required");
		ERR_MAP.put(104, "Your password reset code has expired. Please request a new password reset.");
		ERR_MAP.put(105, "Password reset code is invalid");
		ERR_MAP.put(1000, "Email is already taken");
		ERR_MAP.put(1001, "Invalid username or password");
		ERR_MAP.put(1002, "Deal is not owned by you");
		ERR_MAP.put(1003, "The deal has already been redeemed");
		ERR_MAP.put(1004, "Gifting is not allowed");
		ERR_MAP.put(1005, "Account not found");
		ERR_MAP.put(1006, "Email is required");

	}

	public static String getMessage(final Integer errorCode)
	{
		if (errorCode == null)
		{
			return UNMAPPED_CODE_MSG;
		}
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
