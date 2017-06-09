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
package com.baidu.rigel.biplatform.ma.model.service;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.UserDefineDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.resource.view.RelationTableView;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CallbackDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CustDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.DateDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.NormalDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CallbackDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CustDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.DateDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.NormalDimBindView;

/**
 * 
 * 星型模型构建服务
 * @author zhongyi
 *
 *         2014-7-31
 */
public interface StarModelBuildService {
    
    /**
     * 
     * @param dsId
     * @param cubeTables
     * @return
     */
    List<StarModel> buildStarModel(String dsId, List<FactTableMetaDefine> cubeTables);
    
    /**
     * 
     * @param dsId
     * @param securityKey 
     * @return 
     * @throws DataSourceOperationException 
     */
    List<RelationTableView> getAllTablesAndCols(String dsId, String securityKey) throws DataSourceOperationException;
    
    /**
     * 
     * @param callback
     * @param names
     * @return
     */
    List<CallbackDimTableMetaDefine> generateMetaDefine(CallbackDimBindView callback,
        Map<String, String> names);
    
    /**
     * 
     * @param date
     * @param names
     * @return
     */
    List<TimeDimTableMetaDefine> generateMetaDefine(DateDimBindView date, Map<String, String> names);
    
    /**
     * 
     * @param cust
     * @param names
     * @return
     */
    List<UserDefineDimTableMetaDefine> generateMetaDefine(CustDimBindView cust,
        Map<String, String> names);
    
    /**
     * 
     * @param dsId
     * @param normal
     * @param names
     * @return
     * @throws DataSourceOperationException 
     */
    List<StandardDimTableMetaDefine> generateMetaDefine(String dsId, NormalDimBindView normal,
        Map<String, String> names, String securityKey) throws DataSourceOperationException;
    
    /**
     * 
     * @param dimTable
     * @return
     */
    CallbackDimDetail generateCallbackDimDetail(CallbackDimTableMetaDefine dimTable);
    
    /**
     * 
     * @param dimTable
     * @return
     */
    CustDimDetail generateCustDimDetail(UserDefineDimTableMetaDefine dimTable);
    
    /**
     * 
     * @param dimTable
     * @return
     */
    NormalDimDetail generateNormalDimBindView(StandardDimTableMetaDefine dimTable);
    
    /**
     * 
     * @param cubeId
     * @param dimTable
     * @return
     */
    DateDimDetail generateDateDimDetail(String cubeId, TimeDimTableMetaDefine dimTable);
}
