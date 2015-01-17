package com.cwits.cyx_drive_sdk.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CollectSearchDataBase extends SQLiteOpenHelper implements ICollectSearchDataBase{

	//数据库名
	private final static String DATABASE_NAME="cxy_collect_db";
	//版本号
	private final static int DATABASE_VERSION=1;
	//用户收藏，数据表名
	private final static String TABLE_NAME_COLLECT = "cxy_collect_tab";
	//主键ID
	private final static String FIELD_ID="_id"; 
	//用户ID
	private final static String FIELD_USER_ID = "cxy_collect_user_id";
	//用户收藏，收藏内容
	private final static String FILED_COLLECT_CONTENT = "cxy_collect_content";
	
	public CollectSearchDataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean collectSearchHistory(String content) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> queryCollectSeach() {
		// TODO Auto-generated method stub
		SQLiteDatabase db=this.getReadableDatabase();
		ArrayList<String> resultList = new ArrayList<String>();
		Cursor cursor=db.query(TABLE_NAME_COLLECT, null, null, null, null, null,  " _id desc");
		if(cursor!=null&&cursor.moveToNext()){
			resultList.add(cursor.getString(cursor.getColumnIndex(FILED_COLLECT_CONTENT)));
		}
		return resultList;
	}

	@Override
	public void delectCollect(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCollcet = "Create table "+TABLE_NAME_COLLECT+"("+FIELD_ID+" integer primary key autoincrement,"
				+FIELD_USER_ID+" text," +FILED_COLLECT_CONTENT+ "text);";
		db.execSQL(sqlCollcet);
		
		//测试，插入数据
		ContentValues vales = new ContentValues();
		vales.put(FIELD_USER_ID, "18126219331");
		vales.put(FILED_COLLECT_CONTENT, "西藏");
		db.insert(TABLE_NAME_COLLECT, null, vales);
		//测试end
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql=" DROP TABLE IF EXISTS "+TABLE_NAME_COLLECT;
		db.execSQL(sql);
		onCreate(db);
	}

}
