package com.baidu.rigel.biplatform.ma.report.query.newtable.build;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.DimDataDefine;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.IndDataDefine;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.MutilDimTable;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 前端新版多维表Builder
 * 
 * @author majun04
 *
 */
public class MutilDimTableBuilder {
    /**
     * 后端返回的datamodel具体数据
     */
    private DataModel dataModel = null;
    /**
     * cube对象
     */
    private Cube cube = null;

    private String lineUniqueNamePrefix = null;
    /**
     * 表格横轴上的维度信息列表集合
     */
    private List<DimDataDefine> dims = Lists.newArrayList();
    /**
     * 维度描述
     */
    private List<String> dimsDesc = Lists.newArrayList();
    /**
     * 表格纵轴上的指标信息列表集合
     */
    private List<IndDataDefine> inds = Lists.newArrayList();
    /**
     * 表格所要展示的数据对象，以map格式封装
     */
    private List<Map<String, String>> data = Lists.newArrayList();
    /**
     * 从datamodel中得到的rowHeadFields
     */
    private List<HeadField> rowHeadFields = null;
    /**
     * 从datamodel中得到的columnHeadFields
     */
    private List<HeadField> columnHeadFields = null;

    /**
     * construct
     * 
     * @param dataModel
     */
    private MutilDimTableBuilder(DataModel dataModel, Cube cube,
            List<String> dimCaptions, String lineUniqueNamePrefix) {
        this.dataModel = dataModel;
        this.dimsDesc = dimCaptions;
        this.lineUniqueNamePrefix = lineUniqueNamePrefix;
        this.cube = QueryUtils.transformCube(cube);
        rowHeadFields = getLeafNodeList(dataModel.getRowHeadFields());
        columnHeadFields = getLeafNodeList(dataModel.getColumnHeadFields());

    }

    /**
     * 静态工厂方法
     * 
     * @param dataModel
     * @return new instance
     */
    public static MutilDimTableBuilder getInstance(DataModel dataModel, Cube cube, List<String> dimCaptions,
            String lineUniqueNamePrefix) {
        return new MutilDimTableBuilder(dataModel, cube, dimCaptions, lineUniqueNamePrefix);
    }

    /**
     * 构建维度描述信息
     * 
     * @return MutilDimTableBuilder
     */
    public MutilDimTableBuilder buildDimsDefine(boolean isDrillOption) {
        List<HeadField> rowHeadFieldTree = dataModel.getRowHeadFields();
        for (HeadField headField : rowHeadFieldTree) {
            DimDataDefine firstDimDataDefine = generateBaseDimDataByHeadField(headField);
            fillChildDimData(headField, firstDimDataDefine, isDrillOption);
            dims.add(firstDimDataDefine);
        }
        return this;
    }

    /**
     * 根据传入的headField和构造好的dimDataDefine对象，去递归构造该headField的子节点对象
     * 
     * @param headField headField
     * @param dimDataDefine dimDataDefine
     * @return 返回构建完毕的dimDataDefine维度描述对象
     */
    private DimDataDefine fillChildDimData(HeadField headField, DimDataDefine dimDataDefine, boolean isFirstDim) {
        // 当当前查询是多维度交叉场景，并且自己有孩子节点，并且当前处理的维度排在第一位,并且自己不是汇总节点时，才将下钻标识置为true
        boolean hasChildMember = headField.getChildren().size() > 0;
        if (headField.getNodeList().size() > 0 
                && headField.isHasChildren() 
                && isFirstDim
                && !MetaNameUtil.isAllMemberUniqueName(headField.getValue()) 
                && !hasChildMember) {
            dimDataDefine.setCanDrill(true);
        } else if (headField.isHasChildren()) {
            dimDataDefine.setHasExpandChildren(true);
        }
        if (hasChildMember) {
            dimDataDefine.setIsExpanded(true);
            for (HeadField childheadField : headField.getChildren()) {
                DimDataDefine childDimDataDefine = generateBaseDimDataByHeadField(childheadField);
                dimDataDefine.getExpandChildren().add(childDimDataDefine);
                fillChildDimData(childheadField, childDimDataDefine, isFirstDim);
            }
        }
        if (headField.getNodeList().size() > 0) {
            for (HeadField nodeHeadFiled : headField.getNodeList()) {
                DimDataDefine nodeDimDataDefine = generateBaseDimDataByHeadField(nodeHeadFiled);
                dimDataDefine.getDrillChildren().add(nodeDimDataDefine);
                fillChildDimData(nodeHeadFiled, nodeDimDataDefine, false);
            }
        }
        return dimDataDefine;
    }

    /**
     * 根据headField拼装DimDataDefine基础对象
     * 
     * @param headField headField
     * @return dimDataDefine
     */
    private DimDataDefine generateBaseDimDataByHeadField(HeadField headField) {
        DimDataDefine dimDataDefine = new DimDataDefine();
        dimDataDefine.setId(getDetailUniqueName(headField));
        dimDataDefine.setName(headField.getCaption());
        return dimDataDefine;
    }

    /**
     * 
     * @param headField 要被拼接name的headField
     * @return
     */
    private String getDetailUniqueName(HeadField headField) {
        String nodeUniqueName = headField.getNodeUniqueName();
        // 这里拼接data数据的id属性时候，需要考虑只有在交叉维度查询的时候，还需要拼接lineUniqueNamePrefix，单维度查询时候不需要添加，不然展开的lineUniqueName就不对了
        if (StringUtils.hasLength(lineUniqueNamePrefix) && dimsDesc.size() > 1) {
            nodeUniqueName = lineUniqueNamePrefix + "." + nodeUniqueName;
        }
        return nodeUniqueName;
    }

    /**
     * 构建指标描述信息
     * 
     * @return MutilDimTableBuilder
     */
    public MutilDimTableBuilder buildIndsDefine() {
        for (HeadField headField : columnHeadFields) {
            IndDataDefine indDataDefine = new IndDataDefine();
            indDataDefine.setId(headField.getNodeUniqueName());
            indDataDefine.setName(headField.getCaption());
            Map<String, Object> extInfos = headField.getExtInfos();
            indDataDefine.setSortType(extInfos.get("sortType") == null ? "NONE" : extInfos.get("sortType").toString());

            // 将形如[measure].[show]的UniqueName中取得该指标在模型定义中的olapElementId，然后返回给前端做判断跳转和传参之用
            String measureName = MetaNameUtil.getNameFromMetaName(headField.getValue());
            String measureNameId = "";
            if (cube.getMeasures().get(measureName) != null) {
                measureNameId = cube.getMeasures().get(measureName).getId();
                indDataDefine.setOlapElementId(measureNameId);
            }

            inds.add(indDataDefine);
        }
        return this;
    }

    /**
     * 构建表格数据
     * 
     * @return MutilDimTableBuilder
     */
    public MutilDimTableBuilder buildTableData() {
        int i = 0;
        List<List<BigDecimal>> columnBaseDataList = dataModel.getColumnBaseData();
        for (HeadField rowHeadField : rowHeadFields) {
            int j = 0;
            Map<String, String> dataMap = Maps.newHashMap();
            dataMap.put("id", getDetailUniqueName(rowHeadField));
            for (HeadField columnHeadField : columnHeadFields) {
                String measureName = columnHeadField.getNodeUniqueName();
                BigDecimal dataValue = columnBaseDataList.get(j).get(i);
                String dataValueStr = "";
                if (dataValue != null) {
                    dataValueStr = String.valueOf(dataValue);
                }
                dataMap.put(measureName, dataValueStr);
                j++;
            }
            data.add(dataMap);
            i++;
        }
        return this;
    }

    /**
     * 根据前几步构造完的多维表数据，返回多维表对象
     * 
     * @return MutilDimTable
     */
    public MutilDimTable buildMutilDimTable() {
        return new MutilDimTable(dims, inds, data, dimsDesc);
    }

    /**
     * 获取List<HeadField>结构下的所有叶子节点
     * 
     * @param headFields
     * @return headFields列表
     */
    private List<HeadField> getLeafNodeList(List<HeadField> headFields) {
        List<HeadField> resultList = new ArrayList<HeadField>();

        for (HeadField headField : headFields) {
            resultList.addAll(headField.getLeafFileds(true));
        }
        return resultList;

    }

}
