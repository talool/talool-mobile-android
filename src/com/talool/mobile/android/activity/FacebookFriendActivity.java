package com.talool.mobile.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ListView;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.FacebookHelper;

public class FacebookFriendActivity extends FragmentActivity {
	private ListView facebookFriendsListView;
	public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");
	private FriendPickerFragment friendPickerFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.facebook_friends_layout);

	    Bundle args = getIntent().getExtras();
	    FragmentManager manager = getSupportFragmentManager();
	    Fragment fragmentToShow = null;
	    Uri intentUri = getIntent().getData();
	    
	    if (FRIEND_PICKER.equals(intentUri)) {
	        if (savedInstanceState == null) {
	            friendPickerFragment = new FriendPickerFragment(args);
	            friendPickerFragment.setMultiSelect(false);
	        } else {
	            friendPickerFragment = 
	                (FriendPickerFragment) manager.findFragmentById(R.id.picker_fragment);
	        }
	        // Set the listener to handle errors
	        friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
				@Override
				public void onError(PickerFragment<?> fragment,
						FacebookException error) {
	            	FacebookFriendActivity.this.onError(error);
					
				}
	        });
	        // Set the listener to handle button clicks
	        friendPickerFragment.setOnDoneButtonClickedListener(
	                new PickerFragment.OnDoneButtonClickedListener() {
	            @Override
	            public void onDoneButtonClicked(PickerFragment<?> fragment) {
	                finishActivity();
	            }
	        });
	        fragmentToShow = friendPickerFragment;

	    } else {
	        // Nothing to do, finish
	        setResult(RESULT_CANCELED);
	        finish();
	        return;
	    }

	    manager.beginTransaction()
	           .replace(R.id.picker_fragment, fragmentToShow)
	           .commit();
	}

	private void onError(Exception error) {
	    onError(error.getLocalizedMessage(), false);
	}

	private void onError(String error, final boolean finishActivity) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Facebook Error").
	            setMessage(error).
	            setPositiveButton("Okay", 
	               new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(DialogInterface dialogInterface, int i) {
	                    if (finishActivity) {
	                        finishActivity();
	                    }
	                }
	            });
	    builder.show();
	}

	private void finishActivity() {
		if (FRIEND_PICKER.equals(getIntent().getData())) {
		    if (friendPickerFragment != null) {
		        FacebookHelper.get().setSelectedFriends(friendPickerFragment.getSelection());
		    }   
		}  
	    setResult(RESULT_OK, null);
	    finish();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    if (FRIEND_PICKER.equals(getIntent().getData())) {
	        try {
	            friendPickerFragment.loadData(false);
	        } catch (Exception ex) {
	            onError(ex);
	        }
	    }
	}
}
