package com.baidu.rigel.biplatform.ma.ds.service.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.baidu.rigel.biplatform.ma.model.meta.BaseInfo;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;
import com.google.common.collect.Lists;

/**
 * 数据源信息读取之关系型数据库实现类
 * @author jiangyichao
 *
 */
public class RelationDBInfoReaderServiceImpl implements
		DataSourceInfoReaderService {
	
	/**
	 * 日志对象
	 */
	private static final Logger LOG = LoggerFactory.getLogger(
			RelationDBInfoReaderServiceImpl.class);
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	public List<TableInfo> getAllTableInfos(DataSourceDefine ds, String securityKey) {
		return this.getDbMetaData(ds, securityKey, null);
	}
	/**
	 * @{inheritDoc}
	 */
	@Override
	public List<ColumnInfo> getAllColumnInfos(DataSourceDefine ds, String securityKey, String tableId) {
		return this.getDbMetaData(ds, securityKey, tableId);
	}

	/**
	 * 获取数据库数据源对应数据表信息列表
	 * @param ds 数据源
	 * @param security 密钥
	 * @param tableId 表格id，在获取表格列表时，此值为null;在获取列详细列表时，此值为表格id
	 * @return 表格或列信息列表
	 */
	@SuppressWarnings("unchecked")
	private <T extends BaseInfo> List<T> getDbMetaData(DataSourceDefine ds, String securityKey, String tableId) {
        List<T> lists = Lists.newArrayList();
		DataSourceConnectionService<Connection> dsConnService = null;
		Connection conn = null;
		try {
			dsConnService = (DataSourceConnectionService<Connection>) DataSourceConnectionServiceFactory.
					getDataSourceConnectionServiceInstance(DataSourceType.MYSQL.name ());
			conn = dsConnService.createConnection(ds, securityKey);
			DatabaseMetaData dbMetaData = conn.getMetaData();
            if (StringUtils.hasText(tableId)) {
                String allTable = "%";
                ResultSet rs = null;
                try {
                    rs = dbMetaData.getColumns(null, null, tableId, allTable);
                } catch (Exception e) {
                    LOG.warn("get Column info warning");
                }
                while (rs != null && rs.next()) {
                    String colName = null;
                    String comment = null;
                    try {
                        colName = rs.getString("COLUMN_NAME");
                        comment = rs.getString("REMARKS");
                    } catch (Exception e) {
                        LOG.warn("get Column info warning");
                    }
                    ColumnInfo info = new ColumnInfo();
                    info.setId(colName);
                    info.setName(colName);
                    info.setComment(StringUtils.hasText(comment) ? comment : colName);
                    lists.add((T) info);
                }
            } else {
                String[] types = { "TABLE" };
                String allTable = "%";
                ResultSet rs = dbMetaData.getTables(null, null, allTable, types);
                while (rs.next()) {
                    TableInfo info = new TableInfo();
                    String tableName = null;
                    String comment = null;
                    try {
                        tableName = rs.getString("TABLE_NAME");
                        comment = rs.getString("REMARKS");
                    } catch (Exception e) {
                        LOG.warn("get Tables info warning");
                    }
                    info.setId(tableName);
                    info.setName(tableName);
                    info.setComment(comment);
                    lists.add((T) info);
                }
            }
		}catch (DataSourceOperationException e) {
			LOG.error("[ERROR] --- --- --- --- get ds connection instance error : {}", e.getMessage());
		} catch (DataSourceConnectionException e) {
			LOG.error("[ERROR] --- --- --- --- connection to ds error : {}", e.getMessage());
		} catch (SQLException e) {
			LOG.error("[ERROR] --- --- --- --- get db databaseMetadata error: {}", e.getMessage());
		} finally {
			if (dsConnService != null && conn != null) {
				try {
					dsConnService.closeConnection(conn);
				} catch (DataSourceConnectionException e) {
					LOG.error("[ERROR] --- --- --- --- close db connection error: {}", e.getMessage());
				}
			}
		}
		return lists; 
	}
}
