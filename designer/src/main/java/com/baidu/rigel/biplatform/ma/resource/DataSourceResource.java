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
package com.baidu.rigel.biplatform.ma.resource;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;
import com.baidu.rigel.biplatform.ma.model.service.CubeMetaBuildService;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.resource.cache.CacheManagerForResource;
import com.baidu.rigel.biplatform.ma.resource.cache.NameCheckCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * 数据源管理Rest服务接口：提供对客户端进行数据源管理操作的支持
 * 
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad/dsgroup")
public class DataSourceResource extends BaseResource {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger (DataSourceResource.class);
    
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /**
     * dsGroupService
     */
    @Resource(name = "dsGroupService")
    private DataSourceGroupService dsGroupService;
    
    /**
     * cubeMetaBuildService
     */
    @Resource
    private CubeMetaBuildService cubeMetaBuildService;
    
    /**
     * reportDesignModelService
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;
    
    /**
     * cacheManagerForResource
     */
    @Resource
    private CacheManagerForResource cacheManagerForResource;
    
    /**
     * nameCheckCacheManager
     */
    @Resource
    private NameCheckCacheManager nameCheckCacheManager;
    
    /**
     * 
     * @param dsId
     * @return
     */
    @RequestMapping(value = "{id}/datasources/{subId}/tables", method = { RequestMethod.GET })
    public ResponseResult getAllTables(@PathVariable("id") String dsGId,
            @PathVariable("subId") String dsId) {
        List<TableInfo> tables = null;
        try {
            tables = cubeMetaBuildService.getAllTable (dsGId, securityKey);
        } catch (DataSourceOperationException e) {
            logger.error ("fail in get all table from ds. ds id: " + dsGId, e);
            return ResourceUtils.getCorrectResult ("未查到表定义信息", Maps.newHashMap ());
        }
        Map<String, Object> data = Maps.newHashMap ();
        data.put ("tables", tables);
        ResponseResult rs = ResourceUtils.getResult ("successfully",
                "can not get mode define info", tables);
        logger.info ("query operation rs is : " + rs.toString ());
        return rs;
    }
    
    /**
     * 获取当前所有的活动数据源
     * 
     * @return
     */
    
    @RequestMapping(value = "/active", method = { RequestMethod.GET })
    public ResponseResult listAllActive() {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine[] lists = dsGroupService.listAll ();
            if (lists == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("当前产品线下未定义任何数据源组");
            } else {
                rs.setStatus (0);
                rs.setStatusInfo ("successfully");
                List<Map<String, Object>> datas = Lists.newArrayList ();
                for (DataSourceGroupDefine dsG : lists) {
                    Map<String, Object> data = Maps.newHashMap ();
                    data.put ("id", dsG.getId ());
                    data.put ("name", dsG.getName ());
                    data.put ("active", dsG.getActiveDataSource ());
                    datas.add (data);
                }
                rs.setData (datas);
            }
        } catch (DataSourceOperationException e) {
            logger.error (e.getMessage ());
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 获取所有数据源组列表
     * 
     * @return 数据源组定义列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseResult listAllDsGroup() {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine[] lists = dsGroupService.listAll ();
            if (lists == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("当前产品线下未定义任何数据源组");
            } else {
                rs.setStatus (0);
                rs.setStatusInfo ("successfully");
                List<Map<String, Object>> datas = Lists.newArrayList ();
                for (DataSourceGroupDefine dsG : lists) {
                    Map<String, Object> data = Maps.newHashMap ();
                    data.put ("id", dsG.getId ());
                    data.put ("name", dsG.getName ());
                    if (dsG.getActiveDataSource () != null) {
                        data.put ("active", dsG.getActiveDataSource ().getId ());
                    }
                    data.put ("dsList", dsG.getDataSourceList ());
                    datas.add (data);
                }
                rs.setData (datas);
            }
        } catch (DataSourceOperationException e) {
            logger.error (e.getMessage ());
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 根据数据源组id获取数据源组信息
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.GET })
    public ResponseResult getDataSourceGroupById(@PathVariable("id") String id) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("未能找到数据源组定义, id: " + id);
            } else {
                rs.setStatus (0);
                rs.setStatusInfo ("successfully");
                rs.setData (dsG);
            }
        } catch (Exception e) {
            logger.error (e.getMessage ());
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 依据数据源组id和数据源id获取数据源定义
     * 
     * @param id
     *            数据源组id
     * @param subId
     *            数据源id
     * @return
     */
    @RequestMapping(value = "/{id}/datasources/{subId}", method = { RequestMethod.GET })
    public ResponseResult getDataSourceDefineByIdAndSubId(
            @PathVariable("id") String id, @PathVariable("subId") String subId) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            DataSourceDefine ds = dsGroupService
                    .getDataSourceDefine (id, subId);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("can't get datasource group with Id [ " + id
                        + "]");
            } else {
                if (ds == null) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("can't get datasource with subId [ "
                            + subId + "]");
                } else {
                    Map<String, Object> data = Maps.newHashMap ();
                    data.put ("id", dsG.getId ());
                    data.put ("name", dsG.getName ());
                    data.put ("ds", ds);
                    rs.setStatus (0);
                    rs.setStatusInfo ("successfully");
                    rs.setData (data);
                }
            }
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error (e.getMessage ());
        }
        return rs;
    }
    
    // /**
    // * 依据数据源id获取数据源信息
    // *
    // * @param productLine
    // * @return
    // */
    // @RequestMapping(value = "/{id}", method = { RequestMethod.GET })
    // public ResponseResult getDataSourceById(@PathVariable("id") String id) {
    // ResponseResult rs = new ResponseResult();
    // try {
    // DataSourceDefine define = dsService.getDsDefine(id);
    // if (define == null) {
    // rs.setStatus(1);
    // rs.setStatusInfo("未能找到对于数据源定义，id : " + id);
    // } else {
    // define.setDbPwd(define.getDbPwd()); //
    // AesUtil.getInstance().encrypt(define.getDbPwd(), securityKey));
    // rs.setStatus(0);
    // rs.setStatusInfo("successfully");
    // rs.setData(define);
    // }
    // } catch (Exception e) {
    // rs.setStatus(1);
    // rs.setStatusInfo("error : " + e.getMessage());
    // logger.error(e.getMessage(), e);
    // }
    // return rs;
    // }
    
    /**
     * 保存数据源组
     * 
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseResult saveDataSourceGroup(HttpServletRequest request) {
        ResponseResult rs = new ResponseResult ();
        // 数据源组名称
        String name = request.getParameter ("name");
        // 校验名称
        if (StringUtils.isEmpty (name) || name.length () > 255) {
            logger.debug ("name is empty or length more than 255");
            rs.setStatus (1);
            rs.setStatusInfo ("名称为空或者太长，请重新输入合法名称，长度不能超过255个字符");
        }
        DataSourceGroupDefine dsG = new DataSourceGroupDefine ();
        dsG.setId (UuidGeneratorUtils.generate ());
        dsG.setName (name);
        String productLine = ContextManager.getProductLine ();
        dsG.setProductLine (productLine);
        try {
            dsG = dsGroupService.saveOrUpdateDataSourceGroup (dsG, securityKey);
            rs.setStatus (0);
            rs.setStatusInfo ("successfully");
            rs.setData (dsG);
            logger.info ("successfully save datasource group :" + name);
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error ("未能正确保存数据源组," + e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 获取指定id数据源组下的所有数据源
     * 
     * @param id
     *            数据源组id
     * @return
     */
    public ResponseResult getDataSourceInGroupByGroupId(
            @PathVariable("id") String id) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("can't get datasource group with id: " + id);
            } else {
                DataSourceDefine[] ds = dsG.listAll ();
                if (ds == null) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("该数据源组下没有任何数据源定义");
                } else {
                    if (dsG.getActiveDataSource () == null) {
                        rs.setStatus (1);
                        rs.setStatusInfo ("该数据源组下未选中任何数据源");
                    } else {
                        rs.setStatus (0);
                        rs.setStatusInfo ("successfully");
                        Map<String, Object> data = Maps.newHashMap ();
                        data.put ("dsList", ds);
                        data.put ("activeDs", dsG.getActiveDataSource ());
                        rs.setData (data);
                        logger.info ("successfully get datasource in ds group");
                    }
                }
            }
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error ("get datasource group happen exception");
        }
        return rs;
    }
    
    /**
     * 保存数据源
     * 
     * @param id
     *            数据源组id
     * @return
     */
    @RequestMapping(value = "/{id}/datasources", method = { RequestMethod.POST })
    public ResponseResult saveDataSource(@PathVariable("id") String id,
            HttpServletRequest request) {
        String name = request.getParameter ("name");
        ResponseResult rs = new ResponseResult ();
        if (StringUtils.isEmpty (name) || name.length () > 255) {
            logger.debug ("name is empty or length more than 255");
            rs.setStatus (1);
            rs.setStatusInfo ("名称为空或者太长，请重新输入合法名称，长度不能超过255个字符");
            return rs;
        }
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("未找到数据源组定义,id:" + id);
                logger.warn ("未找到数据源组定义,id:" + id);
            } else {
                // 同一数据源组中的各个数据源要求名称唯一
                if (dsG.existName (name)) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("this datasource name is already exist in this group");
                    logger.warn ("this datasource name is already exist in this group");
                } else {
                    DataSourceDefine define = new DataSourceDefine ();
                    define.setId (UuidGeneratorUtils.generate ());
                    String productLine = dsG.getProductLine ();
                    assignNewValue (productLine, request, define);
                    assertPropertiesForDs (request, define);
                    
                    if (!dsService.isValidateConn(define, securityKey)) {
                        rs.setStatus (1);
                        rs.setStatusInfo ("无法连接数据源。请检查数据源是否正常或数据源信息是否正确。");
                        return rs;
                    }
                    
                    // 添加数据源到数据源组
                    boolean result = dsG.addDataSourceDefine (define);
                    // 如果该数据源组未设置使用数据源，则设置该数据源为当前使用数据源
                    if (result && dsG.getActiveDataSource () == null) {
                        dsG.setActiveDataSource (define);
                    }
                    // 保存数据源组
                    dsG = dsGroupService.saveOrUpdateDataSourceGroup (dsG,
                            securityKey);
                    rs.setStatus (0);
                    rs.setStatusInfo ("successfully");
                    rs.setData (define);
                    logger.info ("save data source successfully!");
                }
            }
        } catch (Exception e) {
            logger.debug (e.getMessage (), e);
            rs.setStatus (1);
            rs.setStatusInfo ("未能正确存储数据源定义信息，原因: " + e.getMessage ());
        }
        return rs;
    }

    /**
     * 设置数据源属性信息
     * @param request
     * @param define
     */
    private void assertPropertiesForDs(HttpServletRequest request, DataSourceDefine define) {
        String properties = request.getParameter ("advancedProperties");
        define.getProperties ().clear ();
        if (!StringUtils.isEmpty (properties)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> propertiesArray = AnswerCoreConstant.GSON.fromJson (properties, type);
            if (propertiesArray.size () > 0) {
                propertiesArray.forEach ((k, v) -> {
                    define.getProperties ().put (k, v);
                });
            }
        }
    }
    
    /**
     * 更新数据源组
     * 
     * @param id
     *            数据源组id
     * @param request
     *            http请求
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.POST })
    public ResponseResult updateDataSourceGroup(@PathVariable("id") String id,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("未能找到数据源组的相应定义 : " + id);
                logger.warn ("can not get datasource group by id : " + id);
            } else {
                // TODO 检查缓存
                String name = request.getParameter ("groupName");
                String productLine = ContextManager.getProductLine ();
                dsG.setName (name);
                dsG.setProductLine (productLine);
                dsGroupService.saveOrUpdateDataSourceGroup (dsG, securityKey);
                rs.setStatus (0);
                rs.setStatusInfo ("successfully");
                rs.setData (dsG);
                logger.info ("successfully update datasource group ");
            }
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error (e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 更新数据源
     * 
     * @param id
     *            数据源组id
     * @param productLine
     * @return
     */
    @RequestMapping(value = "/{id}/datasources/{subId}", method = {
            RequestMethod.PUT, RequestMethod.POST })
    public ResponseResult updateDataSource(@PathVariable("id") String id,
            @PathVariable("subId") String subId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("can't get datasource group with id: " + id);
                logger.error ("can't get datasource group with id: " + id);
            } else {
                DataSourceDefine define = dsGroupService.getDataSourceDefine (
                        id, subId);
                if (define == null) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("can't get datasource with id: " + subId);
                    logger.error ("can't get datasource with id: " + subId);
                } else {
                    // TODO 检查缓存
                    String productLine = ContextManager.getProductLine ();
                    String name = request.getParameter ("name");
                    if (dsG.existName (name)
                            && !name.equals (define.getName ())) {
                        rs.setStatus (1);
                        rs.setStatusInfo ("this datasource name is already exist in this group");
                        logger.warn ("this datasource name is already exist in this group");
                    } else {
                        assignNewValue (productLine, request, define);
                        assertPropertiesForDs (request, define);
                        define.setDbPwd (define.getDbPwd ());
                        dsG.addDataSourceDefine (define);
                        if (!dsService.isValidateConn(define, securityKey)) {
                            rs.setStatus (1);
                            rs.setStatusInfo ("无法连接数据源。请检查数据源是否正常或数据源信息是否正确。");
                            return rs;
                        }
                        // 更新活动数据源
                        if (dsG.getActiveDataSource () != null) {
                            if (dsG.getActiveDataSource ().getId ()
                                    .equals (subId)) {
                                dsG.setActiveDataSource (define);
                            }
                        }
                        dsGroupService.saveOrUpdateDataSourceGroup (dsG,
                                securityKey);
                        // Map<String, String> params = Maps.newHashMap();
                        // // modified by jiangyichao
                        // DataSourceConnectionService<?> dsConnService =
                        // DataSourceConnectionServiceFactory.
                        // getDataSourceConnectionServiceInstance(define.getDataSourceType());
                        // DataSourceInfo info =
                        // dsConnService.parseToDataSourceInfo(define,
                        // securityKey);
                        // params.put("dataSourceInfo",
                        // AnswerCoreConstant.GSON.toJson(info));
                        // HttpRequest.sendPost(ConfigInfoUtils.getServerAddress()
                        // + "datasource/update", params);
                        logger.info ("successfully update datasource with id "
                                + id);
                        rs.setStatus (0);
                        rs.setStatusInfo ("successfully");
                        define.setDbPwd (define.getDbPwd ());
                        rs.setData (define);
                    }
                }
            }
        } catch (Exception e) {
            logger.error (e.getMessage (), e);
            rs.setStatus (1);
            rs.setStatusInfo ("error : 数据更新出错");
        }
        return rs;
    }
    
    /**
     * 修改数据源定义信息
     * 
     * @param productLine
     * @param request
     * @param define
     */
    private void assignNewValue(String productLine, HttpServletRequest request,
            DataSourceDefine define) {
        define.setName (request.getParameter ("name"));
        define.setDbInstance (request.getParameter ("dbInstance"));
        define.setProductLine (productLine);
        define.setEncoding (request.getParameter ("encoding"));
        if (StringUtils.hasText (request.getParameter ("connUrl"))) {
            define.setHostAndPort (request.getParameter ("connUrl"));
        } else {
            define.setHostAndPort (request.getParameter ("hostAndPort"));
        }
        define.setDbUser (request.getParameter ("dbUser"));
        define.setDbPwd (request.getParameter ("dbPwd"));
        define.setDataSourceType (DataSourceType.valueOf (request
                .getParameter ("type")));
        // define.setDataSourceType(DataSourceType.valueOf("H2"));
    }
    
    /**
     * 删除数据源组
     * 
     * @param id
     *            数据源组id
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseResult removeDataSourceGroup(@PathVariable("id") String id) {
        ResponseResult rs = new ResponseResult ();
        boolean canDelete = true;
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG != null) {
                // 检验是否存在使用数据源组中活动数据源的报表
                DataSourceDefine activeDs = dsG.getActiveDataSource ();
                if (activeDs != null) {
                    List<String> refReport = reportDesignModelService
                            .lsReportWithDsId (dsG.getId ());
                    if (refReport != null && refReport.size () > 0) {
                        rs.setStatus (1);
                        rs.setStatusInfo ("数据源组[" + dsG.getName ()
                                + "]中的活动数据源[" + activeDs.getName ()
                                + "]正在被使用，请先删除引用该数据源的报表: "
                                + makeString (refReport));
                        canDelete = false;
                    }
                }
                // 如果缓存中不存在该数据源组，并且没有使用该数据源组下数据源的报表，则删除
                if (canDelete) {
                    // 删除数据源组
                    boolean result = dsGroupService.removeDataSourceGroup (id);
                    rs.setStatus (0);
                    rs.setStatusInfo (String.valueOf (result));
                    logger.info ("remove datasource group with id: " + id);
                }
            } else {
                rs.setStatus (1);
                rs.setStatusInfo ("can't get datasource group with id :" + id);
                logger.error ("can't get datasource group with id :" + id);
            }
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error (e.getMessage ());
        }
        return rs;
    }
    
    /**
     * 删除数据源
     * 
     * @param id
     * @param productLine
     * @return
     */
    @RequestMapping(value = "/{id}/datasources/{subId}", method = { RequestMethod.DELETE })
    public ResponseResult removeDataSource(@PathVariable("id") String id,
            @PathVariable("subId") String subId) {
        ResponseResult rs = new ResponseResult ();
        boolean canDelete = true;
        DataSourceGroupDefine dsG = null;
        try {
            // 如果cache中存在此数据源的id，或者报表目录中存在使用此数据源的报表，则不允许删除数据源
            if (nameCheckCacheManager.existsDSName (id)) {
                canDelete = false;
                rs.setStatus (1);
                rs.setStatusInfo ("数据源正在被使用，请先删除引用该数据源的报表 " + id);
                logger.warn ("the database with id " + id + " is using");
            } else {
                dsG = dsGroupService.getDataSourceGroupDefine (id);
                if (dsG == null) {
                    canDelete = false;
                    rs.setStatus (1);
                    rs.setStatusInfo ("can't get datasource group with id "
                            + id);
                    logger.warn ("can't get datasource group with id " + id);
                } else {
                    // 被删除的为活动数据源
                    DataSourceDefine activeDs = dsG.getActiveDataSource ();
                    if (activeDs != null && activeDs.getId ().equals (subId)) {
                        canDelete = false;
                        rs.setStatus (1);
                        rs.setStatusInfo ("the datasource to be delete is the active one, please check!");
                        logger.warn ("the datasource to be delete is the active one, please check!");
                    }
                }
            }
            
            // 如果可以被删除
            if (canDelete) {
                DataSourceDefine ds = dsGroupService.getDataSourceDefine (id,
                        subId);
                if (ds == null) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("can't get datasource with subId" + subId);
                    logger.warn ("can't get datasource with subId" + subId);
                } else {
                    boolean result = dsG.removeDataSourceDefine (ds);
                    dsG = dsGroupService.saveOrUpdateDataSourceGroup (dsG,
                            securityKey);
                    // Map<String, String> params = Maps.newHashMap();
                    // HttpRequest.sendPost(ConfigInfoUtils.getServerAddress() +
                    // "/datasource/destroy/" + id, params);
                    rs.setStatus (0);
                    rs.setStatusInfo (String.valueOf (result));
                    rs.setData (dsG);
                    logger.info ("remove datasource successuflly");
                }
            }
        } catch (DataSourceOperationException e) {
            rs.setStatus (1);
            rs.setStatusInfo ("删除数据出错");
            logger.error (e.getMessage (), e);
        }
        return rs;
    }
    
    /**
     * 修改活动数据源
     * 
     * @param id
     *            数据源组id
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/datasources/{subId}/changeActive", method = { RequestMethod.POST })
    public ResponseResult changeActiveDataSource(@PathVariable("id") String id,
            @PathVariable("subId") String subId) {
        ResponseResult rs = new ResponseResult ();
        try {
            DataSourceGroupDefine dsG = dsGroupService
                    .getDataSourceGroupDefine (id);
            if (dsG == null) {
                rs.setStatus (1);
                rs.setStatusInfo ("can't get datasource group with id " + id);
                logger.error ("can't get datasource group with id " + id);
            } else {
                Map<String, DataSourceDefine> dataSourceList = dsG
                        .getDataSourceList ();
                if (!dsService.isValidateConn(dataSourceList.get (subId), securityKey)) {
                    rs.setStatus (1);
                    rs.setStatusInfo ("无法连接数据源。请检查数据源是否正常或数据源信息是否正确。");
                    return rs;
                }
                if (dataSourceList != null
                        && dataSourceList.containsKey (subId)) {
                    dsG.setActiveDataSource (dataSourceList.get (subId));
                    dsGroupService.saveOrUpdateDataSourceGroup (dsG,
                            securityKey);
                } else {
                    rs.setStatus (1);
                    rs.setStatusInfo ("can't change active datasource with subId "
                            + id);
                    logger.error ("can't change active datasource with subId "
                            + id);
                }
            }
        } catch (Exception e) {
            rs.setStatus (1);
            rs.setStatusInfo (e.getMessage ());
            logger.error (e.getMessage ());
        }
        return rs;
    }
    
    private String makeString(List<String> refReport) {
        StringBuilder rs = new StringBuilder ();
        refReport.forEach (str -> rs.append (str + " "));
        return rs.toString ();
    }
}
