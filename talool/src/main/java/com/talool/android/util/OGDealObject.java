package com.talool.android.util;

import com.facebook.model.GraphObject;

public interface OGDealObject extends GraphObject {
    // A URL
    public String getUrl();
    public void setUrl(String url);

    // An ID
    public String getId();
    public void setId(String id);
}