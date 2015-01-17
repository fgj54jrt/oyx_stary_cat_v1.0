package com.cwits.cyx_drive_sdk.db;

import java.util.ArrayList;

/**
 * 搜索历史相关接口
 * 
 */
public interface ISeachHistoryDataBase {

	/**
	 * 存储
	 */
	public boolean saveSeachHistory(String content);

	/**
	 * 查询
	 */
	public ArrayList<String> querySeachHistory();

	/**
	 * 删除
	 */
	public boolean deleteSeachHistory();

}