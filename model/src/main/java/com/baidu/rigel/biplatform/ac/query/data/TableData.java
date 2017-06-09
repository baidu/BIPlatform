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
package com.baidu.rigel.biplatform.ac.query.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 *Description: DataModel数据的二维表展现形式
 * @author david.wang
 *
 */
public class TableData implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = -917029423791246077L;

    /**
     * column's info
     */
    private List<Column> columns;
    
    private Map<String, List<String>> colBaseDatas;

    /**
     * @return the colBaseDatas
     */
    public Map<String, List<String>> getColBaseDatas() {
        if (this.colBaseDatas == null) {
            this.colBaseDatas = Maps.newHashMap ();
        }
        return colBaseDatas;
    }

    /**
     * @param colBaseDatas the colBaseDatas to set
     */
    public void setColBaseDatas(Map<String, List<String>> colBaseDatas) {
        this.colBaseDatas = colBaseDatas;
    }
    
    
    /**
     * @return the columns
     */
    public List<Column> getColumns() {
        if (this.columns == null) {
            this.columns = Lists.newArrayList ();
        }
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }


    /**
     * 
     *Description: 数据表列元数据信息描述
     * @author david.wang
     *
     */
    public static class Column implements Serializable {
        
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 3151301875582323397L;

        /**
         * name
         */
        public final String name;
        
        /**
         * key
         */
        public final String key;
        
        /**
         * dataType
         */
        public final String dataType;
        
        /**
         * caption
         */
        public final String caption;
        
        /**
         * tableName
         */
        public final String tableName;
        
        /**
         * 保留字段
         */
        private String dbName;
        
        public Column(String key, String name, String caption, String dataType, String tableName) {
            super ();
            this.key = key;
            this.name = name;
            this.caption = caption;
            this.dataType = dataType;
            this.tableName = tableName;
        }

        /**
         * @return the dbName
         */
        public String getDbName() {
            return dbName;
        }

        /**
         * @param dbName the dbName to set
         */
        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode ());
            result = prime * result + ((key == null) ? 0 : key.hashCode ());
            result = prime * result + ((dataType == null) ? 0 : dataType.hashCode ());
            result = prime * result + ((tableName == null) ? 0 : tableName.hashCode ());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass () != obj.getClass ()) {
                return false;
            }
            Column other = (Column) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals (other.name)) {
                return false;
            }
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals (other.key)) {
                return false;
            }
            if (dataType == null) {
                if (other.dataType != null) {
                    return false;
                }
            } else if (!dataType.equals (other.dataType)) {
                return false;
            }
            if (tableName == null) {
                if (other.tableName != null) {
                    return false;
                }
            } else if (!tableName.equals (other.tableName)) {
                return false;
            }
            return true;
        }
        
        
    }
}
