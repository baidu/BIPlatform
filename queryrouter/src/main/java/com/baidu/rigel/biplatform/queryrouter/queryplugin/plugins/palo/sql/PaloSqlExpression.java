package com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.palo.sql;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QuestionModelTransformationException;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;

public class PaloSqlExpression extends SqlExpression {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3617385716589467950L;
    
    /**
     * palo Id
     */
    private static final String ID = "id";
    
    /**
     * @param driver
     *            jdbc driver
     * @param facttableAlias
     *            facttableAlias facttableAlias
     */
    public PaloSqlExpression(String driver) {
        super(driver);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.common.jdbc
     * .SqlExpression
     * #generateCountSql(com.baidu.rigel.biplatform.queryrouter.queryplugin
     * .plugins.model.PlaneTableQuestionModel, java.util.Map, java.util.List,
     * java.util.Map)
     */
    @Override
    public void generateCountSql(PlaneTableQuestionModel questionModel,
            Collection<SqlColumn> sqlColumns, List<SqlColumn> needColums,
            Map<String, List<Object>> whereData) throws QuestionModelTransformationException {
        // TODO Auto-generated method stub
        super.generateCountSql(questionModel, sqlColumns, needColums, whereData);
        this.getCountSqlQuery().getSelect().setSql(" select count(" + ID + ") as totalc ");
    }
}
