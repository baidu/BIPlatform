package com.baidu.rigel.biplatform.ma.model.ds;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 数据源组定义
 * @author jiangyichao
 *
 */
public class DataSourceGroupDefine implements Serializable {

	/**
	 * 序列id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 数据源组id
	 */
	private String id;
	
	/**
	 * 数据源组名称
	 */
	private String name;
	
	/**
	 * 产品线名称
	 */
	private String productLine;
	
	/**
	 * 当前使用的数据源
	 */
	private DataSourceDefine activeDataSource;
	
	/**
	 * 该数据源组下的数据源列表，其中key：数据源id，value：为数据源定义
	 */
	private Map<String, DataSourceDefine> dataSourceList = Maps.newLinkedHashMap();
	
	/**
	 * 获取数据源组id
	 * @return 数据源组id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 设置数据源组id
	 * @param id 数据源组id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取数据源组名称
	 * @return 数据源组名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置数据源组名称
	 * @param name 数据源组名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 获取数据源组所属产品线
	 * @return
	 */
	public String getProductLine() {
		return productLine;
	}

	/**
	 * 设置数据源组所属产品线
	 * @param productLine 产品线名称
	 */
	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	/**
	 * 获取当前使用数据源
	 * @return 当前使用数据源
	 */
	public DataSourceDefine getActiveDataSource() {
		return activeDataSource;
	}

	/**
	 * 设置当前使用数据源
	 * @param activeDataSource 使用数据源
	 */
	public void setActiveDataSource(DataSourceDefine activeDataSource) {
		this.activeDataSource = activeDataSource;
	}
	
	/**
	 * 获取数据源列表
	 * @return 可用数据源列表
	 */
	public Map<String, DataSourceDefine> getDataSourceList() {
		if (this.dataSourceList == null) {
			return Maps.newHashMap();
		}
		return dataSourceList;
	}
	
	/**
	 * 设置数据源列表
	 * @param dataSourceList 数据源列表
	 */
	public void setDataSourceList(Map<String, DataSourceDefine> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}
	
	/**
	 * 判断当前数据源组是否包含该数据源
	 * @param ds 数据源定义
	 * @return 如果包含，则返回true，否则返回false
	 */
	public boolean contais(DataSourceDefine ds) {
		if (ds == null) {
			return false;
		}
		if (this.dataSourceList == null) {
			return false;
		}
		return this.dataSourceList.containsKey(ds.getId());
	}
	
	/**
	 * 向数据源组中添加数据源
	 * @param ds 待添加数据源
	 * @return 是否成功添加
	 */
	public boolean addDataSourceDefine(DataSourceDefine ds) {
		if (this.dataSourceList == null) {
			dataSourceList = Maps.newLinkedHashMap();
		}
		if (ds == null) {
			return false;
		}
		dataSourceList.put(ds.getId(), ds);
		return true;
	}
	
	/**
	 * 删除数据源组中的数据源
	 * @param ds 待删除数据源
	 * @return 是否成功删除
	 */
	public boolean removeDataSourceDefine(DataSourceDefine ds) {
		if (dataSourceList == null) {
			return false;
		}
		if (ds == null) {
			return false;
		}
		if (dataSourceList.containsKey(ds.getId())) {
			dataSourceList.remove(ds.getId());
			return true;
		}
		return false;
	}
	
	/**
	 * 获取该数据源组中所有的数据源定义
	 * @return 数据源定义
	 */
	public DataSourceDefine[] listAll() {
		if (this.dataSourceList != null) {
			return this.dataSourceList.values().toArray(new DataSourceDefine[0]);
		}
		return null;
	}
	
	public boolean existName(String name) {
		if (dataSourceList == null) {
			dataSourceList = Maps.newLinkedHashMap();
			return false;
		}
		// 同一数据源组中的各个数据源要求名称唯一
		for (DataSourceDefine tempDs : dataSourceList.values()) {
			if (tempDs.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
