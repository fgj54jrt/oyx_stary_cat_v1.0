package com.cwits.cyx_drive_sdk.db;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;
import com.xgr.wonderful.ui.MainActivity;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库公共类，提供基本数据库操作
 * 
 * @author raymon
 * 
 */
public class DBManager {
	private static final String TAG = "DBManager";
	// 默认数据库
	private static final String DB_NAME = "cxy_search_db.db";

	// 数据库版本
	private static final int DB_VERSION = 1;

	// 执行open()打开数据库时，保存返回的数据库对�?
	private SQLiteDatabase mSQLiteDatabase = null;

	// 由SQLiteOpenHelper继承过来
	private DatabaseHelper mDatabaseHelper = null;

	// 本地Context对象
	private Context mContext = null;
	
	private static DBManager dbConn= null;
	
	// 查询游标对象
	private Cursor cursor;
	public final static String TABLE_NAME="cxy_search_tab";
	public final static String FIELD_ID="_id"; 
	public final static String FIELD_TITLE="sec_Title";
	
	//数据库名
	private final static String DATABASE_NAME="cxy_collect_db";
	//版本号
	private final static int DATABASE_VERSION=1;
	//用户收藏，数据表名
	public final static String TABLE_NAME_COLLECT = "cxy_collect_tab";
	//用户ID
	public final static String FIELD_USER_ID = "cxy_collect_user_id";
	//用户收藏地名
	public final static String FIELD_PLACE_NAME = "cxy_place_name";
	//用户收藏地址
	public final static String FIELD_ADDRESS = "cxy_address";
	//用户收藏地址经度
	public final static String FIELD_ADDRESS_LONGITUDE = "cxy_address_langitude";
	//用户收藏地址纬度
	public final static String FIELD_ADDRESS_LATITUDE = "cxy_address_latitude";
	
	/**
	 * SQLiteOpenHelper内部�?
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			// 当调用getWritableDatabase()�?getReadableDatabase()方法�?创建�?��数据�?
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql="Create table "+TABLE_NAME+"("+FIELD_ID+" integer primary key autoincrement,"+FIELD_TITLE+" text );";
					
			db.execSQL(sql);
			
			String sqlCollcet = "Create table "+TABLE_NAME_COLLECT+"("+FIELD_ID+" integer primary key autoincrement,"
			                                                          +FIELD_USER_ID+" text,"
			                                                          +FIELD_ADDRESS_LONGITUDE+" text,"
			                                                          +FIELD_ADDRESS_LATITUDE+" text,"
			                                                          +FIELD_PLACE_NAME+" text,"
					                                                  +FIELD_ADDRESS+ " text);";					
			db.execSQL(sqlCollcet);
			
		
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS cxy_search_tab");
			onCreate(db);
		}
	}

	/**
	 * 构造函数
	 * 
	 * @param mContext
	 */
	private DBManager(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	public static DBManager getInstance(Context mContext){
		if (null == dbConn) {
			dbConn = new DBManager(mContext);
		}
		return dbConn;
	}

	/**
	 * 打开数据库
	 */
	public void open() {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		if (null != mDatabaseHelper) {
			mDatabaseHelper.close();
		}
		if (null != cursor) {
			cursor.close();
		}
	}

	/**
	 * 插入数据
	 * @param tableName 表名
	 * @param nullColumn null
	 * @param contentValues 名�?�?
	 * @return 新插入数据的ID，错误返�?1
	 * @throws Exception
	 */
	public long insert(String tableName, String nullColumn,
			ContentValues contentValues) throws Exception {
		try {
			return mSQLiteDatabase.insert(tableName, nullColumn, contentValues);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 通过主键ID删除数据
	 * @param tableName 表名
	 * @param key 主键�?
	 * @param id 主键�?
	 * @return 受影响的记录�?
	 * @throws Exception
	 */
	public long delete(String tableName, String key, int id) throws Exception {
		try {
			return mSQLiteDatabase.delete(tableName, key + " = " + id, null);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public boolean delete(String tableName, String content) {
		cursor = mSQLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		String record = "";
		int id = -1;
		while(cursor!=null&&cursor.moveToNext()) {
			record = cursor.getString(cursor.getColumnIndex(FIELD_TITLE));
			if(record.equalsIgnoreCase(content)) {
				id = cursor.getInt(cursor.getColumnIndex(FIELD_ID));
				break;
			}
		}
		if(cursor!=null&&!cursor.isClosed())
			cursor.close();
		if(id != -1) {
			try {
				long result = delete(tableName, FIELD_ID, id);
				if(result != 0)
					return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}
	
	public boolean deleteSeachHistory() {
		// TODO Auto-generated method stub
		SQLiteDatabase db= mDatabaseHelper.getWritableDatabase();		
		db.delete(TABLE_NAME, null, null);
		return true;
	}
	public boolean saveSeachHistory(String content) {
		// TODO Auto-generated method stub
		ContentValues cValue = new ContentValues();
		cValue.put(FIELD_TITLE, content);
		long result;
		try {
			result = insert(TABLE_NAME, null, cValue);
			if(result==-1){
				return false;
			}else {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	/**
	 * 查找表的�?��数据
	 * @param tableName 表名
	 * @param columns 如果返回�?��列，则填null
	 * @return
	 * @throws Exception
	 */
	public Cursor findAll(String tableName, String [] columns) throws Exception{
		try {
			cursor = mSQLiteDatabase.query(tableName, columns, null, null, null, null, " _id desc");
			cursor.moveToFirst();
			return cursor;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public ArrayList<String> getAllSearchHistory() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			cursor = mSQLiteDatabase.query(TABLE_NAME, null, null, null, null, null, "_id desc");
//			cursor.moveToFirst();
			while(cursor!=null&&cursor.moveToNext()) {
				String record = cursor.getString(cursor.getColumnIndex(FIELD_TITLE));
				list.add(record);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("DBManager", "查询数据库数据失败");
		}finally{
			if(cursor!=null&&!cursor.isClosed()){
				cursor.close();
			}
		}
		return null;
	}
	
	/**
	 * 根据主键查找数据
	 * @param tableName 表名
	 * @param key 主键�?
	 * @param id  主键�?
	 * @param columns 如果返回�?��列，则填null
	 * @return Cursor游标
	 * @throws Exception 
	 */
	public Cursor findById(String tableName, String key, int id, String [] columns) throws Exception {
		try {
			return mSQLiteDatabase.query(tableName, columns, key + " = " + id, null, null, null, null);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 根据条件查询数据
	 * @param tableName 表名
	 * @param names 查询条件
	 * @param values 查询条件�?
	 * @param columns 如果返回�?��列，则填null
	 * @param orderColumn 排序的列
	 * @param limit 限制返回�?
	 * @return Cursor游标
	 * @throws Exception
	 */
	public Cursor find(String tableName, String [] names, String [] values, String [] columns, String orderColumn, String limit) throws Exception{
		try {
			StringBuffer selection = new StringBuffer();
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ?");
				if (i != names.length - 1) {
					selection.append(",");
				}
			}
			cursor = mSQLiteDatabase.query(true, tableName, columns, selection.toString(), values, null, null, orderColumn, limit);
			cursor.moveToFirst();
			return cursor;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 
	 * @param tableName 表名
	 * @param names 查询条件
	 * @param values 查询条件�?
	 * @param args 更新�?值对
	 * @return true或false
	 * @throws Exception
	 */
	public boolean udpate(String tableName, String [] names, String [] values, ContentValues args) throws Exception{
		try {
			StringBuffer selection = new StringBuffer();
			for (int i = 0; i < names.length; i++) {
				selection.append(names[i]);
				selection.append(" = ?");
				if (i != names.length - 1) {
					selection.append(",");
				}
			}
			return mSQLiteDatabase.update(tableName, args, selection.toString(), values) > 0;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 执行sql语句，包括创建表、删除�?插入
	 * 
	 * @param sql
	 */
	public void executeSql(String sql) {
		mSQLiteDatabase.execSQL(sql);
	}
	
	
	public ArrayList<ContentValues>  getAllUserCollect() {
		
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		//当前用户登录
        BmobUser currentUser = BmobUser.getCurrentUser(mContext);
		String userID = currentUser.getUsername();
		//查询条件:根据用户ID查询
		//String[] columns={FIELD_USER_ID};
		String select =  FIELD_USER_ID +"=?";
		String selectArgs[] = {userID};
		Log.d(TAG, "用户ID："+userID);
		try {
			cursor = mSQLiteDatabase.query(TABLE_NAME_COLLECT, null, select, selectArgs, null, null, "_id desc");
			//cursor.moveToFirst();
			while(cursor!=null && cursor.moveToNext()) {
				
				int id = cursor.getInt(cursor.getColumnIndex(FIELD_ID));
				double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex(FIELD_ADDRESS_LONGITUDE)));
				double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(FIELD_ADDRESS_LATITUDE)));
				String placeName = cursor.getString(cursor.getColumnIndex(FIELD_PLACE_NAME));
				String address = cursor.getString(cursor.getColumnIndex(FIELD_ADDRESS));
				
				ContentValues values = new ContentValues();
				values.put(FIELD_ID, id);
				values.put(FIELD_USER_ID, userID);
				values.put(FIELD_ADDRESS_LONGITUDE, lon);
				values.put(FIELD_ADDRESS_LATITUDE, lat);
				values.put(FIELD_PLACE_NAME, placeName);
				values.put(FIELD_ADDRESS, address);
				list.add(values);
				Log.d(TAG, "查询数据库数据成功");
				Log.d(TAG, values.getAsString(FIELD_ID));
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "查询数据库数据失败");
		}finally{
			if(cursor!=null&&!cursor.isClosed()){
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 保存用户收藏地址
	 * @param 用户ID
	 * @param 经度
	 * @param 纬度
	 * @param 地名
	 * @param 地址
	 * 
	 * @return
	 * 		收藏成功，返回true； 收藏失败，返回false
	 */
	public boolean saveUserCollect(String userID, double longitude, double latitude,String placeName, String address) {
		// TODO Auto-generated method stub
		String lon = String.valueOf(longitude);
		String lat = String.valueOf(latitude);
		ContentValues cValue = new ContentValues();
		cValue.put(FIELD_USER_ID, userID);
		cValue.put(FIELD_ADDRESS_LONGITUDE, lon);
		cValue.put(FIELD_ADDRESS_LATITUDE, lat);
		cValue.put(FIELD_PLACE_NAME, placeName);
		cValue.put(FIELD_ADDRESS, address);
		long result;
		try {
			result = mSQLiteDatabase.insert(TABLE_NAME_COLLECT, null, cValue);
			
			if(result==-1){
				Log.d(TAG, "保存用户收藏失败");
				return false;
			}else {
				Log.d(TAG, "保存用户收藏成功");
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 从收藏列表删除
	 * @param id
	 * @return 成功删除返回true, 失败返回false.
	 */
	public boolean deleteUserCollect(int id) {
		// TODO Auto-generated method stub
		SQLiteDatabase db= mDatabaseHelper.getWritableDatabase();
		//删除条件
		String select =  FIELD_ID +"=?";
		String selectArgs[] = {String.valueOf(id)};
		int result = db.delete(TABLE_NAME_COLLECT, select, selectArgs);
		if (result == -1){
			return false;
		}
		return true;
	}
	
	/**
	 * 从搜索界面删除用户收藏
	 * @param userID 用户ID
	 * @param longitude 经度
	 * @param latitude 纬度
	 * @param placeName 地名
	 * @param address 地址名
	 * @return 删除成功返回true, 删除失败返回false
	 */
	public boolean delectUserCollect(String userID, double longitude, double latitude,String placeName, String address){
		SQLiteDatabase db= mDatabaseHelper.getWritableDatabase();
		String and = "AND ";
		//删除条件
		String select =  FIELD_USER_ID +"=?"+ and +
				         FIELD_PLACE_NAME +"=?" + and +
				         FIELD_ADDRESS + "=?";
		String selectArgs[] = {userID, placeName, address};
		
/*		String select = FIELD_PLACE_NAME +"=?";
		String selectArgs[] = {placeName};*/

		int result = db.delete(TABLE_NAME_COLLECT, select, selectArgs);
		if (result == -1){
			return false;
		}
		return true;
	}
	
	/**
	 * 检查当前位置是否保存在数据库里
	 * @param userID 用户ID
	 * @param longitude 经度
	 * @param latitude 纬度
	 * @param placeName 地名
	 * @param address 地址名
	 * @return 已保存返回true, 否则返回false
	 */
	public boolean checkUpAdress(String userID, double longitude, double latitude,String placeName, String address){
		String and = "AND ";
		//查询条件:根据用户ID, 地名，地址查询
		String[] columns={FIELD_USER_ID, 
				          FIELD_PLACE_NAME,
				          FIELD_ADDRESS};
		
		String select =  FIELD_USER_ID +"=? " + and +
						 FIELD_PLACE_NAME +"=?" + and +
						 FIELD_ADDRESS + "=?";
		
		String selectArgs[] = {userID,
							   placeName,
							   address};
/*		String[] columns={FIELD_PLACE_NAME};
		
		String select =  FIELD_PLACE_NAME +"=?";

		String selectArgs[] = {placeName};*/
		try {
			cursor = null;
			cursor = mSQLiteDatabase.query(TABLE_NAME_COLLECT, columns, select, selectArgs, null, null, "_id desc");
			if (cursor.getCount() > 0){
				return true;
			}		
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "查询数据库数据失败");
		}finally{
			if(cursor!=null&&!cursor.isClosed()){
				cursor.close();
			}
		}
		return false;
	}
}
