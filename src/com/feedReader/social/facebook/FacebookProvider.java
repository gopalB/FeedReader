package com.feedReader.social.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.feedReader.util.Constants;
import com.feedReader.util.ToastUtilities;
/**
 * 
 * @author Gopal Biyani
 *
 */
public class FacebookProvider {
	private final Activity mActivity;
	private final Facebook mFacebook;
	private static final String APP_ID = "220639124615871";
	public static final String FACEBOOK_LOG = "facebook";
	private Bundle mPostBundle;
	
	public FacebookProvider(Activity activity){
		mActivity = activity;
		mFacebook = new Facebook(APP_ID);
		FacebookSessionStore.restore(mFacebook, activity);
	}
	
	public void post(Bundle messageBundle){
		mPostBundle = messageBundle;
		if(mFacebook.isSessionValid()){
			Log.d(Constants.FEED_READER_LOG, "In FacebookProvider.post Session is valid");
			mFacebook.dialog(mActivity, "stream.publish",messageBundle, new 
                  WallPostDialogListener());
		} else {
			 Log.d(Constants.FEED_READER_LOG, "In FacebookProvider Session not valid");
			 mFacebook.authorize(mActivity, new LoginDialogListener());
		}
	}
	
	public boolean isSessionValid(){
		return mFacebook.isSessionValid();
	}
	
	public void onActivityResult(int resultCode,int requestCode,Intent data){
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	private final class LoginDialogListener implements DialogListener {
		private final AlertDialog mAlertDialog;
		public LoginDialogListener(){
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setCancelable(false)
			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.dismiss();
			           }
		       });
			
			mAlertDialog = builder.create();
			
		}
		
		public void onComplete(Bundle values) {
			//Logged in Successfully
			Log.d(FACEBOOK_LOG, "Authorized Successfully");
			FacebookSessionStore.save(mFacebook, mActivity);
			mFacebook.dialog(mActivity, "stream.publish",mPostBundle, new 
	                  WallPostDialogListener());
		}

		public void onFacebookError(FacebookError error) {
			//Error In Login Authorization
			Log.e(FACEBOOK_LOG, error.getMessage());
			mAlertDialog.setMessage("Error : " + error.getMessage());
			mAlertDialog.show();
		}

		public void onError(DialogError error) {
			//Error While Logging
			Log.e(FACEBOOK_LOG, error.getMessage());
			mAlertDialog.setMessage("Error : " + error.getMessage());
			mAlertDialog.show();
		}

		public void onCancel() {
			Log.d(FACEBOOK_LOG,"Login Cancelled");
			mAlertDialog.setMessage("Please login Facebook to post feed.");
			mAlertDialog.show();
		}
	}
	
	  public class WallPostRequestListener extends BaseRequestListener {

	        public void onComplete(final String response, final Object state) {
//	            try {
	                // process the response here: executed in background thread
	                Log.d("Facebook-Example", "Response: " + response.toString());
	                ToastUtilities.showToast(mActivity, "Feed Posted Successfully", false);
//	                JSONObject json = Util.parseJson(response);
//	                final String name = json.getString("name");

	                // then post the processed result back to the UI thread
	                // if we do not do this, an runtime exception will be generated
	                // e.g. "CalledFromWrongThreadException: Only the original
	                // thread that created a view hierarchy can touch its views."
//	                Example.this.runOnUiThread(new Runnable() {
//	                    public void run() {
//	                        mText.setText("Hello there, " + name + "!");
//	                    }
//	                });
//	            } catch (JSONException e) {
//	                Log.w("Facebook-Example", "JSON Error in response");
//	            } catch (FacebookError e) {
//	                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
//	            }
	        }
	    }

	
	 public class WallPostDialogListener extends BaseDialogListener {
	        public void onComplete(Bundle values) {
	            final String postId = values.getString("post_id");
	            if (postId != null) {
	                Log.d(FACEBOOK_LOG, "Dialog Success! post_id=" + postId);
	                ToastUtilities.showToast(mActivity, "Feed Posted Successfully", false);
	            } else {
	                Log.d(FACEBOOK_LOG, "No wall post made");
	                ToastUtilities.showToast(mActivity, "No Feed Was Posted", false);
	            }
	        }
	    }
	
}
