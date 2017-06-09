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
package com.baidu.rigel.biplatform.ma.model.builder;

import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;

/**
 * 
 * Director : design for providing convenience API which can easily fast transform data structure
 * between {@link StarModel} and {@link Schema}. 
 * <p>{@link StarModel} describe the physical model and actual relation between fact table and
 * dimension table, all of operational analysis base on it. {@link Schema} is a logic model, describe some  business subject areas.
 *  <hr/>
 *      all know subclasses<br/>:
 *          {@link DirectorImpl}
 * </p>
 * 
 * 
 * @see com.baidu.rigel.biplatform.ac.model.Schema
 * @see com.baidu.rigel.biplatform.ma.model.meta.StarModel
 * @since JDK1.8 or after
 * @version Silkroad 1.0.1
 * @author david.wang
 *
 */
public interface Director {
    
    /**
     * 
     * transform star models to schema define.<p> If want build schema success, need prepare more than one star model.
     * So, if the star model array is empty or null, the operation will return null, that's mean failed
     * </p>
     * @param starModels -- star model array
     * @return Schema if success return schema instance else null
     * 
     */
    Schema getSchema(StarModel[] starModels);
    
    /**
     * 
     * convert schema define to star model define.<p> on special occasions, you need convert schema to star model for
     * update schema through star model. This operation will lose some properties which define in schema,
     * for example user defined dimension show name, calculate member define and so on, because that can not be 
     * found in fact table and dimension table meta define.
     * </p>
     * @param schema -- schema instance
     * @return star models if success return star models else null
     */
    StarModel[] getStarModel(Schema schema);
    
    /**
     * update schema instance through new star models. 
     * <p>the fact table's foreign key or the reference column changed will generate new star model, 
     * <br/>if the schema build from the star model, that need to be update.
     * </p>
     * 
     * @param schema -- old schema
     * @param starModels -- new star models 
     * @return modified schema if start models is null or empty return original instance
     */
    Schema modifySchemaWithNewModel(Schema schema, StarModel[] starModels);
    
}
