package com.talool.mobile.android.util;

import java.util.ArrayList;
import java.util.List;

import com.facebook.model.GraphUser;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.SocialAccount_t;
import com.talool.api.thrift.SocialNetwork_t;

/**
 * Created with IntelliJ IDEA.
 * User: bryan
 * Date: 8/14/13
 * Time: 11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class FacebookHelper {

	private static final FacebookHelper instance = new FacebookHelper();
    private List<GraphUser> selectedFriends;

	private FacebookHelper()
	{
		selectedFriends = new ArrayList<GraphUser>();
	}
	
	public static FacebookHelper get()
	{
		return instance;
	}
	
    public static Customer_t createCustomerFromFacebook(GraphUser user){

        // TODO verify email.  if null, prompt user to enter it prior to registration
        Customer_t customer = new Customer_t(user.getFirstName(), user.getLastName(), user.getProperty("email").toString());

        SocialAccount_t socialAccount = new SocialAccount_t(SocialNetwork_t.Facebook, user.getId());
        customer.putToSocialAccounts(SocialNetwork_t.Facebook, socialAccount);

        return customer;
    }

	public List<GraphUser> getSelectedFriends() {
		return selectedFriends;
	}

	public void setSelectedFriends(List<GraphUser> selectedFriends) {
		this.selectedFriends = selectedFriends;
	}
    
	public static String dealObjectForGift(String giftId)
	{
	    String url = Constants.getOGGiftPage()+ "/"+giftId;

	    return url;
	}
}
