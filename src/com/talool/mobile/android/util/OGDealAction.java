package com.talool.mobile.android.util;

import com.facebook.model.OpenGraphAction;

public interface OGDealAction extends OpenGraphAction {
	// The deal object
	public OGDealObject getDeal();
	public void setDeal(OGDealObject deal);
}
