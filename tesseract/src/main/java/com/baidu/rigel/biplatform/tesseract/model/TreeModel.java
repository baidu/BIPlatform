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
package com.baidu.rigel.biplatform.tesseract.model;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 类TreeModel.java的实现描述：abstract tree model
 * 
 * @author xiaoming.chen 2013-12-22 上午12:15:46
 */
public interface TreeModel extends Serializable {

    /**
     * 获取树节点的孩子接到
     * 
     * @return 当前节点的孩子接到
     */
    List<? extends TreeModel> getChildren();

    /**
     * 返回树的第level层节点
     * 
     * @param tree 树
     * @param level 指定层级
     * @return 返回指定层级的节点集合
     */
    @SuppressWarnings("unchecked")
    public static <T extends TreeModel> List<T> getLevelPosTreeNodes(T tree, int level) {
        List<T> results = Lists.newArrayList();

        if (level == 0) {
            results.add(tree);
        } else {
            if (tree.getChildren() != null && tree.getChildren().size() > 0) {
                level--;
                for (TreeModel node : tree.getChildren()) {
                    results.addAll(getLevelPosTreeNodes((T) node, level));
                }
            }
        }
        return results;
    }
}
