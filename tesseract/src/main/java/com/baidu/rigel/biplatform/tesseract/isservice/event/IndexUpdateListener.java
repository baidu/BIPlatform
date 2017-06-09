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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.tesseract.isservice.search.service.IndexSearcherFactory;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexUpdateListener
 * 
 * @author lijin
 *
 */
@Service
public class IndexUpdateListener implements ApplicationListener<IndexUpdateEvent> {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexUpdateListener.class);
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org
     * .springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(IndexUpdateEvent event) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_BEGIN,
            "IndexUpdateListener.onApplicationEvent", event));
        if (event == null) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "IndexUpdateListener.onApplicationEvent", event));
            return;
        }
        IndexSearcherFactory factory = IndexSearcherFactory.getInstance();
        if (event.getUpdateInfo() != null) {
            if (event.getUpdateInfo().getIdxNoServicePathList() != null
                && event.getUpdateInfo().getIdxNoServicePathList().size() > 0) {
                for (String idxPath : event.getUpdateInfo().getIdxNoServicePathList()) {
                    try {
                        factory.releaseSearchManager(idxPath);
                    } catch (IOException e) {
                        String message = String.format(
                            LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                            "IndexUpdateListener.onApplicationEvent", event);
                        LOGGER.error(message + " when processing [idxPath:" + idxPath + "]", e);
                    }
                }
            }
            
            if (event.getUpdateInfo().getIdxServicePathList() != null
                && event.getUpdateInfo().getIdxServicePathList().size() > 0) {
                for (String idxPath : event.getUpdateInfo().getIdxServicePathList()) {
                    try {
                        factory.refreshSearchManager(idxPath);
                    } catch (IOException e) {
                        String message = String.format(
                            LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                            "IndexUpdateListener.onApplicationEvent", event);
                        LOGGER.error(message + " when processing [idxPath:" + idxPath + "]", e);
                    }
                }
            }
        }
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_END,
            "IndexUpdateListener.onApplicationEvent", event));
    }
    
}
