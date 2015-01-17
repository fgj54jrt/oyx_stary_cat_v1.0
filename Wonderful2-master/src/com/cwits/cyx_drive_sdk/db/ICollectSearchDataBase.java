package com.cwits.cyx_drive_sdk.db;

import java.util.ArrayList;

/**
 * 收藏用户搜索记录
 */
public interface ICollectSearchDataBase {
	/**
	 * 收藏用户搜索记录
	 */
	public boolean collectSearchHistory(String content);

	/**
	 * 查询用户收藏，返回全部收藏
	 */
	public ArrayList<String> queryCollectSeach();

	/**
	 * 删除用户指定收藏
	 * @param id 列表ID
	 */
	public void delectCollect(int id);

}
