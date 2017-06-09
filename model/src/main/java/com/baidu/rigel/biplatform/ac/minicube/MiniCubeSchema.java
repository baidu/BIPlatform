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
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Schema;

/**
 * minicube schema implemention
 * 
 * @author xiaoming.chen
 *
 */
public class MiniCubeSchema extends OlapElementDef implements Schema { // ,Externalizable{

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8953870059441592739L;

    /**
     * cubes
     */
    private Map<String, MiniCube> cubes;

    /**
     * reference datasource name
     */
    private String datasource;

    public MiniCubeSchema() {
        this("null_name");
    }

    /**
     * construct with schema name
     * 
     * @param name schema name
     */
    public MiniCubeSchema(String name) {
        super(name);
    }

    @Override
    public Map<String, MiniCube> getCubes() {
        // return Collections.unmodifiableMap(this.cubes);
        return this.cubes;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public void setCubes(Map<String, MiniCube> cubes) {
        this.cubes = cubes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{id : " + this.getId());
        str.append(",name : " + this.getName());
        str.append(",dsId : " + this.getDatasource());
        str.append("}");
        return str.toString();
    }
}
