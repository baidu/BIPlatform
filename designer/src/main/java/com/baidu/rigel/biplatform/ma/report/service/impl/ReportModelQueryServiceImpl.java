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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.MiniCubeDriverManager;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 报表数据查询服务实现
 *
 * @author david.wang
 * @version 1.0.0.1
 */
@Service("reportModelQueryService")
public class ReportModelQueryServiceImpl implements ReportModelQueryService {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(ReportModelQueryServiceImpl.class);
    
    @Resource
    private DataSourceService dataSourceService;
    
    @Resource
    private DataSourceGroupService dataSourceGroupService;
    
    /**
     * 
     * {@inheritDoc}
     * @throws MiniCubeQueryException 
     * @throws DataSourceOperationException 
     * 
     */
    @Override
    public List<Member> getMembers(Cube cube, Dimension dimension, Level level, Map<String, String> params,
                String securityKey)
            throws MiniCubeQueryException, DataSourceOperationException {
        LinkedHashMap<String, Integer> levelIndexRep = Maps.newLinkedHashMap();
        int index = 0;
        for (String levelName : dimension.getLevels().keySet()) {
            levelIndexRep.put(levelName, index++);
        }
        // For Mock
        // List<Member> members = MockUtils.mockMembers(level);
        DataSourceDefine dsDefine = null;
        DataSourceInfo dsInfo = null;
        try {
            dsDefine = dataSourceService.getDsDefine(cube.getSchema().getDatasource(), params);
            DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory.
            		getDataSourceConnectionServiceInstance(dsDefine.getDataSourceType().name ());
            dsInfo = dsConnService.parseToDataSourceInfo(dsDefine, securityKey);
        } catch (DataSourceOperationException e) {
            logger.error("Fail in Finding datasource define. ", e);
            throw e;
        } catch (DataSourceConnectionException e) {
        	logger.error("Fail in parse datasource to datasourceInfo.", e);
        	throw new DataSourceOperationException(e);
        }
        List<Member> members;
        try {
            members = level.getMembers(cube, dsInfo, params);
        } catch (MiniCubeQueryException e) {
            logger.error("Fail in querying members of dim " + dimension.getName()
                    + " on level " + level.getName(), e);
            throw e;
        }
        
        return members;
    }
    
    /**
     * @param dim
     *            维度
     * @return 维度成员
     * @throws DataSourceOperationException 
     * @throws MiniCubeQueryException 
     */
    @Override
    public List<List<Member>> getMembers(Cube cube, Dimension dim, Map<String, String> params, String securityKey)
            throws MiniCubeQueryException, DataSourceOperationException {
        
        if (dim == null) {
            return Lists.newArrayList();
        }
        
        List<List<Member>> members = Lists.newArrayList();
        if (dim.getLevels() == null || dim.getLevels().size() == 0) {
            return Lists.newArrayList();
        }
        Level[] parentLevels = dim.getLevels().values().toArray(new Level[0]);
        List<Member> rootMembers = null;
        try {
            parentLevels[0].setDimension (dim);
            if (params.get(Constants.LEVEL_KEY) == null) {
                params.put(Constants.LEVEL_KEY, "ALL");
            }
            rootMembers = getMembers(cube, dim, parentLevels[0], params, securityKey);
        } catch (MiniCubeQueryException | DataSourceOperationException e) {
            logger.error("Exception happened when getMemebers of dim " + dim.getName(), e);
            throw e;
        }
        members.add(rootMembers);
        // 1.equeals(params.get(level)) 只取当前层级
        if (parentLevels.length > 1 && !"1".equals (params.get(Constants.LEVEL_KEY))) {
            for (int i = 1; i < parentLevels.length; ++i) {
                List<Member> tmpMember = Lists.newArrayList();
                for (Member m : rootMembers) {
                    // tmpMember.addAll(getMembers(cube, dim, m, parentLevels[i], params, securityKey));
                    if (((MiniCubeMember) m).getChildren() != null) {
                        tmpMember.addAll(((MiniCubeMember) m).getChildren());
                    }
                }
                members.add(tmpMember);
                rootMembers = tmpMember;
            }
        }
        return members;
    }
    
    /**
     * 
     * @param dim
     * @param m
     * @param level
     * @return
     * @throws DataSourceOperationException 
     * @throws MiniCubeQueryException 
     */
    private List<Member> getMembers(Cube cube, Dimension dim,
            Member parent, Level level, Map<String, String> params, String securityKey)
            throws MiniCubeQueryException, DataSourceOperationException {
        DataSourceDefine dsDefine = null;
        DataSourceInfo dsInfo = null;
        try {
            dsDefine = dataSourceService.getDsDefine(cube.getSchema().getDatasource(), params);
            DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory.
            		getDataSourceConnectionServiceInstance(dsDefine.getDataSourceType().name ());
            dsInfo = dsConnService.parseToDataSourceInfo(dsDefine, securityKey);
        } catch (DataSourceOperationException e) {
            logger.error("Fail in Finding datasource define. ", e);
            throw e;
        } catch (DataSourceConnectionException e) {
        	logger.error("Fail in parse datasource to datsourceInfo.", e);
        	throw new DataSourceOperationException(e);
        }
        List<Member> members = parent.getChildMembers(cube, dsInfo, params);
        for (Member m : members) {
            MiniCubeMember member = (MiniCubeMember) m;
            member.setParent(parent);
        }
        return members;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action, boolean usingCache,
                boolean needSumary, String securityKey)
            throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException {
        return this.queryDatas(model, action, usingCache, needSumary, null, securityKey);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws DataSourceOperationException
     * @throws QueryModelBuildException
     * @throws MiniCubeQueryException
     */
    @Override
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action,
            boolean usingCache, boolean needSumary, Map<String, Object> requestParams, String securityKey)
            throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException {
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageSize(100);
		pageInfo.setTotalPage(1);
		return this.queryDatasHelper(model, action, usingCache, needSumary, requestParams, pageInfo, securityKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action, boolean usingCache, Map<String, Object> requestParams,
    		PageInfo pageInfo, String securityKey) throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException {
    	return this.queryDatasHelper(model, action, usingCache, false, requestParams, pageInfo, securityKey);
    }
    
    /**
     * 查询数据的辅助类
     */
    private ResultSet queryDatasHelper(ReportDesignModel model, QueryAction action, boolean usingCache, 
    		boolean needSumary, Map<String, Object> requestParams, PageInfo pageInfo, String securityKey) 
    		throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException {
        ResultSet rs = new ResultSet();
        DataSourceDefine dsDefine;
        DataSourceInfo dsInfo = null;
        // 获取数据源连接信息
        String queryDsId = model.getDsId();
        dsDefine = dataSourceService.getDsDefine(queryDsId, requestParams);
        DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory
                .getDataSourceConnectionServiceInstance(dsDefine.getDataSourceType().name());
        try {
            dsInfo = dsConnService.parseToDataSourceInfo(dsDefine, securityKey);
        } catch (DataSourceConnectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        MiniCubeConnection connection = MiniCubeDriverManager.getConnection(dsInfo);
        QuestionModel questionModel;
        try {
            // 获取问题模型
            questionModel = QueryUtils.convert2QuestionModel(dsDefine, model, action, requestParams, 
                    pageInfo, securityKey);
        } catch (QueryModelBuildException e) {
            if (model.getExtendById(action.getExtendAreaId()).getLogicModel() != null) {
                logger.debug("Fail in building question model ! ", e);
                throw e;
            }
            logger.debug("Logic Model is null at present. ");
            return rs;
        }
        DataModel dataModel;
        try {
            long start = new Date().getTime();
            questionModel.setQueryId(UuidGeneratorUtils.generate());
            dataModel = connection.query(questionModel);
            long end = new Date().getTime();
            long seconds = (end - start) / 1000;
            logger.debug("Query with queryId " + questionModel.getQueryId() + " Cost: " + seconds);
        } catch (MiniCubeQueryException e) {
            logger.error("Fail in quering data ! ", e);
            throw e;
        }
        rs.setDataModel(dataModel);
        return rs;
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> getMembers(Cube cube, String uniqueName,
            Map<String, String> params, String securityKey) {
        DataSourceDefine dsDefine = null;
        DataSourceInfo dsInfo = null;
        try {
            dsDefine = dataSourceService.getDsDefine(cube.getSchema().getDatasource(), params);
            DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory.
            		getDataSourceConnectionServiceInstance(dsDefine.getDataSourceType().name ());
            dsInfo = dsConnService.parseToDataSourceInfo(dsDefine, securityKey);
        } catch (DataSourceOperationException e) {
            logger.error("Fail in Finding datasource define. ", e);
            throw new RuntimeException(e);
        } catch (DataSourceConnectionException e) {
        	logger.error("Fail in parse datasource to datasourceInfo.", e);
        	throw new RuntimeException(e);
        }
        Member member = cube.lookUp (dsInfo, uniqueName, params);
        if (member != null) {
            return member.getChildMembers (cube, dsInfo, params);
        }
        return Lists.newArrayList ();
    }  
    
    
}
