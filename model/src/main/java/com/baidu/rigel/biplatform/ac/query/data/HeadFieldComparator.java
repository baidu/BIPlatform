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

import java.util.Comparator;

import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;

/**
 * 返回表头结构排序比较实现
 * 
 * @author xiaoming.chen
 *
 */
public class HeadFieldComparator implements Comparator<HeadField> {

    /**
     * sortType 排序信息
     */
    private SortType sortType;

    /**
     * constructor
     * @param sortType
     */
    public HeadFieldComparator(SortType sortType) {
        this.sortType = sortType;

    }

    @Override
    public int compare(HeadField filed1, HeadField filed2) {
            
        if (filed2 == null && filed1 == null) {
            return 0;
        } else if (sortType == null || sortType.equals(SortType.NONE)) {
            return 0;
        } else {
            if (filed2 != null && filed2.getSummarizeData() == null) {
                if (sortType.equals(SortType.ASC)) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (filed1 != null && filed1.getSummarizeData() == null) {
                if (sortType.equals(SortType.ASC)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (filed1.getSummarizeData ().compareTo (filed2.getSummarizeData ()) == 0) {
                    boolean asc = sortType.equals (SortType.ASC);
                    if (asc) {
                        return filed1.getCaption ().compareTo (filed2.getCaption ());
                    } else {
                        return filed2.getCaption ().compareTo (filed1.getCaption ());
                    }
                } else if (sortType.equals (SortType.ASC)) {
                    return filed1.getSummarizeData ().compareTo (filed2.getSummarizeData ());
                } else {
                    return -filed1.getSummarizeData ().compareTo (filed2.getSummarizeData ());
                }

            }
        }

    }

}
