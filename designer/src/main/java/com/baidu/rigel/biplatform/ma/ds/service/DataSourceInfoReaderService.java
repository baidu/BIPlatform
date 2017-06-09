package com.baidu.rigel.biplatform.ma.ds.service;

import java.util.List;

import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;

/**
 * 数据源信息读取服务接口
 * @author jiangyichao
 *
 */
public interface DataSourceInfoReaderService {
	
	/**
	 * 获取数据源下对应的所有数据表
	 * @param ds 数据源定义
	 * @param securityKey 密钥
	 * @return 数据源下数据表列表
	 */
	public List<TableInfo> getAllTableInfos(DataSourceDefine ds, String securityKey);
	
	/**
	 * 获取指定数据表中所有的数据列名称
	 * @param ds 数据源定义
	 * @param securityKey 密钥
	 * @param tableId 数据表对应的id
	 * @return 该数据表所有的数据列名称
	 */
	public List<ColumnInfo> getAllColumnInfos(DataSourceDefine ds, String securityKey,String tableId);
}
