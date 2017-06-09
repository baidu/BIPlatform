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
package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.TimerAreaLogicModel;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.baidu.rigel.biplatform.ma.resource.view.vo.ExtendAreaViewObject;
import com.baidu.rigel.biplatform.ma.resource.view.vo.ItemViewObject;
import com.google.common.collect.Lists;

/**
 * REST工具类
 * 
 * @author zhongyi
 *
 *         
 */
public final class ResourceUtils {
    
    /**
     * ResourceUtils
     */
    private ResourceUtils() {
        
    }
    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param errorMessage
     * @param data
     * @return
     */
    public static ResponseResult getResult(String successMessage, String errorMessage, Object data) {
        ResponseResult rs = new ResponseResult();
        if (data == null) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo(errorMessage);
        } else {
            rs.setStatus(ResponseResult.SUCCESS);
            rs.setStatusInfo(successMessage);
            rs.setData(data);
        }
        return rs;
    }
    
    /**
     * 构建返回结果
     * 
     * @param errorMessage
     * @param errorCode
     * @return
     */
    public static ResponseResult getErrorResult(String errorMessage, int errorCode) {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(errorCode);
        rs.setStatusInfo(errorMessage);
        return rs;
    }
    
    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param data
     * @return
     */
    public static ResponseResult getCorrectResult(String successMessage, Object data) {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(ResponseResult.SUCCESS);
        rs.setStatusInfo(successMessage);
        rs.setData(data);
        return rs;
    }
    
    /**
     * 构建扩展区ValueObject
     * @param model 
     * @param area
     * @return
     */
    public static ExtendAreaViewObject buildValueObject(ReportDesignModel model, ExtendArea area) {
        
        if (area == null) {
            return new ExtendAreaViewObject();
        }
        ExtendAreaViewObject rs = new ExtendAreaViewObject();
        String cubeId = area.getCubeId();
        if (!StringUtils.hasText(cubeId)) {
            return new ExtendAreaViewObject();
        }
        Item[] yAxis = area.getLogicModel().getColumns();
        rs.setyAxis(buildItemViewObject(model.getSchema(), cubeId, yAxis, null));
        Item[] xAxis = area.getLogicModel().getRows();
        rs.setxAxis(buildItemViewObject(model.getSchema(), cubeId, xAxis, null));
        Item[] sAxis = area.getLogicModel().getSlices();
        rs.setsAxis(buildItemViewObject(model.getSchema(), cubeId, sAxis, null));
        if (area.getType() == ExtendAreaType.TIME_COMP) {
            Map<Item, TimerAreaLogicModel.TimeRange> timeItem = ((TimerAreaLogicModel) area.getLogicModel())
                    .getTimeDimensions();
            rs.setxAxis(buildItemViewObject(model.getSchema(), cubeId,
                    timeItem.keySet().toArray(new Item[0]), null));
        }
        final Schema schema = model.getSchema();
        if (area.getType() == ExtendAreaType.LITEOLAP) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) area;
            Set<String> usedItemOlapIdSet = area.listAllItems().keySet();
            Item[] candDims = liteOlapArea.getCandDims().values().toArray(new Item[0]);
            rs.setCandDims(buildItemViewObject(schema, cubeId, candDims, usedItemOlapIdSet));
            Item[] candInds = liteOlapArea.getCandInds().values().toArray(new Item[0]);
            rs.setCandInds(buildItemViewObject(schema, cubeId, candInds, usedItemOlapIdSet));
        } else {
            final Item[] canDims = area.getLogicModel().getSelectionDims().values().toArray(new Item[0]);
            rs.setCandDims(buildItemViewObject(schema, cubeId, canDims, null));
            Item[] candInds = area.getLogicModel().getSelectionMeasures().values().toArray(new Item[0]);
            rs.setCandInds(buildItemViewObject(schema, cubeId, candInds, null));
        }
        return rs;
    }

    /**
     * 构建item value object
     * @param schema
     * @param cubeId
     * @param axisItem
     * @return
     */
    private static List<ItemViewObject> buildItemViewObject(Schema schema,
            String cubeId, Item[] axisItem, Set<String> usedItemOlapIdSet) {
        if (axisItem == null || axisItem.length == 0) {
            return Lists.newArrayList();
        }
        List<ItemViewObject> rs = Lists.newArrayList();
        for (Item item : axisItem) {
            ItemViewObject obj = new ItemViewObject();
            obj.setCubeId(cubeId);
            obj.setId(item.getId());
            obj.setOlapElementId(item.getOlapElementId());
            obj.setChartType((String) item.getParams().get("chartType"));
            if (usedItemOlapIdSet != null) {
                obj.setUsed(usedItemOlapIdSet.contains(item.getOlapElementId()));
            }
            OlapElement olapElement = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    cubeId, item.getOlapElementId());
            if (olapElement != null) {
                obj.setCaption(olapElement.getCaption());
                obj.setName(olapElement.getName());
                if (olapElement instanceof Dimension) {
                    obj.setDimGroup(((Dimension) olapElement).getType() == DimensionType.GROUP_DIMENSION);
                }
                rs.add(obj);
            }
        }
        return rs;
    }
    
    /**
     * 从request请求对象中去取制定的cookie信息
     * 
     * @param request request
     * @param cookieName cookieName
     * @return 返回取到的cookie信息
     */
    public static String getCookieValueFromRequest(HttpServletRequest request, String cookieName) {
        if (!StringUtils.hasLength(cookieName)) {
            throw new IllegalArgumentException("the cookieName can not be empty !");
        }
        Cookie[] cookies = request.getCookies();
        String result = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie != null && cookieName.equals(cookie.getName())) {
                    result = cookie.getValue();
                    break;
                }
            }
        }
        return result;
    }

}