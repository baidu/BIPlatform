
/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.regular.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.comm.util.ParamValidateUtils;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportParam;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportSettingService;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 固定报表设置信息服务实现类
 * 
 * @author yichao.jiang
 * @version 2015年8月5日
 * @since jdk 1.8 or after
 */
@Service("regularReportSettingService")
public class RegularReportSettingServiceImpl implements RegularReportSettingService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportSettingServiceImpl.class);

    /**
     * id
     */
    private static final String ID = "id";

    /**
     * value
     */
    private static final String VALUE = "value";

    /**
     * 报表查询服务
     */
    @Resource
    private ReportModelQueryService reportModelQueryService;
    
    /**
     * 报表设计服务
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;
    
    /**
     * 密钥
     */
    @Value("${biplatform.ma.ser_key}")
    private String securityKey;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel saveRegularReportSetting(String reportId, RegularReportTaskInfo taskBo) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        if (model == null) {
            LOG.error("can't get report model with report id: " + reportId);
            return model;
        }
        if (taskBo == null) {
            return model;
        }
        model.getRegularTasks().put(taskBo.getTaskId(), taskBo);
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegularReportTaskInfo getRegularReportSetting(String reportId, String taskId) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        if (model == null) {
            LOG.error("can't get report model with report id: " + reportId);
            return null;
        }
        return model.getRegularReportTaskInfo(taskId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegularReportTaskInfo> getRegularReportSetting(String reportId) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        List<RegularReportTaskInfo> tasks = Lists.newArrayList();
        if (model == null) {
            LOG.error("can't get report model with report id: " + reportId);
            return tasks;
        }
        Map<String, RegularReportTaskInfo> regularTasks = model.getRegularTasks();
        regularTasks.forEach((k, v) -> {
            tasks.add(v);
        });
        return tasks;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RegularReportParam> getAllParamsOfReportModel(String reportId) {
        Set<RegularReportParam> allParams = Sets.newHashSet();
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        if (model == null) {
            LOG.error("can't get report model with report id: " + reportId);
            return allParams;
        }
        // 获取报表对应的cube信息
        MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        Map<String, Dimension> dims = cube.getDimensions();
        // TODO 先取报表中的参数纬度P设置的参数信息
        Map<String, ReportParam> reportParams = model.getParams();
        reportParams.forEach((k, v) -> {
            // 排除时间维度
            if (dims != null && dims.size() != 0 && 
                    !this.isTimeDimension(dims.get(v.getElementId()))) {
                RegularReportParam param = new RegularReportParam();
                param.setParamId(v.getElementId());
                param.setParamName(v.getName());
                param.setCaption(this.getCaptionOfOlapElement(model, v.getElementId()));
                Map<String, Object> paramValue = Maps.newHashMap();
                paramValue.put(ID, Lists.newArrayList());
                paramValue.put(VALUE, Lists.newArrayList());
                param.setParamValue(paramValue);
                allParams.add(param);                
            }
        });

        // 获取平面表中的条件参数信息
        Map<String, PlaneTableCondition> planeTableConditions = model.getPlaneTableConditions();
        planeTableConditions.forEach((k, v) -> {
            // 排除时间维度和指标
            if (dims != null && dims.size() != 0 && 
                    dims.containsKey(v.getElementId()) && !this.isTimeDimension(dims.get(v.getElementId()))) {
                RegularReportParam param = new RegularReportParam();
                param.setParamId(v.getElementId());
                param.setParamName(v.getName());
                param.setCaption(this.getCaptionOfOlapElement(model, v.getElementId()));
                Map<String, Object> paramValue = Maps.newHashMap();
                paramValue.put(ID, Lists.newArrayList());
                paramValue.put(VALUE, Lists.newArrayList());
                param.setParamValue(paramValue);
                allParams.add(param);
            }
        });
        return allParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RegularReportParam> getAllParamsOfReguarTask(String reportId, String taskId) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        Set<RegularReportParam> allParams = Sets.newHashSet();
        if (model == null || StringUtils.isEmpty(taskId)) {
            return allParams;
        }
        RegularReportTaskInfo task = model.getRegularReportTaskInfo(taskId);
        if (task== null) {
            return allParams;
        }
        return task.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAuthoritysOfRegularReport(String reportId, String taskId) {
        ReportDesignModel reportModel = this.getDesignModelAccordingReportId(reportId);
        // 权限信息
        List<String> authoritys = Lists.newArrayList();
        if (reportModel == null) {
            return authoritys;
        }
        // 获取该报表中设置的所有参数信息
        Set<RegularReportParam> allParams = this.getAllParamsOfReguarTask(reportId, taskId);
        if (allParams == null || allParams.size() == 0) {
            return authoritys;
        }
        // 获取该taskId对应的
        RegularReportTaskInfo taskBo = this.getRegularReportSetting(reportId, taskId);
        // 固定报表中的参数信息
        Set<RegularReportParam> taskParams = taskBo.getParams();
        // 获取固定参数中的callback参数信息
        RegularReportParam callbackParam = this.getParamOfCallback(reportModel, taskParams);
        if (callbackParam == null) {
            return authoritys;
        }
        // 获取callback参数值
        Map<String, Object> callbackValue = callbackParam.getParamValue();
        String id = (String) callbackValue.get(ID);
//        String value = (String) callbackValue.get(VALUE);
        // 数值为多个岗位
        if (((String) id).contains(",")) {
            return Lists.newArrayList(((String) id).split(","));
        } else {
            // 仅一个岗位
            return Lists.newArrayList((String) id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> getParamTreeOfRegularReport(String reportId, String taskId, String paramId, String id,
            String name, Map<String, String> params) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        if (model == null) {
            LOG.error("can't get report model with report id: " + reportId);
            return Lists.newArrayList();
        }
        // 获取固定报表的参数信息
        Set<RegularReportParam> regularParam = this.getAllParamsOfReguarTask(reportId, taskId);
        
        // 包含树上id的信息
        Set<String> setString = this.covSetParams(regularParam, paramId);
        // 获取报表对应的cube信息
        MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        Map<String, Dimension> dimensions = cube.getDimensions();
        if (dimensions == null || dimensions.size() == 0 || !dimensions.containsKey(paramId)) {
            return Lists.newArrayList();
        }
        // 根据参数id获取参数名称
        String paramName = this.getParamNameOfReport(paramId, model);
        // 获取维度信息
        Dimension dim = dimensions.get(paramId);
        // 如果是callback维度
        boolean isRoot = false;
        if (this.isCallback(dim)) {
            Level level = dim.getLevels().values().toArray(new Level[0])[0];
            CallbackLevel callbackLevel = (CallbackLevel) level;
            Map<String, String> callbackParams = callbackLevel.getCallbackParams();
            if (StringUtils.isEmpty(id)) {
                id = callbackParams.get(paramName);
                name = callbackParams.get(paramName);
                params.putAll(callbackParams);
                isRoot = true;
            }            
            params = this.resetParamsOfRegularReport(params, paramId, paramName, id, name, true);
        } else {
            params = this.resetParamsOfRegularReport(params, paramId, paramName, id, name, false);
            if (StringUtils.isEmpty(name)) {
                isRoot = true;
            }
        }
       
        // 返回对应的树的信息
        return this.handle4ParamTree(cube, paramId, params, setString, isRoot);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTaskNameExists(String reportId, String taskName) {
        ReportDesignModel model = this.getDesignModelAccordingReportId(reportId);
        if (model == null) {
            return false;
        }
        Map<String, RegularReportTaskInfo> tasks = model.getRegularTasks();
        if (tasks == null || tasks.size() == 0) {
            return false;
        }
        for (RegularReportTaskInfo task : tasks.values()) {
            if (task.getTaskName().equalsIgnoreCase(taskName.trim())) {
                return true;
            }
        }
        return false;
        
    }

    /**
     * 转换处理固定报表参数
     * @param params
     * @return
     */
    private Set<String> covSetParams(Set<RegularReportParam> params, String paramId) {
        Set<String> newParams = Sets.newHashSet();
        params.forEach(v -> {
            if (v.getParamId().equals(paramId)) {
                Map<String, Object> tmpMap = v.getParamValue();
                Object idObj = tmpMap.get("id");
                if (idObj instanceof String) {
                    String ids = idObj.toString();
                    if (ids.contains(",")) {
                        String[] id = ids.split(",");
                        for (String tmpId : id) {
                            newParams.add(tmpId);
                        }
                    } else {
                        newParams.add(ids);
                    }
                }
            }
        });
        return newParams;
    }
    
    /**
     * 获取报表中的callback参数
     * @param reportDesignModel
     * @param params
     * @return
     */
    private RegularReportParam getParamOfCallback(ReportDesignModel reportDesignModel,
            Set<RegularReportParam> params) {
        // 获取报表对应的cube信息
        MiniCube cube = reportDesignModel.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        // 获取cube中的纬度信息
        Map<String, Dimension> dimensions = cube.getDimensions();
        if (dimensions == null || dimensions.size() == 0) {
            return null;
        }

        // 参数遍历
        for (RegularReportParam param : params) {
            // 先对报表维度参数进行处理
            if (reportDesignModel.getParams() != null && reportDesignModel.getParams().size() != 0) {
                Object[] reportParams =
                        reportDesignModel.getParams().values()
                                .stream().filter(value -> {
                                    // 遍历，找到与param名称和id一样，并且是callback的参数
                                    if (value.getName().equals(param.getParamName())
                                            && value.getElementId().equals(param.getParamId())) {
                                        Dimension dim = cube.getDimensions().get(value.getElementId());
                                        return this.isCallback(dim);
                                    }
                                    return false;
                                }).toArray();
                // 对结果进行判断
                if (reportParams != null && reportParams.length != 0) {
                    return param;
                }
            }

            // 对平面表参数进行处理
            if (reportDesignModel.getPlaneTableConditions() != null
                    && reportDesignModel.getPlaneTableConditions().size() != 0) {
                Object[] planeTableConditions =
                        reportDesignModel.getPlaneTableConditions().values().stream().filter(value -> {
                            // 遍历，找到与param名称和id一样，并且是callback的参数
                            if (value.getName().equals(param.getParamName())
                                    && value.getElementId().equals(param.getParamId())) {
                                    Dimension dim = cube.getDimensions().get(value.getElementId());
                                    return this.isCallback(dim);
                                }
                                return false;
                            }).toArray();
                // 对结果判断
                if (planeTableConditions != null && planeTableConditions.length != 0) {
                    return param;
                }
            }
        }
        return null;
    }

    /**
     * 获取某个元素的caption信息
     * @param model
     * @param elementId
     * @return
     */
    private String getCaptionOfOlapElement(ReportDesignModel model, String elementId) {
        // 获取报表对应的cube信息
        MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        // 获取对应elementId的信息
        OlapElement element =
                ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), cube.getId(), elementId);
        if (element == null) {
            return null;
        }
        // 返回元素的caption信息
        return element.getCaption();
    }

    /**
     * 判断某个维度是否为时间维度
     * @param dim
     * @return
     */
    private boolean isTimeDimension(Dimension dim) {
        return dim != null && dim.getType() == DimensionType.TIME_DIMENSION;
    }
    
    /**
     * 判断某个纬度是否为callback纬度
     * @param dim
     * @return
     */
    private boolean isCallback(Dimension dim) {
        if (dim == null) {
            return false;
        }
        if (dim.getLevels() != null && dim.getLevels().size() != 0) {
            Level level = dim.getLevels().values().toArray(new Level[0])[0];
            return this.isCallbackLevel(level);            
        }
        return false;
    }

    /**
     * 判断某个level是否为callback isCallbackLevel
     * @param level
     * @return
     */
    private  boolean isCallbackLevel(Level level) {
        return level != null && level.getType() == LevelType.CALL_BACK;
    }
    
    /**
     * 根据报表id获取报表模型
     * @param reportId
     * @return
     */
    private ReportDesignModel getDesignModelAccordingReportId(String reportId) {
        // 如果从已发布中找不到，则从未发布中找 
        ReportDesignModel model = reportDesignModelService.getModelByIdOrName(reportId, false);
        if (model == null) {
            // 如果缓存中取不到则从已发布文件中取
            model = reportDesignModelService.getModelByIdOrName(reportId, true);
        }
        return model;
    }
    
    /**
     * 处理参数树问题
     * @param cube
     * @param uniqueName
     * @param params
     * @param setOfTree 树上已经选中的节点
     * @return
     */
    private List<Map<String, Object>> handle4ParamTree(Cube cube, String paramId, Map<String, String> params,
            Set<String> setOfTree, boolean isRoot) {
        if (!ParamValidateUtils.check("cube", cube)) {
            return null;
        }
        Dimension dim = cube.getDimensions().get(paramId);
        if (dim != null) {
            List<Map<String, Object>> values;
            try {
                Cube tmpCube = QueryUtils.transformCube(cube);
                values = Lists.newArrayList();
                List<Member> members =
                        reportModelQueryService.getMembers(tmpCube, tmpCube.getDimensions().get(dim.getName()), params,
                                securityKey).get(0);
                members.forEach(m -> {
                    Map<String, Object> tmp = Maps.newHashMap();
                    tmp.put("id", m.getUniqueName());
                    tmp.put("name", m.getCaption());
                    if (!CollectionUtils.isEmpty(((MiniCubeMember) m).getChildren())
                            || ((MiniCubeMember) m).getQueryNodes().size() > 1) {
                        tmp.put("isParent", true);
                    } else {
                        tmp.put("isParent", false);
                    }
                    if (setOfTree != null && setOfTree.size() != 0 && setOfTree.contains(m.getUniqueName())) {
                        tmp.put("checked", true);
                    } else {
                        tmp.put("checked", false);
                    }
                    MiniCubeMember realMember = (MiniCubeMember) m;
                    if (isRoot) {
                        values.add(tmp);
                    }
                    if (!isRoot) {
                        List<Map<String, Object>> children =
                                getChildren(realMember, realMember.getChildren(), setOfTree);
                        if (children != null && !children.isEmpty()) {
                            values.addAll(children);
                        }
                    }
                });
                
                return values;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } 
        return null;
    }
    
    /**
     * 处理孩子节点
     * @param parent
     * @param children
     * @param setOfTree 树上已经选中的节点
     * @return
     */
    private List<Map<String, Object>> getChildren(Member parent, List<Member> children, Set<String> setOfTree) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> rs = Lists.newArrayList();
        MiniCubeMember tmp = null;
        for (Member m : children) {
            tmp = (MiniCubeMember) m;
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", tmp.getUniqueName());
            map.put("name", tmp.getCaption());
//            map.put("isParent", parent.getUniqueName());
            if (setOfTree != null && setOfTree.size() != 0 && setOfTree.contains(m.getUniqueName())) {
                map.put("checked", true);
            } else {
                map.put("checked", false);
            }
            if (!CollectionUtils.isEmpty(((MiniCubeMember) m).getChildren()) ||
                    ((MiniCubeMember) m).getQueryNodes().size() > 1) {
                map.put("isParent", true);
            } else {
               map.put("isParent", false); 
            }
            rs.add(map);
            if (!CollectionUtils.isEmpty(tmp.getChildren())) {
                rs.addAll(getChildren(tmp, tmp.getChildren(), setOfTree));
            }
        }
        return rs;
    }
    
    /**
     * 根据用户点击，重置查询参数
     * @param params 原有参数
     * @param paramId 参数id
     * @param paramName 参数名称
     * @param id 当前节点id
     * @param name 当前节点的name
     * @param isCallback 当前参数是否为callback维度
     * @return
     */
    private Map<String, String> resetParamsOfRegularReport(Map<String, String> params,
            String paramId, String paramName, String id, String name, boolean isCallback) {
        Map<String, String> newParams = Maps.newHashMap();
        params.forEach((k, v) -> {
            // 对已经存在的参数进行替换
            if (!StringUtils.isEmpty(paramId) && !StringUtils.isEmpty(paramName) && 
                    (k.equals(paramId) || k.equals(paramName))) {
                // 如果是callback维度，则替换为id
                if (isCallback) {
                    StringBuilder newValue = new StringBuilder();
                    // 如果是数组
                    if (!StringUtils.isEmpty(id) && id.contains(",")) {
                        String[] tmpIds = id.split(",");
                        for (String tmpId : tmpIds) {
                            String[] tmp = MetaNameUtil.parseUnique2NameArray(tmpId);
                            newValue.append(tmp[tmp.length - 1] + ",");
                        }
                        // 替换最后一个,
                        newValue.replace(newValue.length() - 2, newValue.length() - 1, "");
                    } else {
                        // 非数组
                        if (!StringUtils.isEmpty(id) && MetaNameUtil.isUniqueName(id)) {
                            String[] tmp = MetaNameUtil.parseUnique2NameArray(id);
                            newValue.append(tmp[tmp.length - 1]);                            
                        } else {
                            newValue.append(id);
                        }
                    }
                    newParams.put(k, newValue.toString());
                } else {
                    // 如果是其他维度，则替换为name
                    newParams.put(k, name);
                }
            } else {
                newParams.put(k, v);
            }
        });
        return newParams;
    }
    
    /**
     * 获取某个参数id对应的参数名称
     * @param paramId
     * @param model
     * @return
     */
    private String getParamNameOfReport(String paramId, ReportDesignModel model) {
        // 维度参数
        Map<String, ReportParam> reportParams = model.getParams();
        Map<String, String> newReportParams = Maps.newHashMap();
        // 封装新的参数
        reportParams.forEach((k, v) -> {
            newReportParams.put(v.getName(), v.getElementId());
            newReportParams.put(v.getElementId(), v.getName());
        });
        
        // 平面表条件参数
        Map<String, PlaneTableCondition> planeTableConditions = model.getPlaneTableConditions();
        Map<String, String> newPlaneConditions = Maps.newHashMap();
        planeTableConditions.forEach((k, v) -> {
            newPlaneConditions.put(v.getElementId(), v.getName());
            newPlaneConditions.put(v.getName(), v.getElementId());
        });
        if (newReportParams != null && newReportParams.size() != 0 && newReportParams.containsKey(paramId)) {
            
            // 如果维度参数和平面表条件中均有，则以平面表参数为主
            if (newPlaneConditions != null && newPlaneConditions.size() != 0 && 
                    newPlaneConditions.containsKey(paramId)) {
                return newPlaneConditions.get(paramId);
            } else {
                return newReportParams.get(paramId);                
            }
        }
        if (newPlaneConditions != null && newPlaneConditions.size() != 0 && 
                newPlaneConditions.containsKey(paramId)) {
            return newPlaneConditions.get(paramId);
        }
        return null;
    }
}
