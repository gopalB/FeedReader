package com.feedReader.provider;

import com.feedReader.util.Constants;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * FeedDB will take care of Creating and Upgrading Database.
 * Will Instantiate DatabaseHelper object and will be shared
 * across the application. 
 * @author Gopal Biyani
 * 
 * Databse Version 3 : Created Index on feedDetail table on column _id and feed_id
 *
 */
public class FeedDB extends Application{

	private static final String DATABASE_NAME = "feedDB";
	private static final int DATABASE_VERSION = 3;
	

	private DatabaseHelper mFeedDBHelper;
	//create statement for feedList table
	private static final String CREATE_FEED_SOURCE_TABLE =
		"create table "+ Tables.FEED_SOURCE +" (_id integer primary key autoincrement, "
		+ "feed_name text not null, feed_url text not null,"
		+ "xml_encoding text not null, last_updated long);";
	//create statement for feedDetail table
	private static final String CREATE_FEED_DETAIL_TABLE = 	
		"create table "+ Tables.FEED_DETAIL +" (_id integer primary key autoincrement, " 
		+ "feed_id integer," //feed source id fk @Tables.FEED_SOURCE
		+ "title text not null,link text not null,pub_date long,"
		+ "isRead integer,guid text,starred int,description text);";
	//Insert Starred row into feedList table Default Key 1
	private static final String INSERT_STARRED = 
			"insert into feedList values ("+ FeedSourceProvider.STARRED_ID +",'Starred','','',0)"; //Starred Default Key 1
	//create index on feed_detail table
	private static final String CREATE_INDEX_FEED_DETAIL_ID = "create" +
			" INDEX feed_detail_id_idx ON " + Tables.FEED_DETAIL + "(_id)" ;
	private static final String CREATE_INDEX_FEED_DETAIL_FEED_ID = "create" +
			" INDEX feed_detail_feed_id_idx ON " + Tables.FEED_DETAIL + "(feed_id)" ;
	@Override
	public void onCreate() {
		super.onCreate();  
		mFeedDBHelper = new DatabaseHelper(FeedDB.this);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_FEED_SOURCE_TABLE);//create feedSource table
			db.execSQL(CREATE_FEED_DETAIL_TABLE);//create feedDetail table
			db.execSQL(INSERT_STARRED);//create default row for starred in feedSource table
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(CREATE_INDEX_FEED_DETAIL_ID);
			db.execSQL(CREATE_INDEX_FEED_DETAIL_FEED_ID);
		}
	}

	public SQLiteOpenHelper getDatabseHelper(){
		return mFeedDBHelper;
	}
	/**
	 * Closes Database should be called from
	 * onDestroy of main activity
	 */
	public void closeDatabaseHelper(){
		Log.d(Constants.FEED_READER_LOG, "closing database...");
		mFeedDBHelper.close();
	}

}
