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
package com.baidu.rigel.biplatform.ma.model.external.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.external.service.MeasureClassfyService;
import com.baidu.rigel.biplatform.ma.model.external.utils.MeasureClassfyMetaUtils;
import com.baidu.rigel.biplatform.ma.model.external.vo.MeasureClassfyObject;
import com.google.common.collect.Lists;

/**
 *Description:
 * @author david.wang
 *
 */
@Service("measureClassfyService")
public class MeasureClassfyServiceImpl implements MeasureClassfyService {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger (MeasureClassfyServiceImpl.class);
    
    /**
     * sql
     */
    private static final String SQL = "SELECT FIRST_CLASS_TYPE, FIRST_CLASS_TYPE_NAME, FIRST_CLASS_TYPE_DESC,"
            + "SECOND_CLASS_TYPE, SECOND_CLASS_TYPE_NAME, SECOND_CLASS_TYPE_DESC,"
            + "THIRD_CLASS_TYPE, THIRD_CLASS_TYPE_NAME, THIRD_CLASS_TYPE_DESC,"
            + "SELECTED_OPERATION_TYPE "
            + " FROM FACT_TAB_COL_META_CLASS";
    
    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.ma.model.external.service.MeasureClassfyService
     * #getChangableMeasureClassfyMeta(java.lang.String, com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine)
     */
    @Override
    public List<MeasureClassfyObject> getChangableMeasureClassfyMeta(String table, DataSourceDefine ds, String secKey , String areaId)
        throws Exception {
        List<String> tmp = genResultList (ds, secKey , areaId);
        if (tmp.isEmpty ()) {
            return Lists.newArrayList ();
        }
        return convertMapToMeasureClassfyList(tmp);
    }

    /**
     * 
     * @param ds
     * @param secKey
     * @param areaId
     * @return List<String>
     * @throws DataSourceOperationException
     * @throws DataSourceConnectionException
     * @throws SQLException
     */
    private List<String> genResultList(DataSourceDefine ds, String secKey, String areaId) 
            throws DataSourceOperationException, DataSourceConnectionException,
            SQLException {
        List<String> tmp = Lists.newArrayList ();
        String where = "";
        if(!StringUtils.isEmpty(areaId)){
            where = " WHERE AREA_ID = ? " ;
        }
        String dbType = ds.getDataSourceType().name ();
        @SuppressWarnings("unchecked")
        DataSourceConnectionService<Connection> dsConnService = 
                (DataSourceConnectionService<Connection>) 
                DataSourceConnectionServiceFactory.getDataSourceConnectionServiceInstance(dbType);
        Connection conn = null;
        try {
            conn = (Connection) dsConnService.createConnection (ds, secKey);
            PreparedStatement ps = conn.prepareStatement (SQL+where);
            if(!StringUtils.isEmpty(where)){
                ps.setString(1, areaId);
            }
            ResultSet rs = ps.executeQuery ();
            StringBuilder str = null;
            while (rs.next ()) {
                str = new StringBuilder();
                str.append (rs.getString ("FIRST_CLASS_TYPE") + "\t");
                str.append (rs.getString ("FIRST_CLASS_TYPE_NAME") + "\t");
                str.append (rs.getString ("FIRST_CLASS_TYPE_DESC") + "\t");
                str.append (rs.getString ("SECOND_CLASS_TYPE") + "\t");
                str.append (rs.getString ("SECOND_CLASS_TYPE_NAME") + "\t");
                str.append (rs.getString ("SECOND_CLASS_TYPE_DESC") + "\t");
                str.append (rs.getString ("THIRD_CLASS_TYPE") + "\t");
                str.append (rs.getString ("THIRD_CLASS_TYPE_NAME") + "\t");
                str.append (rs.getString ("THIRD_CLASS_TYPE_DESC") + "\t");
                str.append (rs.getString ("SELECTED_OPERATION_TYPE") );                
                tmp.add (str.toString ());
            }
        } finally {
            dsConnService.closeConnection (conn);
        }
        return tmp;
    }
    
    /**
     * 
     * @param resultList
     * @return List<MeasureClassfyObject>
     */
   private List<MeasureClassfyObject> convertMapToMeasureClassfyList(List<String> resultList) {
       List<MeasureClassfyObject> rs = Lists.newArrayList ();
       String[] resultArray = null;
       for (String tmp : resultList) {
           resultArray = tmp.split ("\t");
           MeasureClassfyObject firstClassObj = new MeasureClassfyObject ();
           firstClassObj.setDesc (resultArray[2]);
           firstClassObj.setCaption (resultArray[1]);
           firstClassObj.setName (resultArray[0]);
           int index = -1;
           if (rs.isEmpty () || (index = rs.indexOf (firstClassObj)) == -1) {
               rs.add (firstClassObj);
           } else {
               firstClassObj = rs.get (index);
           }
           MeasureClassfyObject secondClassObj = genSecondClassfy (resultArray, firstClassObj);
           genThirdClassfy (resultArray, secondClassObj); 
       }
        return rs;
    }

   /**
    * 获取明细元数据描述信息
    * @param resultArray
    * @param secondClassObj
    */
    private void genThirdClassfy(String[] resultArray, MeasureClassfyObject secondClassObj) {
        MeasureClassfyObject thirdClassfy = new MeasureClassfyObject ();
        thirdClassfy.setName (resultArray[6]);
        thirdClassfy.setCaption (resultArray[7]);
        thirdClassfy.setDesc (resultArray[8]);
        if ("1".equals (resultArray[9])) {
            thirdClassfy.setSelected (null);
        }
        List<MeasureClassfyObject> secondClassChildren = secondClassObj.getChildren ();
        if (CollectionUtils.isEmpty (secondClassChildren)
                || secondClassChildren.indexOf (thirdClassfy) == -1) {
            secondClassChildren.add (thirdClassfy);
        }
    }

    private MeasureClassfyObject genSecondClassfy(String[] resultArray, MeasureClassfyObject firstClassObj) {
        int childIndex = -1;
        MeasureClassfyObject secondClassObj = new MeasureClassfyObject();
        secondClassObj.setName (resultArray[3]);
        secondClassObj.setCaption (resultArray[4]);
        secondClassObj.setDesc (resultArray[5]);
        List<MeasureClassfyObject> firstClassChildren = firstClassObj.getChildren ();
        if (CollectionUtils.isEmpty (firstClassChildren) 
            || (childIndex = firstClassChildren.indexOf (secondClassObj)) == -1) {
            firstClassChildren.add (secondClassObj);
        } else {
            secondClassObj = firstClassChildren.get (childIndex);
        }
        return secondClassObj;
    }

 /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.ma.model.external.service.MeasureClassfyService
     * #getChangalbeMeasuerMeta(java.lang.String, com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine)
     */
    @Override
    public List<String> getChangalbeMeasuerMeta(String factTable, DataSourceDefine ds, String secKey, String areaId) {
        if (StringUtils.isEmpty (factTable)) {
            throw new RuntimeException ("fact table not be null");
        }
        final List<String> rs = Lists.newArrayList ();
        try {
            List<MeasureClassfyObject> measuerClassfyMeta = getChangableMeasureClassfyMeta (factTable, ds, secKey,areaId);
            List<MeasureClassfyObject> tmp = Lists.newArrayList ();
            for (MeasureClassfyObject obj : measuerClassfyMeta) {
                tmp.addAll (MeasureClassfyMetaUtils.getLeafMeasureMeta(obj));
            }
            tmp.forEach (meta -> {
                if (meta.getName ().startsWith (factTable + ".")) {
                    rs.add (meta.getName ().replace (factTable + ".", ""));
                } else if (!meta.getName ().contains (".")) {
                    rs.add (meta.getName ());
                }
            });
        } catch (Exception e) {
            LOG.error (e.getMessage (), e);
        }
        return rs;
    }
    
}
