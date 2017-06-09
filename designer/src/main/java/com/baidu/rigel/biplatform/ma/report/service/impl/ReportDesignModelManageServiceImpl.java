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

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.TimerAreaLogicModel;
import com.baidu.rigel.biplatform.ma.report.model.TimerAreaLogicModel.TimeRange;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelManageService;

/**
 * 
 * 报表模型管理接口
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
@Service("manageService")
public class ReportDesignModelManageServiceImpl implements ReportDesignModelManageService {
    
    /**
     * 日志管理器
     */
    private Logger logger = LoggerFactory.getLogger(ReportDesignModelManageService.class);
    
    /**
     * {@inheritDoc}
     * @throws ReportModelOperationException 
     */
    @Override
    public ReportDesignModel addExtendArea(ReportDesignModel ori,
            ExtendArea extendArea) throws ReportModelOperationException {
        logger.info("begin add extend area into report model");
        if (!checkModel(ori, extendArea)) {
            throw new ReportModelOperationException("can not add area to model. ");
        }
        // assign id for new extend area
        // extendArea.setId(UuidGeneratorUtils.generate());
        ori.addExtendArea(extendArea);
        logger.info("successfully add extend area into report model");
        return ori;
    }
    
    /**
     * 
     * @param ori
     * @param extendArea
     * @return
     */
    private boolean checkModel(ReportDesignModel ori, ExtendArea extendArea) {
        if (ori == null) {
            logger.debug("ori model is empty");
            return false;
        }
        if (extendArea == null) {
            logger.debug("extend area define is empty");
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     * @throws ReportModelOperationException 
     */
    @Override
    public ReportDesignModel removeExtendArea(ReportDesignModel ori,
            String extendAreaId) throws ReportModelOperationException {
        if (checkInput(ori, extendAreaId)) {
            ori.deleteAreaById(extendAreaId);
            logger.info("successfully remove extendArea with id : " + extendAreaId);
            return ori;
        } else {
            throw new ReportModelOperationException("can not remove area from model.");
        }
    }
    
    /**
     * 
     * @param ori
     * @param extendAreaId
     * @return
     */
    private boolean checkInput(ReportDesignModel ori, String extendAreaId) {
        logger.info("begin remove area operation");
        if (ori == null) {
            logger.debug("ori model is empty");
            return false;
        }
        if (StringUtils.isEmpty(extendAreaId)) {
            logger.debug("extendArea is empty");
            return false;
        }
        ExtendArea area = ori.getExtendById(extendAreaId);
        if (area == null) {
            logger.debug("extend area define is empty");
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    @Override
    public ReportDesignModel addOrUpdateItemIntoArea(ReportDesignModel ori, String areaId,
            Item item, PositionType position) throws ReportModelOperationException {
        logger.info("begin add item into area");
        if (this.checkInput(ori, areaId)) {
            ExtendArea area = ori.getExtendById(areaId);
            if (item.getId() != null) {
                Item oldItem = getItem(area, item.getOlapElementId());
                if (oldItem == null) {
                    logger.debug("can not get item with item id : " + item.getOlapElementId());
                    return null;
                }
                if (item.getParams() != null) {
                    oldItem.setParams(item.getParams());
                }
                if (position == PositionType.CAND_DIM) {
                    if (area instanceof LiteOlapExtendArea) {
                        ((LiteOlapExtendArea) area).addCandDim(item);
                    } else {
                        item.setPositionType(PositionType.CAND_DIM);
                        area.addSelectionDimItem(item);
                    }
                } else if (position == PositionType.CAND_IND) {
                    if (area instanceof LiteOlapExtendArea) {
                        ((LiteOlapExtendArea) area).addCandInd(item);
                    } else {
                        item.setPositionType(PositionType.CAND_IND);
                        area.addSelectionMeasureItem(item);
                    }
                } else {
                    changeSelItemChartType(item, area);
                }
            } else {
                addNewInfoIntoArea(ori, areaId, item, position, area);
            }
            return ori;
        } else {
            return null;
        }
    }

    /**
     * @param item
     * @param area
     */
    private void changeSelItemChartType(Item item, ExtendArea area) {
        final String chartTypeKey = "chartType";
        final Object chartType = item.getParams().get(chartTypeKey);
        if (chartType == null) {
            return;
        }
        
        if (area.getLogicModel() == null || area.getLogicModel().getSelectionMeasures() == null) {
            return;
        }
        Map<String, Item> tmpMap = DeepcopyUtils.deepCopy(area.getLogicModel().getSelectionMeasures());
        tmpMap.forEach((k, v) -> {
            Object tmp = v.getParams().get(chartTypeKey);
            if (tmp != null) {
                if ("column".equalsIgnoreCase(chartType.toString()) || "line".equalsIgnoreCase(chartType.toString())) {
                    if (!"column".equalsIgnoreCase(tmp.toString()) && !"line".equalsIgnoreCase(tmp.toString())) {
                        v.getParams().put(chartTypeKey, chartType);
                    }
                } else {
                    v.getParams().put(chartTypeKey, tmp);
                }
            } else {
                v.getParams().put(chartTypeKey, chartType);;
            }
            area.getLogicModel().getSelectionMeasures().put(k, v);
        }); 
    }
    
    /**
     * 
     * @param area
     * @param id
     * @return
     */
    private Item getItem(ExtendArea area, String id) {
        Map<String, Item> allItems = area.listAllItems();
        if (allItems != null) {
            return allItems.get(id);
        }
        return null;
    }
    
    /**
     * 
     * @param ori
     * @param areaId
     * @param item
     * @param position
     * @param area
     * @throws Exception
     */
    private void addNewInfoIntoArea(ReportDesignModel ori, String areaId, Item item,
        PositionType position, ExtendArea area) throws ReportModelOperationException {
        logger.info("begin create new item and into area");
        // modify by jiangyichao at 2014-11-03
        item.setId(item.getOlapElementId());
        // item.setId(UuidGeneratorUtils.generate());
        if (!StringUtils.hasText(area.getCubeId())) {
            area.setCubeId(item.getCubeId());
        } else if (!area.getCubeId().equals(item.getCubeId())) {
            throw new ReportModelOperationException("can not add item from different cubes.");
        }
        item.setCubeId(item.getCubeId());
        item.setAreaId(areaId);
        item.setReportId(ori.getId());
        if (ori.getSchema() != null) {
            item.setSchemaId(ori.getSchema().getId());
        }
        item.setPositionType(position);
        // TODO assign other values
        if (area.getLogicModel() == null) {
            if (area.getType() == ExtendAreaType.TIME_COMP) {
                area.setLogicModel(new TimerAreaLogicModel());
            } else {
                area.setLogicModel(new LogicModel());
            }
        }
        if (area.getType() == ExtendAreaType.TIME_COMP) {
            TimerAreaLogicModel logicModel = (TimerAreaLogicModel) area.getLogicModel();
            logicModel.addTimeDimension(item, null);
            logicModel.addRow(item);
            return;
        }
        switch (position) {
            case X:
                area.getLogicModel().addRow(item);
                break;
            case Y:
                area.getLogicModel().addColumn(item);
                break;
            case S:
                area.getLogicModel().addSlice(item);
                break;
            case CAND_DIM:
                if (area.getType() != ExtendAreaType.LITEOLAP) {
                    item.setPositionType(PositionType.CAND_DIM);
                    area.addSelectionDimItem(item);
                } else {
                    ((LiteOlapExtendArea) area).addCandDim(item);
                }
                break;
            case CAND_IND:
                if (area.getType() != ExtendAreaType.LITEOLAP) {
                    item.setPositionType(PositionType.CAND_IND);
                    area.addSelectionMeasureItem(item);
                } else {
                    ((LiteOlapExtendArea) area).addCandInd(item);
                }
                break;
            default:
        }
        logger.info("successfully add item into area");
    }
    
    /**
     * {@inheritDoc}
     * @throws ReportModelOperationException 
     */
    @Override
    public ReportDesignModel removeItem(ReportDesignModel ori, String areaId, String itemId,
            PositionType position) throws ReportModelOperationException {
        logger.info("begin remove item from report model");
        if (this.checkInput(ori, areaId)) {
            ExtendArea area = ori.getExtendById(areaId);
            String olapElementId = itemId;
            switch (position) {
                case X:
                    if (area.getType() == ExtendAreaType.TIME_COMP) {
                        TimerAreaLogicModel logicModel = (TimerAreaLogicModel) area.getLogicModel();
                        LinkedHashMap<Item, TimeRange> items = logicModel.getTimeDimensions();
                        for (Item item : items.keySet()) {
                            if (item.getId().equals(itemId)) {
                                logicModel.getTimeDimensions().remove(item);
                                area.removeSelectDimItem(itemId);
                                break;
                            }
                        }
                        area.removeItem(itemId);
                    } else if (isQueryCompArea(area.getType())) {
                    	area.getLogicModel().removeRow(olapElementId);
                    	area.getLogicModel().getSelectionDims().remove(olapElementId);
                    } else {
                        area.getLogicModel().removeRow(olapElementId);
                    }
                    break;
                case Y:
                    area.getLogicModel().removeColumn(olapElementId);
                    break;
                case S:
                    area.getLogicModel().removeSlice(olapElementId);
                    break;
                case CAND_DIM:
                    if (area.getType() != ExtendAreaType.LITEOLAP) {
                        area.removeSelectDimItem(olapElementId);
                    } else {
                        ((LiteOlapExtendArea) area).removeCandDim(olapElementId);
                    }
//                    if (area.listAllItems().containsKey(olapElementId)) {
//                        throw new ReportModelOperationException("不能从候选区删除已经使用的维度！");
//                    }
                    break;
                case CAND_IND:
                    if (area.getType() != ExtendAreaType.LITEOLAP) {
                        area.removeSelectMeasureItem(olapElementId);
                    } else {
                        ((LiteOlapExtendArea) area).removeCandInd(olapElementId);
                    }
//                    if (area.listAllItems().containsKey(olapElementId)) {
//                        throw new ReportModelOperationException("不能从候选区删除已经使用的维度！");
//                    }
                    break;
                default:
            }
            logger.info("successfully remove item from report model");
            return ori;
        }
        return null;
    }

    private boolean isQueryCompArea(ExtendAreaType type) {
        return type == ExtendAreaType.SELECT
                || type == ExtendAreaType.MULTISELECT
                || type == ExtendAreaType.CASCADE_SELECT
                || type == ExtendAreaType.SINGLE_DROP_DOWN_TREE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel changeItemOrder(ReportDesignModel model, String areaId, String source,
            String target, PositionType type) throws ReportModelOperationException {
        logger.info("begin remove item from report model");
        if (this.checkInput(model, areaId)) {
            ExtendArea area = model.getExtendById(areaId);
            LogicModel logicModel = area.getLogicModel();
            Integer first = Integer.valueOf(source);
            Integer second = Integer.valueOf(target);
            switch (type) {
                case X:
                    Item[] rows = logicModel.getRows();
                    moveItem(rows, first, second);
                    logicModel.resetRows(rows);
                    // 如果为日期控件，修改日期维度中的顺序
                    if (area.getType() == ExtendAreaType.TIME_COMP) {
                        TimerAreaLogicModel timerLogicModel = (TimerAreaLogicModel) logicModel;
                        LinkedHashMap<Item, TimeRange> newTimeItems = 
                            this.moveItem(rows, timerLogicModel.getTimeDimensions());
                        timerLogicModel.setTimeDimensions(newTimeItems);
                    }
                    return model;
                case Y:
                    Item[] columns = logicModel.getColumns();
                    moveItem(columns, first, second);
                    logicModel.resetColumns(columns);
                    return model;
                default:
            }
            throw new ReportModelOperationException("不支持移动");
        }
        return null;
    }

    /**
     * 
     * @param rows
     * @param source
     * @param target
     */
    private void moveItem(Item[] rows, Integer source, Integer target) {
        if (source > target) {
            /**
             * 从后挪到前 比如将1，2，3，4，5中的4挪到2之后，挪动之后为1，2，4, 3，5
             * 从指定位置开始，将所有元素后移一位
             */
            Item tmp = rows[source];
            for (int i = source; i > target; --i) {
                rows[i] = rows[i - 1];
            }
            rows[target] = tmp;
        } else {
            /**
             * 
             * 从前挪到后，比如将1，2，3，4，5中的2挪到4之后，挪动之后为1，3，4，2，5
             * 从指定位置开始，所有元素前移一位
             * 
             */
            Item tmp = rows[source];
            for (int j = source; j < target; ++j) {
                rows[j] = rows[j + 1];
            }
            rows[target] = tmp;
        }
    }
    
    /**
     * 对日期维度进行重新排序
     * @param rows  已经排序好的rows
     * @param old   原有时间维度
     * @return
     */
    private LinkedHashMap<Item, TimeRange> moveItem(Item[] rows, LinkedHashMap<Item, TimeRange> old) {
        LinkedHashMap<Item, TimeRange> newTimeItems = new LinkedHashMap<Item, TimeRange>();
        for (int i = 0; i < rows.length; i++) {
            newTimeItems.put(rows[i], old.get(rows[i]));
        }
        return newTimeItems;
    }
    
}
