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
package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 * 
 * 报表模型操作工具类，提供操作报表模型的通用方法
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public final class ReportDesignModelUtils {
    
    /**
     * 日志记录工具
     */
    private static Logger logger = LoggerFactory.getLogger(ReportDesignModelUtils.class);
    
    /**
     * 
     * ReportDesignModelUtils
     */
    private ReportDesignModelUtils() {
        
    }
    
    /**
     * 依据报表定义模型构建MiniCube
     * 
     * @param model
     * @return 立方体定义信息
     */
    public static List<Cube> getCubes(ReportDesignModel model) {
        if (model == null) {
            logger.error("model is null");
            return new ArrayList<Cube>();
        }
        ExtendArea[] extendAreaList = model.getExtendAreaList();
        if (extendAreaList == null || extendAreaList.length == 0) {
//            logger.error("can not get extendarea from current model : {}", model);
            return new ArrayList<Cube>();
        }
        List<Cube> cubes = new ArrayList<Cube>();
        for (ExtendArea area : extendAreaList) {
            if (area == null || area.getLogicModel() == null) {
                continue;
            }
            Cube cube = buileCube(model, area);
            if (cube != null) {
                cubes.add(cube);
            }
        }
        return Collections.unmodifiableList(cubes);
    }
    
    /**
     * 通过扩展区域定义的逻辑模型生成立方体 通常扩展区域与逻辑模型之间对因关系为一对一
     * 
     * @param model
     *            报表模型
     * @param area
     *            扩展区域
     * @return 立方体定义
     */
    private static Cube buileCube(ReportDesignModel model, ExtendArea area) {
        LogicModel logicModel = area.getLogicModel();
        Item[] items = logicModel.getItems();
        if (items == null || items.length == 0) {
//            logger.error("can not get reference items from currrent area : {}", area);
            return null;
        }
        Schema schema = model.getSchema();
        if (schema == null) {
//            logger.error("can not get schema define from report model : {}", model);
            return null;
        }
        
        if (schema.getCubes() == null) {
//            logger.error("can not get cubes from schema : {}", schema);
            return null;
        }
        
        Cube tmp = schema.getCubes().get(area.getCubeId());
        if (tmp == null) {
//            logger.error("can not get cube define from schema {} with id [{}]", schema,
//                    area.getCubeId());
            return null;
        }
        MiniCube cube = new MiniCube();
        cube.setId(tmp.getId());
        cube.setName(tmp.getName());
        cube.setCaption(tmp.getCaption());
        Map<String, Measure> measures = new HashMap<String, Measure>();
        Map<String, Dimension> dimensions = new HashMap<String, Dimension>();
        for (Item item : items) {
            String id = item.getOlapElementId();
            if (tmp.getDimensions() != null) {
                Dimension dim = tmp.getDimensions().get(id);
                if (dim != null) {
                    dimensions.put(dim.getId(), dim);
                }
                continue;
            } else {
                logger.info("current cube [{}] dimensions is empty.", tmp);
            }
            
            if (tmp.getMeasures() != null) {
                if (tmp.getMeasures().get(id) != null) {
                    Measure m = tmp.getMeasures().get(id);
                    measures.put(m.getId(), m);
                    continue;
                }
            } else {
                logger.info("current cube [{}] measures is empty.", tmp);
            }
        }
        if (measures.size() == 0 && dimensions.size() == 0) {
            logger.info("current cube measures and dimensions is empty");
            return null;
        }
        cube.setMeasures(measures);
        cube.setDimensions(dimensions);
        logger.info("mini cube define is : {}", cube);
        return cube;
    }
    
    /**
     * 依据id获取指标或者维度定义
     * @param dimOrIndId
     * @param schema
     * @param cubeId
     * @return 存在返回定义，否则返回null
     */
    public static OlapElement getDimOrIndDefineWithId(Schema schema, 
        String cubeId, final String dimOrIndId) {
        if (schema == null) {
            logger.info("schema is empty");
            return null;
        }
        
        if (schema.getCubes() == null) {
            logger.info("schema's cubes is empty");
            return null;
        }
        
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
//            logger.info("can not get cube with id [{}] from schema [{}]", cubeId, schema);
            return null;
        }
        
        if (cube.getDimensions() != null) {
//            OlapElement dim = cube.getDimensions().get(dimOrIndId);
            Object[] tmp =  cube.getDimensions().values().stream().filter (dim -> dim != null).filter(dim -> {
                    return dimOrIndId.equals(dim.getId()) || dimOrIndId.equals(dim.getName());
            }).toArray();
            if (tmp != null && tmp.length == 1) {
//                logger.info("get dimension for cube [{}] with id [{}]", cube, dimOrIndId);
                return (OlapElement) tmp[0];
            }
//            logger.info("can not get dim from cube [{}] with id [{}]", cube, dimOrIndId);
        }
        
        if (cube.getMeasures() != null) {
//            OlapElement measure = cube.getMeasures().get(dimOrIndId);
            Object[] tmp= cube.getMeasures().values().stream().filter(m -> m != null).filter(m ->{
                    return dimOrIndId.equals(m.getId()) || dimOrIndId.equals(m.getName());
            }).toArray();
            if (tmp != null && tmp.length == 1) {
                logger.info("get measuer for cube [{}] with id [{}]", cube, dimOrIndId);
                return (OlapElement) tmp[0];
            }
//            logger.info("can not get measuer from cube [{}] with id [{}]", cube, dimOrIndId);
        }
        
//        logger.info("current cube [{}] 's dimensions and measurs define are empty", cube);
        return null;
    }
}
