package com.feedReader.detail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * @author Gopal Biyani
 *
 */
public class FeedBean{
	private static final SimpleDateFormat mFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	private String mTitle;
	private String mLink;
	private long mPubDate;
	private String mDescription;//not capturing may be used in future release
	private String mGUID;
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;   
	}
	public String getTitle() {
		return mTitle;
	}
	public void setLink(String mLink) {
		this.mLink = mLink;
	}
	public String getLink() {
		return mLink;
	}
	public void setPubDate(String mPubDate) {
		try {
			this.mPubDate = mFormat.parse(mPubDate).getTime();
		} catch (ParseException e) {
			this.mPubDate = (new Date()).getTime();
		}
	}
	
	public long getPubDate() {
		return mPubDate;
	}
	
	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	
	public String getDescription() {
		return mDescription;
	}

	/**
	 * @param mGUID the mGUID to set
	 */
	public void setGUID(String mGUID) {
		this.mGUID = mGUID;
	}
	/**
	 * @return the mGUID
	 */
	public String getGUID() { 
		return mGUID;
	}

	public String toString(){
		return mTitle + " " + mLink + " " + mPubDate + " " + mDescription;
	}
}	
