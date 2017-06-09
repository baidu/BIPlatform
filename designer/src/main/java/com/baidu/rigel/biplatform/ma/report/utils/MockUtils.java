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
package com.baidu.rigel.biplatform.ma.report.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Mock工具类
 * 
 * @author zhongyi
 *
 */
public final class MockUtils {
    
    private static final String[] DIMS = new String[]{
        "北京", "天津", "河北", "上海", "重庆", "武汉", "南京", "成都", "青海", "长沙"
    };
    
    /**
     * 构造函数
     */
    private MockUtils() {
        
    }
    
    /**
     * 
     * @param dimension
     * @param level
     * @return
     */
    public static List<Member> mockMembers(Level level) {

        List<Member> members = Lists.newArrayList();
        for (int i = 0; i < 10; ++i) {
            MiniCubeMember member = new MiniCubeMember(i + "");
            member.setCaption(DIMS[i]);
            member.setLevel(level);
            member.setVisible(true);
            members.add(member);
        }
        return members;
    }
    
    /**
     * mock一个datamodel
     * @return
     */
    public static DataModel mockDataModel() {
        DataModel dataModel = new DataModel();
        List<HeadField> columnHeader = new ArrayList<HeadField>();
        // int c
        String[] trades = {"房屋出租", "房地产其他", "皮革", "服装", "鞋帽", "纺织辅料", "服装鞋帽其他", "工艺品", "礼品", "饰品"};
        
        String[] cols = {"消费", "点击", "访问人数", "人均消费", "平均价格", "总量", "分量", "单位产出", "月均订单数", "收入"};
        
        for (int i = 0; i < 10; ++i) {
            HeadField header = new HeadField(null);
            header.setCaption(cols[i]);
            header.setValue("column_" + i);
            header.setNodeUniqueName("小占统_" + i + "号");
            columnHeader.add(header);
        }
        dataModel.setColumnHeadFields(columnHeader);
        List<HeadField> rowHeader = new ArrayList<HeadField>();
        for (int i = 0; i < 10; ++i) {
            HeadField header = new HeadField(null);
            header.setCaption(trades[i]);
            header.setValue("row_" + i);
            header.setNodeUniqueName("小占统_" + i + "号");
            rowHeader.add(header);
        }
        dataModel.setRowHeadFields(rowHeader);
        List<List<BigDecimal>> datas = new ArrayList<List<BigDecimal>>();
        
        for (int i = 0; i < 10; ++i) {
            List<BigDecimal> cellDatas = new ArrayList<BigDecimal>();
            for (int j = 0; j < 10; ++j) {
                Random random = new Random();
                cellDatas.add(BigDecimal.valueOf(random.nextInt(10000)));
            }
            datas.add(cellDatas);
        }
        dataModel.setColumnBaseData(datas);
        return dataModel;
    }
    
    public static List<Map<String, String>> mockBreadCrumbs() {
        List<Map<String, String>> mainDims = Lists.newArrayList();
        Map<String, String> dims = Maps.newHashMap();
        dims.put("uniqName", "[dim_trade].[All dim_trades]");
        dims.put("showName", "全部行业");
        mainDims.add(dims);
        Map<String, String> dims2 = Maps.newHashMap();
        dims2.put("uniqName", "[dim_trade].[All dim_trades]");
        dims2.put("showName", "医疗行业");
        mainDims.add(dims2);
        Map<String, String> dims3 = Maps.newHashMap();
        dims3.put("uniqName", "[dim_trade].[All dim_trades]");
        dims3.put("showName", "牙科");
        mainDims.add(dims3);
        return mainDims;
    }
}
