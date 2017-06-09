package com.baidu.rigel.biplatform.ma.resource.builder;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Maps;

/**
 * 此builder类专门处理前端querydata请求时，对请求param参数的处理
 * 
 * @author majun04
 *
 */
public class QueryDataParamBuilder {

    /**
     * 为下钻操作时候的展开操作封装参数之用，该封装为第一步参数封装，只处理初步的uniqueName等参数
     * 
     * @param dimParamObj runtime中对应条件上下文的参数对象
     * @param type 下钻类型
     * @param condition 前端传入的下钻uniqueName
     * @param queryParams 后续查询需要用到的param对象
     * @return 返回构建和封装完毕的queryParams对象
     */
    public static Map<String, Object> bulidDillDownParams4PrepareExpand(Object dimParamObj, String type,
            String condition, Map<String, Object> queryParams) {
        String dimParam = "";
        if (dimParamObj instanceof String[]) {
            String[] dimParamArray = (String[]) dimParamObj;
            dimParam = String.join(",", dimParamArray);
        } else {
            dimParam = (String) dimParamObj;
        }
        // 只有展开操作需要构建param，这里需要判断：当前下钻时需要从下钻的条件condition里面取具体参数值还是从保存在runtime中的条件上下文取值
        if (type.equals("expand") && StringUtils.hasLength(dimParam) && !dimParam.equals("null")
                && condition.split("\\.").length < dimParam.split("\\.").length) {
            // 如果下钻参数过多，后续有不同分支进行处理
            if (dimParam.contains(",")) {
                queryParams.put("uniqueName", condition);
            } else {
                queryParams.put("uniqueName", dimParam);
            }
            Object lineUniqueNameObj = queryParams.get("lineUniqueName");
            queryParams.put("dimParam", dimParam);
            if (lineUniqueNameObj != null) {
                String lineUniqueNameStr = String.valueOf(lineUniqueNameObj);
                queryParams.put("lineUniqueName", lineUniqueNameStr.replace(
                        lineUniqueNameStr.substring(1, lineUniqueNameStr.length() - 1), condition));
            }
        }
        return queryParams;
    }

    /**
     * 为下钻做请求参数封装
     * 
     * @param queryParams queryParams
     * @param uniqNames uniqNames
     * @param targetIndex targetIndex
     * @param store store
     * @param model model
     * @return 返回封装完毕的queryParams参数对象
     */
    public static Map<String, Object> buildDillDownParams(Map<String, Object> queryParams, String[] uniqNames,
            int targetIndex, Map<String, Item> store, ReportDesignModel model) {
        String drillTargetUniqueName = uniqNames[targetIndex];
        String dimName = MetaNameUtil.getDimNameFromUniqueName(drillTargetUniqueName);

        /**
         * 把本行前面的维度都放到过滤中，作为过滤条件
         */
        for (int i = 0; i < targetIndex; i++) {
            String rowAheadUniqueName = uniqNames[i];
            String rowAheadDimName = MetaNameUtil.getDimNameFromUniqueName(rowAheadUniqueName);
            Item rowAhead = store.get(rowAheadDimName);
            queryParams.put(rowAhead.getOlapElementId(), rowAheadUniqueName);
            // 避免出现旋转操作参数遗漏
            model.getParams().values().forEach(p -> {
                if (p.getElementId().equals(rowAhead.getOlapElementId())) {
                    String[] tmp = MetaNameUtil.parseUnique2NameArray(rowAheadUniqueName);
                    queryParams.put(p.getName(), tmp[tmp.length - 1]);
                }
            });
        }

        Item row = store.get(dimName);
        queryParams.put(row.getOlapElementId(), drillTargetUniqueName);
        model.getParams().values().forEach(p -> {
            if (p.getElementId().equals(row.getOlapElementId())) {
                String[] tmp = MetaNameUtil.parseUnique2NameArray(drillTargetUniqueName);
                queryParams.put(p.getName(), tmp[tmp.length - 1]);
            }
        });
        return queryParams;
    }

    
    public static Map<String, Object> modifyReportParams(Map<String, ReportParam> params, Map<String, Object> queryParams,
            Cube cube) {
        cube = QueryUtils.transformCube(cube);
        Map<String, Object> rs = Maps.newHashMap();
        rs.putAll(queryParams);
        final Map<String, String> tmp = Maps.newHashMap();
        params.forEach((k, v) -> tmp.put(v.getElementId(), v.getName()));
        queryParams
                .forEach((k, v) -> {
                    if (tmp.containsKey(k) && v != null) {
                        String uniqueName = null;
                        if (v instanceof String[]) {
                            uniqueName = ((String[]) v)[0];
                        } else if (MetaNameUtil.isUniqueName(v.toString())
                                && !MetaNameUtil.isAllMemberUniqueName(v.toString())) {
                            uniqueName = v.toString();
                        }
                        if (!StringUtils.isEmpty(uniqueName) && MetaNameUtil.isUniqueName(uniqueName)) {
                            String[] array = MetaNameUtil.parseUnique2NameArray(uniqueName);
                            rs.put(tmp.get(k), array[array.length - 1]);
                        } else {
                            rs.put(tmp.get(k), v.toString());
                        }
                    }
                });
        return rs;
    }
}
