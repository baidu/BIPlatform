package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.CellData;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColField;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;

/**
 * 跳转列操作utils工具类
 * 
 * @author majun04
 *
 */
public class OlapLinkUtils {

    /**
     * 操作列标识前缀
     */
    private static final String OPERATION_COLUMN_PREFIX = "operationColumn";
    /**
     * 操作列在表格上的显示名称
     */
    private static final String OPERATION_COLUMN_CAPTION = "操作列";

    /**
     * 根据已保存过的跳转信息map，取出所有包含操作列的跳转信息
     * 
     * @param linkInfoMap 跳转信息设置集合
     * @return 返回包含操作列的跳转信息集合
     */
    public static List<LinkInfo> getOperationColumKeys(Map<String, LinkInfo> linkInfoMap) {
        List<LinkInfo> savedLinkInfoList = new ArrayList<LinkInfo>();
        for (String columKey : linkInfoMap.keySet()) {
            if (columKey.startsWith(OPERATION_COLUMN_PREFIX)) {
                LinkInfo savedLinkInfo = linkInfoMap.get(columKey);
                savedLinkInfoList.add(savedLinkInfo);
            }
        }
        return savedLinkInfoList;
    }

    /**
     * 为pivottable增加操作列属性
     * 
     * @param linkInfoMap linkInfoMap
     * @param table pivottable
     * @return 返回修改过后的PivotTable对象
     */
    public static PivotTable addOperationColum(Map<String, LinkInfo> linkInfoMap, PivotTable table) {
        List<LinkInfo> savedOperationLinkInfoList = OlapLinkUtils.getOperationColumKeys(linkInfoMap);
        if (!CollectionUtils.isEmpty(savedOperationLinkInfoList)) {
            String linkBridgeIds = "";
            String linkBridgeStrs = "";
            int i = 0;
            for (LinkInfo linkInfo : savedOperationLinkInfoList) {
                if (i < savedOperationLinkInfoList.size() && i != 0) {
                    linkBridgeIds += ",";
                    linkBridgeStrs += ",";
                }
                linkBridgeIds += linkInfo.getColunmSourceId();
                linkBridgeStrs += linkInfo.getColunmSourceCaption();
                i++;
            }
            // 为ColDefine增加操作列属性
            List<ColDefine> colDefineList = table.getColDefine();
            ColDefine operationColDefine = new ColDefine();
            operationColDefine.setLinkBridge(linkBridgeIds);
            operationColDefine.setCaption(OPERATION_COLUMN_CAPTION);
            operationColDefine.setUniqueName(OPERATION_COLUMN_CAPTION);
            colDefineList.add(operationColDefine);
            // 为ColField增加操作列属性
            List<List<ColField>> colFieldListLs = table.getColFields();
            for (List<ColField> colFieldList : colFieldListLs) {
                ColField colField = new ColField();
                colField.setV(OPERATION_COLUMN_CAPTION);
                colField.setUniqName(OPERATION_COLUMN_CAPTION);
                colField.setRowspan(1);
                colField.setColSpan(1);
                colFieldList.add(colField);
            }
            // 为CellData增加操作列属性
            List<List<CellData>> cellDataList = table.getDataSourceRowBased();
            for (List<CellData> cellDatas : cellDataList) {
                CellData cellData = new CellData();
                cellData.setStr(linkBridgeStrs);
                cellDatas.add(cellData);
            }
        }
        return table;
    }
}
