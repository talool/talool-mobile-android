package com.talool.android.adapters;

import com.talool.api.thrift.Merchant_t;

import android.widget.ImageView;
import android.widget.TextView;

public class MyDealsRow {
	public Merchant_t merchant;
    public TextView myDealsMerchantIcon;
    public TextView myDealsMerchantTitle;
    public TextView myDealsMerchantLocation;
    public TextView myDealsMerchantDistance;
    public ImageView myDealsMerchantArrow;
}
