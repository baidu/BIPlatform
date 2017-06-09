/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.ma.model.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.DimSourceType;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ReferenceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimType;
import com.baidu.rigel.biplatform.ma.model.meta.UserDefineDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService;
import com.baidu.rigel.biplatform.ma.model.utils.HttpUrlUtils;
import com.baidu.rigel.biplatform.ma.model.utils.TimeTypeAdaptorUtils;
import com.baidu.rigel.biplatform.ma.resource.view.RelationTableView;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CallbackDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CustDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.DateDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.NormalDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.RefreshType;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CallbackDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CustDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.DateDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.NormalDimBindView;
import com.google.common.collect.Lists;

/**
 * 星型模型构建的实现类
 * 
 * @author zhongyi
 *
 *         2014-7-29
 */
@Service("starModelBuildService")
public class StarModelBuildServiceImpl implements StarModelBuildService {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(StarModelBuildServiceImpl.class);
    
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * buildStarModel(java.util.List)
     */
    @Override
    public List<StarModel> buildStarModel(String dsId, List<FactTableMetaDefine> cubeTables) {
        List<StarModel> models = Lists.newArrayList();
        for (FactTableMetaDefine factTable : cubeTables) {
            StarModel model = new StarModel();
            model.setCubeId(factTable.getCubeId());
            model.setDsId(dsId);
            model.setFactTable(factTable);
            models.add(model);
        }
        return models;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * getAllTablesAndCols(java.lang.String)
     */
    @Override
    public List<RelationTableView> getAllTablesAndCols(String dsId, String securityKey) 
        throws DataSourceOperationException {
        DataSourceDefine ds = null;
        try {
            ds = dsService.getDsDefine(dsId);
        } catch (DataSourceOperationException e) {
            LOG.error("Fail in getting ds by id: " + dsId);
            throw e;
        }
        
        if (ds == null) {
            return Lists.newArrayList();
        }
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            dsInfoReaderService = DataSourceInfoReaderServiceFactory.
                getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
            List<TableInfo> tables = dsInfoReaderService.getAllTableInfos(ds, securityKey);
            List<RelationTableView> relationTables = Lists.newArrayList();
            for (TableInfo table : tables) {
                RelationTableView relation = new RelationTableView();
                relation.setId(table.getId());
                relation.setName(table.getName());
                List<ColumnInfo> cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, table.getId());
                relation.setFields(cols);
                relationTables.add(relation);
            }
            return relationTables;
        } catch(Exception e) {
            LOG.error("[ERROR] --- --- --- --- fail to get columnInfos from datasource : {}", e.getMessage());
            LOG.error("[ERROR] --- --- --- --- stackTrace :", e);
        	throw new DataSourceOperationException(e);
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateMetaDefine
     * (com.baidu.rigel.biplatform.ma.resource.view.dimView.CallbackDimBindView,
     * java.util.Map)
     */
    @Override
    public List<CallbackDimTableMetaDefine> generateMetaDefine(CallbackDimBindView callback,
            Map<String, String> names) {
        List<CallbackDimTableMetaDefine> callbackMetaDefines = Lists.newArrayList();
        if (callback == null) {
            LOG.error("callback view is null!");
            return callbackMetaDefines;
        }
        for (CallbackDimDetail detail : callback.getChildren()) {
            CallbackDimTableMetaDefine callbackMetaDefine = new CallbackDimTableMetaDefine();
            String baseUrl = HttpUrlUtils.getBaseUrl(detail.getAddress());
            callbackMetaDefine.setUrl(baseUrl);
            Map<String, String> params = HttpUrlUtils.getParams(detail.getAddress());
            callbackMetaDefine.setParams(params);
         // modify by jiangyichao at 2014-09-12
            // 添加刷新时间和刷新类型
            int refreshType = detail.getRefreshType();
            // 构造枚举类型变量
            RefreshType refresh = RefreshType.values()[refreshType - 1];
            int interval = 0;
            // 根据刷新时间类型，设置刷新间隔
            switch (refresh) {
                case NO_REFRESH:
                    interval = 0;
                    break;
                case REFRESH_WITH_CUBE:
                    interval = -1;
                    break;
                case REFRESH_WITH_INTERVAL:
                    interval = detail.getInterval();
                    break;
                default:
                    break;
            } 
            callbackMetaDefine.addConfigItem(CallbackDimTableMetaDefine.REF_INTERNAL_KEY,
                    String.valueOf(interval));
            callbackMetaDefine.addConfigItem(CallbackDimTableMetaDefine.REFRESH_KEY, 
                    String.valueOf(refreshType));
            ReferenceDefine reference = new ReferenceDefine();
            reference.setMajorColumn(detail.getCurrDim());
            callbackMetaDefine.setReference(reference);
            
            callbackMetaDefine.setName(detail.getCurrDim());
            // modify by jiangyichao at 2014-09-12
            // add column message
            ColumnMetaDefine column = new ColumnMetaDefine(); 
            if (names == null || names.size() == 0 
                    || StringUtils.isEmpty(reference.getMajorColumn()) 
                    || !names.containsKey(reference.getMajorColumn())) {
                column.setCaption(detail.getCurrDim());
                column.setName(detail.getCurrDim());
            } else {
                column.setCaption(names.get(reference.getMajorColumn()));
                column.setName(names.get(reference.getMajorColumn()));
            }
            callbackMetaDefine.addColumn(column);
            callbackMetaDefines.add(callbackMetaDefine);
        }
        return callbackMetaDefines;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateMetaDefine
     * (com.baidu.rigel.biplatform.ma.resource.view.dimView.DateDimBindView,
     * java.util.Map)
     */
    @Override
    public List<TimeDimTableMetaDefine> generateMetaDefine(DateDimBindView date,
            Map<String, String> names) {
        List<TimeDimTableMetaDefine> dateMetaDefines = Lists.newArrayList();
        for (DateDimDetail detail : date.getChildren()) {
            TimeType timeType = TimeTypeAdaptorUtils.parseToTimeType(detail.getField());
            TimeDimType type = TimeDimType.TIME;
            // TODO 内置时间的特熟表名
            if ("ownertable".equals(detail.getRelationTable())) {
                type = TimeDimType.STANDARD_TIME;
            }
            TimeDimTableMetaDefine dateMetaDefine = new TimeDimTableMetaDefine(type);
            // 日期格式
            dateMetaDefine.setFormat(detail.getFormat());
            ReferenceDefine reference = new ReferenceDefine();
            
            // 事实表中的列
            reference.setMajorColumn(detail.getCurrDim());
            // 时间维度表中对外关联的列
            reference.setSalveColumn(timeType.name());
            dateMetaDefine.setReference(reference);
            dateMetaDefine.setName(detail.getRelationTable());
            
            // 设置时间维度列 默认一个时间维度对应一列 只有内置时间维度才有此情况
            ColumnMetaDefine column = new ColumnMetaDefine();
            column.setCaption(detail.getField());
            column.setName(detail.getField());
            dateMetaDefine.addColumns(parseToDefine(detail));
            dateMetaDefines.add(dateMetaDefine);
        }
        return dateMetaDefines;
    }
    
    private List<ColumnMetaDefine> parseToDefine(DateDimDetail detail) {
        
        List<ColumnMetaDefine> cols = Lists.newArrayList();
        TimeType timeType = TimeTypeAdaptorUtils.parseToTimeType(detail.getField());
        switch (timeType) {
            case TimeYear: {
                ColumnMetaDefine colYear = buildTimeCol("年份", TimeType.TimeYear.name());
                cols.add(colYear);
                break;
            }
            case TimeQuarter: {
                ColumnMetaDefine colQuarter = buildTimeCol("季度", TimeType.TimeQuarter.name());
                cols.add(colQuarter);
                ColumnMetaDefine colYear = buildTimeCol("年份", TimeType.TimeYear.name());
                cols.add(colYear);
                break;
            }
            case TimeMonth: {
                ColumnMetaDefine colMonth = buildTimeCol("月份", TimeType.TimeMonth.name());
                cols.add(colMonth);
                ColumnMetaDefine colQuarter = buildTimeCol("季度", TimeType.TimeQuarter.name());
                cols.add(colQuarter);
                ColumnMetaDefine colYear = buildTimeCol("年份", TimeType.TimeYear.name());
                cols.add(colYear);
                break;
            }
            case TimeWeekly: {
                ColumnMetaDefine colWeek = buildTimeCol("星期", TimeType.TimeWeekly.name());
                cols.add(colWeek);
                ColumnMetaDefine colMonth = buildTimeCol("月份", TimeType.TimeMonth.name());
                cols.add(colMonth);
                ColumnMetaDefine colQuarter = buildTimeCol("季度", TimeType.TimeQuarter.name());
                cols.add(colQuarter);
                ColumnMetaDefine colYear = buildTimeCol("年份", TimeType.TimeYear.name());
                cols.add(colYear);
                break;
            }
            case TimeDay: {
                ColumnMetaDefine colDay = buildTimeCol("天", TimeType.TimeDay.name());
                cols.add(colDay);
                ColumnMetaDefine colWeek = buildTimeCol("星期", TimeType.TimeWeekly.name());
                cols.add(colWeek);
                ColumnMetaDefine colMonth = buildTimeCol("月份", TimeType.TimeMonth.name());
                cols.add(colMonth);
                ColumnMetaDefine colQuarter = buildTimeCol("季度", TimeType.TimeQuarter.name());
                cols.add(colQuarter);
                ColumnMetaDefine colYear = buildTimeCol("年份", TimeType.TimeYear.name());
                cols.add(colYear);
                break;
            }
            default:
        }
        return cols;
    }
    
    private ColumnMetaDefine buildTimeCol(String caption, String name) {
        ColumnMetaDefine define = new ColumnMetaDefine();
        define.setCaption(caption);
        define.setName(name);
        return define;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateMetaDefine
     * (com.baidu.rigel.biplatform.ma.resource.view.dimView.CustDimBindView,
     * java.util.Map)
     */
    @Override
    public List<UserDefineDimTableMetaDefine> generateMetaDefine(CustDimBindView cust,
            Map<String, String> names) {
        List<UserDefineDimTableMetaDefine> custMetaDefines = Lists.newArrayList();
        for (CustDimDetail detail : cust.getChildren()) {
            UserDefineDimTableMetaDefine custMetaDefine = new UserDefineDimTableMetaDefine();
            custMetaDefine.setName(detail.getDimName());
            custMetaDefine.setSourceType(DimSourceType.SQL);
            custMetaDefine.setValue(detail.getSql());
            // TODO param and sql ...
            // custMetaDefine.setParams(params);
            custMetaDefines.add(custMetaDefine);
        }
        return custMetaDefines;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateMetaDefine
     * (com.baidu.rigel.biplatform.ma.resource.view.dimView.NormalDimBindView,
     * java.util.Map)
     */
    @Override
    public List<StandardDimTableMetaDefine> generateMetaDefine(String dsId,
            NormalDimBindView normal, Map<String, String> names, String securityKey) 
            throws DataSourceOperationException {
        List<StandardDimTableMetaDefine> standMetaDefines = Lists.newArrayList();
        
        DataSourceDefine ds = null;
        try {
            ds = dsService.getDsDefine(dsId);
        } catch (DataSourceOperationException e) {
            LOG.error("Fail in getting ds by id: " + dsId, e);
            throw e;
        }
        
        if (ds == null) {
            return Lists.newArrayList();
        }
        
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            dsInfoReaderService = DataSourceInfoReaderServiceFactory.
                getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
            for (NormalDimDetail detail : normal.getChildren()) {
                StandardDimTableMetaDefine stand = new StandardDimTableMetaDefine();
                ReferenceDefine reference = new ReferenceDefine();
                reference.setMajorColumn(detail.getCurrDim());
                reference.setSalveColumn(detail.getField());
                stand.setReference(reference);
                stand.setName(detail.getRelationTable());
                
                List<ColumnInfo> cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, detail.getRelationTable());
                stand.addColumns(parseToDefine(cols));
                standMetaDefines.add(stand);
            }
        } catch (Exception e) {
        	LOG.error("fail to get columnInfos from datasource");
        	throw new DataSourceOperationException(e);
        }
        return standMetaDefines;
    }
    
    /**
     * 
     * @param cols
     * @return
     */
    private List<ColumnMetaDefine> parseToDefine(List<ColumnInfo> cols) {
        List<ColumnMetaDefine> defines = Lists.newArrayList();
        for (ColumnInfo col : cols) {
            ColumnMetaDefine define = new ColumnMetaDefine();
//            define.setName(StringUtils.hasText(col.getComment())? col.getName() : col.getId());
//            define.setCaption(StringUtils.hasText(col.getComment())? col.getComment() :col.getName());
            define.setCaption(col.getComment());
            define.setName(col.getName());
            defines.add(define);
        }
        return defines;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateCallbackDimDetail
     * (com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine)
     */
    @Override
    public CallbackDimDetail generateCallbackDimDetail(CallbackDimTableMetaDefine dimTable) {
        CallbackDimDetail callbackDim = new CallbackDimDetail();
        callbackDim.setAddress(getUrlWithParams(dimTable.getUrl(), dimTable.getParams()));
        callbackDim.setCurrDim(dimTable.getReference().getMajorColumn());
        // modify by jiangyichao at 2014-09-11
        // 获取刷新类型和刷新间隔的配置信息
        Map<String, String> configuration = dimTable.getConfiguration();
        // 设置刷新时间间隔
        if (configuration.containsKey(CallbackDimTableMetaDefine.REF_INTERNAL_KEY)) {
            String interval = configuration.get(CallbackDimTableMetaDefine.REF_INTERNAL_KEY);  
            try {
                callbackDim.setInterval(Integer.valueOf(interval));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("the type of refresh interval must be integer, "
                        + "but now the interval is " + interval);
            }
        } else {
            // 设置同cube一同刷新
            callbackDim.setInterval(Integer.valueOf(-1));
        }
        // 设置刷新时间类型
        if (configuration.containsKey(CallbackDimTableMetaDefine.REFRESH_KEY)) {
            String refreshType = configuration.get(CallbackDimTableMetaDefine.REFRESH_KEY);
            try {
                callbackDim.setRefreshType(Integer.valueOf(refreshType));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("the value of refresh type must be 1,2,or 3, "
                        + "but now the refresh type  is " + refreshType);
            }
        } else {
            // 设置刷新类型为：同cube一同刷新
            callbackDim.setRefreshType(Integer.valueOf(RefreshType.REFRESH_WITH_CUBE.getRefreshType()));
        }
        return callbackDim;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateCustDimDetail
     * (com.baidu.rigel.biplatform.ma.model.meta.UserDefineDimTableMetaDefine)
     */
    @Override
    public CustDimDetail generateCustDimDetail(UserDefineDimTableMetaDefine dimTable) {
        CustDimDetail custDim = new CustDimDetail();
        custDim.setDimName(dimTable.getName());
        custDim.setSql(dimTable.getValue());
        return custDim;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateNormalDimBindView
     * (com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine)
     */
    @Override
    public NormalDimDetail generateNormalDimBindView(StandardDimTableMetaDefine dimTable) {
        NormalDimDetail dim = new NormalDimDetail();
        if (StringUtils.isEmpty(dimTable.getReference().getMajorColumn()) 
                    || StringUtils.isEmpty(dimTable.getReference().getSalveColumn())
                    || StringUtils.isEmpty(dimTable.getName())) {
            return null;
        }
        dim.setCurrDim(dimTable.getReference().getMajorColumn());
        dim.setField(dimTable.getReference().getSalveColumn());
        dim.setRelationTable(dimTable.getName());
        return dim;
    }
    
    private String getUrlWithParams(String urlPrefix, Map<String, String> params) {
        // modify by jiangyichao at 2014-09-11
        // 组成带参数的url
        if (params == null) {
            return urlPrefix;
        }
        StringBuilder url = new StringBuilder();
        url.append(urlPrefix);
        url.append("?");
        // 参数个数
        int size = params.size();
        int index = 1;
        // 把参数放入到url中
        for (Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey() + "=");
            url.append(entry.getValue());
            if (index != size) {
                url.append("&");
            }
            index++;
        }
        return url.toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService#
     * generateDateDimDetail(java.lang.String,
     * com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine)
     */
    @Override
    public DateDimDetail generateDateDimDetail(String cubeId, TimeDimTableMetaDefine dimTable) {
        DateDimDetail dateDim = new DateDimDetail();
        dateDim.setCurrDim(dimTable.getReference().getMajorColumn());
        /**
         * TODO 非内置时间可能需要指定时间维度表中各个字段的时间level
         */
        dateDim.setField(dimTable.getReference().getSalveColumn());
        dateDim.setFormat(dimTable.getFormat());
        dateDim.setRelationTable(dimTable.getName());
        return dateDim;
    }
    
}