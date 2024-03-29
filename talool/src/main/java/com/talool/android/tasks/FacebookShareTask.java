package com.talool.android.tasks;

import android.os.AsyncTask;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.talool.android.util.FacebookHelper;
import com.talool.android.util.OGDealAction;
import com.talool.android.util.OGDealObject;

import java.util.List;

public class FacebookShareTask extends AsyncTask<Void, Void, Response> {
	private String giftId;
	
	public FacebookShareTask(String giftId)
	{
		this.giftId = giftId;
	}

	@Override
	protected Response doInBackground(Void... params) {
		List<GraphUser> selectedUsers = FacebookHelper.get().getSelectedFriends();
		if(selectedUsers != null && selectedUsers.size()>0)
		{
			OGDealObject dealObject = GraphObject.Factory.create(OGDealObject.class);
			dealObject.setUrl(FacebookHelper.dealObjectForGift(giftId));
			OGDealAction dealAction = 
			        GraphObject.Factory.create(OGDealAction.class);
			dealAction.setTags(selectedUsers);
			dealAction.setDeal(dealObject);
			
			// Set up a request with the active session, set up
	        // an HTTP POST to the eat action endpoint
	        Request request = new Request(Session.getActiveSession(),
	        		"me/taloolclient:gift", null, HttpMethod.POST);
	        // Add the post parameter, the eat action
	        request.setGraphObject(dealAction);
	        // Execute the request synchronously in the background
	        // and return the response.
	        return request.executeAndWait();
		}
		else
		{
			//TODO cory - return an error
		}		
		return null;
		
	}

}
