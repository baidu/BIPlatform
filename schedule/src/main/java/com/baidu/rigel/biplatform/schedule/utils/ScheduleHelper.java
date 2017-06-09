package com.baidu.rigel.biplatform.schedule.utils;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.exception.ProductLineNameIsNullException;
import com.baidu.rigel.biplatform.schedule.exception.ScheduleException;

/**
 * Schedule调度辅助功能类
 * 
 * @author majun04
 *
 */
@Service
public class ScheduleHelper {
    /**
     * TASK_KEY
     */
    private static final String TASK_KEY = "schedule-task";

    private static final String PRODUCTLINE_NAME_DIVIDE_FLAG = "&";

    /**
     * 根据任务信息，生成随机的任务id
     * 
     * @param taskInfo 给定的任务信息
     * @return 返回生成的随机任务id
     */
    public static String generateTaskId(ScheduleTaskInfo taskInfo) {
        return Md5Util.encode(taskInfo.toString());
    }

    /**
     * 根据产品线名称，拼装该产品线下持久化过之后的调度任务保存目录
     * 
     * @param productLineName 产品线名称
     * @return 产品线下持久化过之后的调度任务保存目录
     */
    public static String getTaskSavedRootPath(String productLineName) {
        StringBuffer pathBuffer = new StringBuffer(productLineName);
        pathBuffer.append(File.separator);
        pathBuffer.append(TASK_KEY);
        return pathBuffer.toString();
    }

    /**
     * 根据用户标识，得出产品线名称
     * 
     * @param userStr 用户标识
     * @return 返回产品线名称
     */
    public static String parserProductLineNameFromUserDesc(String userStr) {
        String productLineName = null;
        if (userStr.contains(PRODUCTLINE_NAME_DIVIDE_FLAG)) {
            productLineName = userStr.split(PRODUCTLINE_NAME_DIVIDE_FLAG)[0];
        }
        return productLineName;
    }

    /**
     * 根据任务信息，生成filesave要持久化的文件路径地址
     * 
     * @param taskInfo 给定的任务信息
     * @return 返回filesave持久化的文件路径地址
     * @throws ScheduleException 当传入的调度任务信息为空时，抛出ScheduleException异常
     */
    public static String generateFileSavePath(ScheduleTaskInfo taskInfo) throws ScheduleException {
        if (taskInfo != null) {
            return generateFileSavePath(taskInfo.getTaskId(), taskInfo.getProductLineName());
        }
        throw new ScheduleException("taskInfo can not be null!");
    }

    /**
     * 根据任务id和产品线名称， 生成filesave持久化的文件路径地址
     * 
     * @param taskId 任务id
     * @param productLineName 产品线名称
     * @return 返回filesave持久化的文件路径地址
     * @throws ScheduleException 当发现产品线名称为空时，抛出ScheduleException异常
     */
    public static String generateFileSavePath(String taskId, String productLineName) throws ScheduleException {
        if (!StringUtils.isEmpty(productLineName) && !StringUtils.isEmpty(taskId)) {
            StringBuffer pathBuffer = new StringBuffer(productLineName);
            pathBuffer.append(File.separator);
            pathBuffer.append(TASK_KEY);
            pathBuffer.append(File.separator);
            pathBuffer.append(taskId);
            // pathBuffer.append(File.separator);
            return pathBuffer.toString();
        }
        throw new ProductLineNameIsNullException("productline name can not be empty!");
    }
}
