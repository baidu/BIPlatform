package com.baidu.rigel.biplatform.ma.ds.service;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;

/**
 * 数据源连接服务DataSourceConnectionService的实例化工厂
 * @author jiangyichao
 *
 */
public class DataSourceConnectionServiceFactory {

	/**
	 * 返回数据源连接实例
	 * @param sourceType 数据源来源信息
	 * @return 
	 * @return 数据源连接服务实例
	 * @throws DataSourceOperationException 未知数据源类型
	 */
	public static DataSourceConnectionService<?> getDataSourceConnectionServiceInstance(String sourceType)
			throws DataSourceOperationException {
	    return DbConnectionServiceHelper.getDsMetaService (sourceType);
//		switch(sourceType) {
//			// 关系数据库
//			case MYSQL:
//			case MYSQL_DBPROXY:
//			case H2:
//			case ORACLE:
//				return new RelationDBConnectionServiceImpl();
//			// 列式数据库
//			case COL_DATABASE:
//			// EXCEL文件
//			case EXCEL:
//			// CSV文件
//			case CSV:
//			// TXT文件
//			case TXT:
//			// HDFS文件系统
//			case HDFS:
//			// 未支持数据源
//			default:
//				throw new DataSourceOperationException("unknow datasource type:"
//                        + sourceType);
//		}
	}
}
