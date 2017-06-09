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
package com.baidu.rigel.biplatform.queryrouter.query.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.CallbackMember;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackConstants;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackDimTreeNode;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackResponse;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackServiceInvoker;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackType;
import com.baidu.rigel.biplatform.ac.model.callback.ResponseStatus;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.service.DimensionMemberService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * callback获取member的实现
 * 
 * @author xiaoming.chen
 *
 */
@Service(DimensionMemberService.CALLBACK_MEMBER_SERCICE)
public class CallbackDimensionMemberServiceImpl implements DimensionMemberService {

    /**
     * log
     */
    private Logger log = Logger.getLogger(this.getClass());

    /**
     * treeCallbackService TODO 这个后续修改，会变成从工厂获取
     */
//    private static CallbackServiceImpl treeCallbackService = new CallbackServiceImpl();

    @Override
    public List<MiniCubeMember> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo, Member parentMember,
            Map<String, String> params) throws MiniCubeQueryException, MetaException {
//        MetaDataService.checkCube(cube);
//        MetaDataService.checkDataSourceInfo(dataSourceInfo);
        CallbackLevel callbackLevel = (CallbackLevel) level;
        Map<String, String> callbackParams = Maps.newHashMap(callbackLevel.getCallbackParams());
        if (MapUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (callbackParams.containsKey(k) || callbackParams.containsKey(CallbackConstants.CB_NEED_SUMMARY)) {
                    callbackParams.put(k, v);
                }
            }); 
            callbackParams.put (HttpRequest.COOKIE_PARAM_NAME, params.get (HttpRequest.COOKIE_PARAM_NAME));
        }
        CallbackResponse response = 
                CallbackServiceInvoker.invokeCallback(callbackLevel.getCallbackUrl(), 
                callbackParams, CallbackType.DIM);
        if (response.getStatus() == ResponseStatus.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<CallbackDimTreeNode> posTree = (List<CallbackDimTreeNode>) response.getData();
            List<MiniCubeMember> result = createMembersByPosTreeNode(posTree, level, null);
            if (parentMember == null) {
                // 如果根节点的children有值，那么,目前cb有两个类型，一个为扁平结构，一个问题汇总树节点，
                if (CallbackConstants.CB_NEED_SUMMARY_FALSE.equals(params
                        .get(CallbackConstants.CB_NEED_SUMMARY))
                        && CollectionUtils.isNotEmpty(result.get(0).getChildren())) {
                    // 如果CB_NEED_SUMMARY为空，返回扁平结构。
                    List<MiniCubeMember> resultList = Lists.newArrayList();
                    for (Member Member : result.get(0).getChildren()) {
                        resultList.add((MiniCubeMember) Member);
                    }
                    return resultList;
                } else if (CallbackConstants.CB_NEED_SUMMARY_FALSE.equals(params
                        .get(CallbackConstants.CB_NEED_SUMMARY))
                        && (result.size() == 1 && result.get(0).getCaption().indexOf("全部") == 0)) {
                    // 此种情况为临时方案，就是判断如果为size为1，caption为全部开头的抓取children
                    return Lists.newArrayList();
                } else if (CollectionUtils.isEmpty(result.get(0).getChildren())
                        && result.size() > 1 
                        && CallbackConstants.CB_NEED_SUMMARY_TRUE
                        .equals(params.get(CallbackConstants.CB_NEED_SUMMARY))) {
                    // 如果CB_NEED_SUMMARY有值，返回带汇总节点的结构。
                    CallbackMember member = new CallbackMember(MetaNameUtil.SUMMARY_MEMBER_NAME_PRE
                            + level.getName());
                    member.setLevel(level);
                    member.setCaption(CallbackMember.SUMMARY_NODE_CAPTION);
                    member.setHasChildren(true);
                    member.generateUniqueName(null);
                    member.setParent(parentMember);
                    member.setChildren(result);
                    List<MiniCubeMember> miniCubeMemberList = Lists.newArrayList();
                    miniCubeMemberList.add(member);
                    return miniCubeMemberList;
                } else {
                    // 如果没有显式设置CB_NEED_SUMMARY，走cb默认返回值结构，
                    return result;
                }
            } else {
                return createMembersByPosTreeNode(posTree.get(0).getChildren(), level, null);
            }
        } else {
            log.error("[ERROR] --- --- " + response.getStatus() + "---" + response.getMessage());
            // 错误请求，直接提示出错
            throw new RuntimeException(response.getMessage());
        }
        
    }

    /**
     * 
     * @param posTree
     * @param level
     * @param parent
     * @return
     */
    private List<MiniCubeMember> createMembersByPosTreeNode(List<CallbackDimTreeNode> posTree, Level level,
            MiniCubeMember parent) {
        List<MiniCubeMember> members = new ArrayList<MiniCubeMember>();
        if (CollectionUtils.isNotEmpty(posTree)) {
            for (CallbackDimTreeNode node : posTree) {
                MiniCubeMember member = createMemberByPosTreeNode(node, level, parent);
                member.setChildren(createMembersByPosTreeNode(node.getChildren(), level, member));
                members.add(member);
            }
        }
        return members;
    }

//    /**
//     * 根据Callback的level获取Callback
//     * 
//     * @param level CallbackLevel
//     * @param params 参数信息
//     * @return Callback返回结果
//     * @throws IOException Http请求异常
//     */
//    private List<CallBackTreeNode> fetchCallBack(Level level, Map<String, String> params) throws MiniCubeQueryException {
//        if (level == null || !level.getType().equals(LevelType.CALL_BACK)) {
//            throw new IllegalArgumentException("level type must be call back:" + level);
//        }
//        CallbackLevel callbackLevel = (CallbackLevel) level;
//        if (StringUtils.isBlank(callbackLevel.getCallbackUrl())) {
//            throw new IllegalArgumentException("callback url can not be empty:" + callbackLevel);
//        }
//        Map<String, String> callbackParams = Maps.newHashMap(callbackLevel.getCallbackParams());
//        if (MapUtils.isNotEmpty(params)) {
//            callbackParams.putAll(params);
//        }
//
//        // 默认设置只取本身和本身的孩子节点
//        List<CallBackTreeNode> result;
//        try {
//            result = treeCallbackService.fetchCallback(callbackLevel.getCallbackUrl(), callbackParams);
//            return result;
//        } catch (IOException e) {
//            log.error("fetch callback error,url:" + callbackLevel.getCallbackUrl() + " params:" + callbackParams, e);
//            throw new MiniCubeQueryException(e);
//        }
//    }

    @Override
    public MiniCubeMember getMemberFromLevelByName(DataSourceInfo dataSourceInfo, Cube cube, Level level, String name,
            MiniCubeMember parent, Map<String, String> params) throws MiniCubeQueryException, MetaException {
//        MetaDataService.checkCube(cube);
//        MetaDataService.checkDataSourceInfo(dataSourceInfo);
//        List<CallBackTreeNode> posTree = fetchCallBack(level, params);
        CallbackLevel callbackLevel = (CallbackLevel) level;
        Map<String, String> callbackParams = Maps.newHashMap(callbackLevel.getCallbackParams());
        if (MapUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (callbackParams.containsKey(k)) {
                    callbackParams.put(k, v);
                }
            }); 
//            callbackParams.putAll(params);
        }
        callbackParams.put(HttpRequest.COOKIE_PARAM_NAME, params.get(HttpRequest.COOKIE_PARAM_NAME));
        CallbackResponse response = 
                CallbackServiceInvoker.invokeCallback(callbackLevel.getCallbackUrl(), 
                callbackParams, CallbackType.DIM);
        if (response.getStatus() == ResponseStatus.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<CallbackDimTreeNode> posTree = (List<CallbackDimTreeNode>) response.getData();
            if (posTree.size() != 1) {
                CallbackDimTreeNode node = findTreeNodeByName(posTree, name);
                return createMemberByPosTreeNode(node, level, null);
            }
            // TODO 
            
            return createMemberByPosTreeNode(posTree.get(0), level, null);
            
        } else {
            throw new RuntimeException(response.getMessage());
        }
    }

    private CallbackDimTreeNode findTreeNodeByName(List<CallbackDimTreeNode> posTree, String name) {
        for (CallbackDimTreeNode node : posTree) {
            if (node.getId().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * @param node
     * @param level
     * @return
     */
    private MiniCubeMember createMemberByPosTreeNode(CallbackDimTreeNode node, Level level, Member parentMember) {
        CallbackMember result = new CallbackMember(node.getId());
        result.setLevel(level);
        result.setCaption(node.getName());
        result.setHasChildren (node.isHasChildern ());
        if (CollectionUtils.isNotEmpty(node.getCsIds())) {
            result.setQueryNodes(Sets.newHashSet(node.getCsIds()));
        }
        // 先生成一下uniqueName，避免后续生成带上了父节点的UniqueName
        result.generateUniqueName(null);
        result.setParent(parentMember);
//        if (CollectionUtils.isNotEmpty (node.getChildren ())) {
//            Set<String> leafIds = Sets.newHashSet ();
//            node.getChildren ().forEach (n -> {
//                leafIds.add (n.getId ());
//            });
//            result.setQueryNodes (leafIds);
//        }
        return result;
    }

    @Override
    public List<MiniCubeMember> getMemberFromLevelByNames(
            DataSourceInfo dataSourceInfo, Cube cube, Level level,
            Map<String, String> params, List<String> uniqueNameList) {
        throw new UnsupportedOperationException ();
    }

}
