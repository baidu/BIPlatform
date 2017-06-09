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
/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.event;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.tesseract.isservice.event.InitMiniCubeEvent.InitMiniCubeInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * InitMiniCubeListener
 * 
 * @author lijin
 *
 */
@Service
public class InitMiniCubeListener implements ApplicationListener<InitMiniCubeEvent> {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitMiniCubeListener.class);
    
    /**
     * indexService
     */
    @Resource(name = "indexService")
    private IndexService indexService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org
     * .springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(InitMiniCubeEvent event) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_BEGIN,
            "InitMiniCubeListener.onApplicationEvent", event));
        if (event == null) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "InitMiniCubeListener.onApplicationEvent", event));
            throw new IllegalArgumentException();
        }
        if (event.getInitMiniCubeInfo() != null) {
            InitMiniCubeInfo initMiniCubeInfo = event.getInitMiniCubeInfo();
            try {
                this.indexService.initMiniCubeIndex(initMiniCubeInfo.getCubeList(),
                        initMiniCubeInfo.getDataSourceInfo(), initMiniCubeInfo.isIndexAsap(),
                        initMiniCubeInfo.isLimited());
            } catch (IndexAndSearchException e) {                
                LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                    "InitMiniCubeListener.onApplicationEvent", event), e);
            }
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_END,
            "InitMiniCubeListener.onApplicationEvent", event));
        
    }
    
}
