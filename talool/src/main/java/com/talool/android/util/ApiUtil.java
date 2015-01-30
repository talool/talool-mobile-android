package com.talool.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;

import com.talool.android.R;
import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.Category_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;

/**
 * Convenience methods for the API
 * 
 * @author clintz
 * 
 */
public final class ApiUtil
{

	public static boolean isClickableActivityLink(final Activity_t activity)
	{
		final ActivityEvent_t event = activity.getActivityEvent();

		if (activity.activityLink == null)
		{
			return false;
		}

		switch (event)
		{
			case REDEEM:
			case FACEBOOK_SEND_GIFT:
			case FRIEND_GIFT_REDEEM:
			case FRIEND_GIFT_REJECT:
			case EMAIL_SEND_GIFT:
			case UNKNOWN:
			case PURCHASE:
				return false;

			case TALOOL_REACH:
			case MERCHANT_REACH:
			case WELCOME:
            case FUNDRAISER_SUPPORT:
				return true;

			default:
				return !activity.actionTaken;
		}

	}

	public static int getIcon(final Activity_t activity)
	{
		switch (activity.activityEvent)
		{
			case EMAIL_RECV_GIFT:
			case EMAIL_SEND_GIFT:
			case FRIEND_GIFT_ACCEPT:
			case FRIEND_GIFT_REJECT:
			case FACEBOOK_RECV_GIFT:
			case FACEBOOK_SEND_GIFT:
			case REJECT_GIFT:
				return R.string.icon_gift;

			case FRIEND_PURCHASE_DEAL_OFFER:
			case PURCHASE:
			case REDEEM:
				return R.string.icon_money;

			case WELCOME:
			case MERCHANT_REACH:
			case TALOOL_REACH:
				return R.string.icon_envelope_alt;

			case UNKNOWN:
			default:
				return R.string.icon_envelope_alt;

		}

	}

	public static int getIcon(final Category_t category)
	{
		switch (category.categoryId)
		{
			case Constants.FOOD_CATEGORY_ID:
				return R.string.icon_food;

			case Constants.SHOPPING_CATEGORY_ID:
				return R.string.icon_shopping_cart;

			case Constants.FUN_CATEGORY_ID:
				return R.string.icon_ticket;

            case Constants.NIGHTLIFE_CATEGORY_ID:
                return R.string.icon_glass;

            case Constants.SERVICES_CATEGORY_ID:
                return R.string.icon_cogs;

			default:
				return R.string.icon_food;

		}

	}

	public static int getIconColor(final Category_t category)
	{
		switch (category.categoryId)
		{
			case Constants.FOOD_CATEGORY_ID:
				return R.color.food_icon_color;

			case Constants.SHOPPING_CATEGORY_ID:
				return R.color.shopping_icon_color;

			case Constants.FUN_CATEGORY_ID:
				return R.color.fun_icon_color;

            case Constants.NIGHTLIFE_CATEGORY_ID:
                return R.color.nightlife_icon_color;

            case Constants.SERVICES_CATEGORY_ID:
                return R.color.services_icon_color;

			default:
				return R.color.gray_icon;

		}

	}

    public static Drawable getFontAwesomeDrawable(final Context context, final int icon,
                                                  final int color, final int size)
    {
        TextDrawable d = new TextDrawable(context);

        d.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        d.setTypeface(TypefaceFactory.get().getFontAwesome());
        d.setText(context.getResources().getString(icon));
        d.setTextColor(context.getResources().getColor(color));
        d.setTextSize(size);

        return d;
    }

    public static MerchantLocation_t getClosestLocation(Merchant_t merchant)
    {
        MerchantLocation_t closestLocation = null;
        for (MerchantLocation_t l : merchant.getLocations())
        {
            if (closestLocation==null || l.distanceInMeters<closestLocation.distanceInMeters)
            {
                closestLocation = l;
            }
        }
        return closestLocation;
    }

}
