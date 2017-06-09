package com.baidu.rigel.biplatform.asyndownload;

import com.baidu.rigel.biplatform.asyndownload.bo.AddTaskParameters;
import com.baidu.rigel.biplatform.asyndownload.bo.AddTaskStatus;
import com.baidu.rigel.biplatform.asyndownload.exception.AsynDownloadException;

public interface AyncAddDownloadTaskService {
    /**
     * addTask,新建下载任务
     *
     * @param AddTaskParameters addTaskParameters
     * @return AddTaskStatus success or fail
     */
    public AddTaskStatus addTask(AddTaskParameters addTaskParameters) throws AsynDownloadException;
}
