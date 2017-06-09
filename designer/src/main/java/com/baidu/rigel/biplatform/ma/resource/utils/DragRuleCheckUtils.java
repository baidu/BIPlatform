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
/**
 * 
 */
package com.baidu.rigel.biplatform.ma.resource.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;

/**
 * 拖拽规则检查
 * 
 * @author zhongyi
 *
 */
public final class DragRuleCheckUtils {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DragRuleCheckUtils.class);
    
    /**
     *DragRuleCheckUtils 
     */
    private DragRuleCheckUtils() {
        
    }
    
    /**
     * 
     * @param element
     * @param position
     * @param area
     * @return boolean
     */
    public static boolean checkIllegal(OlapElement element, PositionType position, ExtendArea area) {
        if (element == null || area == null) {
            logger.error("element or area can not be null!");
            return false;
        }
        // modify by jiangyichao at 2015-05-18, 对于平面表列轴上可以拖动指标和维度
        if (area.getType() != null && area.getType().equals(ExtendAreaType.PLANE_TABLE)) {
            return position == PositionType.Y || position == PositionType.CAND_IND
                    ||position == PositionType.X || position == PositionType.S 
                    || position == PositionType.CAND_DIM;            
        } else {
        	/**
        	 * 指标的检查
        	 */
        	if (element instanceof Measure) {
        		return position == PositionType.Y || position == PositionType.CAND_IND;
        	} 
        	/**
        	 * 维度的检查
        	 */
        	if (element instanceof Dimension) {
        		return position == PositionType.X || position == PositionType.S 
        				|| position == PositionType.CAND_DIM;
        	}         	
        }
        return false;
    }
}
