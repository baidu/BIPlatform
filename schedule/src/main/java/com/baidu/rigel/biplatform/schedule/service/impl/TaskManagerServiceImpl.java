package com.baidu.rigel.biplatform.schedule.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.exception.PersitentTaskAlreadyExistException;
import com.baidu.rigel.biplatform.schedule.exception.ScheduleException;
import com.baidu.rigel.biplatform.schedule.listener.ServerStartedJob;
import com.baidu.rigel.biplatform.schedule.service.ScheduleService;
import com.baidu.rigel.biplatform.schedule.service.TaskManagerService;
import com.baidu.rigel.biplatform.schedule.utils.ScheduleHelper;
import com.google.common.collect.Lists;

/**
 * 调度TaskManagerServiceImpl
 * 
 * @author majun04
 *
 */
@Service("taskManagerService")
public class TaskManagerServiceImpl implements TaskManagerService, ServerStartedJob {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManagerServiceImpl.class);

    private static final String USER_CHAR = "user";
    @Resource
    private FileService fileService;
    @Resource
    private ScheduleService scheduleService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.TaskManagerService#addTaskToScheduleEngine(com.baidu.rigel.biplatform
     * .schedule.bo.ScheduleTaskInfo)
     */
    public boolean addTaskToScheduleEngine(ScheduleTaskInfo taskInfo) throws ScheduleException {
        boolean flag = true;
        String fileSavePath = ScheduleHelper.generateFileSavePath(taskInfo);
        // 如果文件已经存在，则直接抛出异常
        if (isFileExisted(fileSavePath)) {
            throw new PersitentTaskAlreadyExistException("the task file is already exist. please check the task : ["
                    + taskInfo.toString() + "]");
        }
        try {
            fileService.write(fileSavePath, SerializationUtils.serialize(taskInfo));
            scheduleService.addTaskToSchedule(taskInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.schedule.service.TaskManagerService#loadPersistScheduleTaskToEngine()
     */
    public boolean loadPersistScheduleTaskToEngine() {
        boolean flag = true;
        List<String> productLineNameList = this.getProductLineNameList();
        if (!CollectionUtils.isEmpty(productLineNameList)) {
            for (String productLineName : productLineNameList) {
                try {
                    String[] taskIds = fileService.ls(ScheduleHelper.getTaskSavedRootPath(productLineName));
                    if (taskIds != null && taskIds.length > 0) {
                        for (String taskId : taskIds) {
                            ScheduleTaskInfo taskInfo =
                                    this.getTaskObj(ScheduleHelper.generateFileSavePath(taskId, productLineName));
                            this.scheduleService.addTaskToSchedule(taskInfo);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    flag = false;
                }
            }
        }
        return flag;

    }

    /**
     * 得到文件服务器下的各产品线名称组成的列表
     * 
     * @return 返回文件服务器下的各产品线名称组成的列表
     */
    private List<String> getProductLineNameList() {
        List<String> productLineNameList = Lists.newArrayList();
        try {
            String[] usersStr = fileService.ls(USER_CHAR);
            if (!ArrayUtils.isEmpty(usersStr)) {
                for (String userStr : usersStr) {
                    String productLineName = ScheduleHelper.parserProductLineNameFromUserDesc(userStr);
                    if (!StringUtils.isEmpty(productLineName)) {
                        productLineNameList.add(productLineName);
                    }
                }
            }
        } catch (FileServiceException e) {
            LOG.error(e.getMessage(), e);
        }
        return productLineNameList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.TaskManagerService#updateTask4ScheduleEngine(com.baidu.rigel.biplatform
     * .schedule.bo.ScheduleTaskInfo)
     */
    public boolean updateTask4ScheduleEngine(ScheduleTaskInfo taskInfo) throws ScheduleException {
        boolean flag = true;
        String fileSavePath = ScheduleHelper.generateFileSavePath(taskInfo);
        try {
            ScheduleTaskInfo newTaskInfo = getTaskObj(fileSavePath);
            BeanUtils.copyProperties(taskInfo, newTaskInfo);
            fileService.write(fileSavePath, SerializationUtils.serialize(newTaskInfo), true);
            scheduleService.updateTask2Schedule(newTaskInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            flag = false;
        }

        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.TaskManagerService#deleteTask4ScheduleEngine(com.baidu.rigel.biplatform
     * .schedule.bo.ScheduleTaskInfo)
     */
    public boolean deleteTask4ScheduleEngine(ScheduleTaskInfo taskInfo) throws ScheduleException {
        boolean returnFlag = true;
        String fileSavePath = ScheduleHelper.generateFileSavePath(taskInfo.getTaskId(), taskInfo.getProductLineName());
        if (isFileExisted(fileSavePath)) {
            try {
                fileService.rm(fileSavePath);
                scheduleService.deleteTask4Schedule(taskInfo);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                returnFlag = false;
            }
        }

        return returnFlag;
    }

    /**
     * 由fileserver得到task二进制反序列化之后的ScheduleTaskInfo对象
     * 
     * @param fileSavePath 文件存储位置
     * @return 反序列化之后的ScheduleTaskInfo对象
     */
    public ScheduleTaskInfo getTaskObj(String fileSavePath) {
        ScheduleTaskInfo newTaskInfo = null;
        try {
            newTaskInfo = (ScheduleTaskInfo) SerializationUtils.deserialize(fileService.read(fileSavePath));
        } catch (FileServiceException e) {
            LOG.error(e.getMessage(), e);
        }
        return newTaskInfo;
    }

    /**
     * 先判断task持久化文件是否存在
     * 
     * @param fileSavePath 文件存储位置
     * @return 返回task存在与否的标识
     */
    private boolean isFileExisted(String fileSavePath) {
        boolean isFileExisted = true;
        try {
            fileService.read(fileSavePath);
        } catch (Exception fse) {
            LOG.error("task savepath : [" + fileSavePath + "],do not exist,maybe it have been deleted....", fse);
            isFileExisted = false;
        }
        return isFileExisted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.schedule.listener.ServerStartedJob#doInitJob()
     */
    @Override
    public void doInitJob() {
        this.loadPersistScheduleTaskToEngine();
    }
}
