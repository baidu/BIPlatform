package com.baidu.rigel.biplatform.asyndownload.bo;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;

/**
 * 类AddTaskParameters.java的实现描述：AddTaskParameters 类实现描述 
 * @author luowenlei 2015年8月31日 上午11:23:51
 */
public class AddTaskParameters {
    
    /**
     * reportName
     */
    public String reportName;
    
    /**
     * recMail
     */
    public String recMail;
    
    /**
     * columns
     */
    public List<String> columns;

    /**
     * QuestionModel
     */
    public QuestionModel questionModel;
    
    /**
     * cookies
     */
    public Map<String, String> cookies;
    
    /**
     * requestUrl
     */
    public String requestUrl;
    
    /**
     * default generate get columns
     * @return the columns
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * default generate set columns
     * @param columns the columns to set
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * default generate get recMail
     * @return the recMail
     */
    public String getRecMail() {
        return recMail;
    }

    /**
     * default generate set recMail
     * @param recMail the recMail to set
     */
    public void setRecMail(String recMail) {
        this.recMail = recMail;
    }
    
    
    /**
     * default generate get cookies
     * @return the cookies
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * default generate set cookies
     * @param cookies the cookies to set
     */
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    /**
     * default generate get questionModel
     * @return the questionModel
     */
    public QuestionModel getQuestionModel() {
        return questionModel;
    }

    /**
     * default generate set questionModel
     * @param questionModel the questionModel to set
     */
    public void setQuestionModel(QuestionModel questionModel) {
        this.questionModel = questionModel;
    }

    /**
     * default generate get reportName
     * @return the reportName
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * default generate set reportName
     * @param reportName the reportName to set
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }


    /**
     * default generate get requestUrl
     * @return the requestUrl
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * default generate set requestUrl
     * @param requestUrl the requestUrl to set
     */
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
}
