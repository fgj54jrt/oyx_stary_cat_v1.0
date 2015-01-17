package com.cwits.cyx_drive_sdk.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchHistoryDataBase extends SQLiteOpenHelper implements ISeachHistoryDataBase{

	private final static String DATABASE_NAME="cxy_search_db";
	private final static int DATABASE_VERSION=1;
	private final static String TABLE_NAME="cxy_search_tab";
	public final static String FIELD_ID="_id"; 
	public final static String FIELD_TITLE="sec_Title";
	Context context;
	
	public SearchHistoryDataBase(Context context)
	{
		super(context, DATABASE_NAME,null, DATABASE_VERSION);
	}
	
	 
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql="Create table "+TABLE_NAME+"("+FIELD_ID+" integer primary key autoincrement,"
		+FIELD_TITLE+" text );";
		db.execSQL(sql);
				 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql=" DROP TABLE IF EXISTS "+TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}
	
	@Override
	public boolean saveSeachHistory(String content) {
		// TODO Auto-generated method stub
		SQLiteDatabase db=this.getWritableDatabase();
//		String deleteRepeat="delete from cxy_search_tab where sec_Title =?";
//		db.execSQL(deleteRepeat, new Object[] { content });
		ContentValues cv=new ContentValues(); 
		cv.put(FIELD_TITLE, content);
		db.insert(TABLE_NAME, null, cv);
		return true;
	}

	@Override
	public ArrayList<String> querySeachHistory() {
		// TODO Auto-generated method stub
		SQLiteDatabase db=this.getReadableDatabase();
		ArrayList<String> resultList = new ArrayList<String>();
		Cursor cursor=db.query(TABLE_NAME, null, null, null, null, null,  " _id desc");
		if(cursor!=null&&cursor.moveToNext()){
			resultList.add(cursor.getString(cursor.getColumnIndex(FIELD_TITLE)));
		}
		return resultList;
		
	}

	@Override
	public boolean deleteSeachHistory() {
		// TODO Auto-generated method stub
		SQLiteDatabase db= getWritableDatabase();		
		db.delete(TABLE_NAME, null, null);
		return true;
	}
		
	
	public void update(String id,String Title)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		String where=FIELD_ID+"=?";
		String[] whereValue={id};
		ContentValues cv=new ContentValues(); 
		cv.put(FIELD_TITLE, Title);
		db.update(TABLE_NAME, cv, where, whereValue);
	}
		
	
}