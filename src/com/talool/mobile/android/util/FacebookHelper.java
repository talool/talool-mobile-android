package com.talool.mobile.android.util;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
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

    public static Customer_t createCostomerFromFacebook(GraphUser user){

        //verify email
        Customer_t customer = new Customer_t(user.getFirstName(), user.getLastName(), user.getProperty("email").toString());

        SocialAccount_t socialAccount = new SocialAccount_t(SocialNetwork_t.Facebook, user.getId());
        customer.putToSocialAccounts(SocialNetwork_t.Facebook, socialAccount);

        return customer;
    }


}
