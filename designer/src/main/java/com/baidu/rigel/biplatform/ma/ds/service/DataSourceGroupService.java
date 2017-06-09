package com.baidu.rigel.biplatform.ma.ds.service;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;

/**
 * 数据源组服务操作接口
 * @author jiangyichao
 *
 */
public interface DataSourceGroupService {
	
	/**
	 * 保存或者更新数据源组
	 * @param dsG 数据源组
	 * @param securityKey 密钥
	 * @return 数据源组定义
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public DataSourceGroupDefine saveOrUpdateDataSourceGroup(DataSourceGroupDefine dsG, 
			String securityKey) throws DataSourceOperationException;
	
	/**
	 * 删除数据源组
	 * @param id 数据源组id
	 * @return 是否成功删除该数据源组，成功返回true，否则返回false
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public boolean removeDataSourceGroup(String id) throws DataSourceOperationException;
	
	/**
	 * 判断名称是否存在，对于某个产品线下，需要求名称唯一
	 * @param name 数据源组名称
	 * @return 名称是否唯一
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public boolean isNameExist(String name) throws DataSourceOperationException;
	
	/**
	 * 判断该数据源组中的数据源是否有效
	 * @param dsG 数据源组
	 * @param securityKey 密钥
	 * @return 该数据源组的所有数据源是否有效
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public boolean isValidate(DataSourceGroupDefine dsG, String securityKey) throws DataSourceOperationException;
	
	/**
	 * 获取当前产品线下的所有数据源组定义
	 * @return 所有数据源组构成的数组
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public DataSourceGroupDefine[] listAll() throws DataSourceOperationException;
	
	/**
	 * 根据id获取数据源组定义
	 * @param id 数据源组id
	 * @return 该id对应的数据源组定义或者null
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public DataSourceGroupDefine getDataSourceGroupDefine(String id) throws DataSourceOperationException;
	
	/**
	 * 根据产品线和数据源组名称获取数据源组定义
	 * @param productLine 产品线名称
	 * @param name
	 * @return
	 * @throws DataSourceOperationException
	 */
	public DataSourceGroupDefine getDataSourceGroupDefine(String productLine, String name) throws DataSourceOperationException;
	/**
	 * 根据数据源组id和数据源id，获取数据源定义
	 * @param id 数据源组id
	 * @param subId 数据源id
	 * @return 数据源定义
	 * @throws DataSourceOperationException 数据源操作异常
	 */
	public DataSourceDefine getDataSourceDefine(String id, String subId) throws DataSourceOperationException;
}
