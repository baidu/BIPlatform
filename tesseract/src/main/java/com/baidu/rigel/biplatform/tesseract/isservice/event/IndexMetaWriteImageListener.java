package com.baidu.rigel.biplatform.tesseract.isservice.event;

import java.io.File;
import java.util.Iterator;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexMetaWriteImageListener 处理索引元数据更新事件
 * @author lijin
 *
 */
@Service
public class IndexMetaWriteImageListener implements
        ApplicationListener<IndexMetaWriteImageEvent> {
    @Resource
    private IndexMetaService idxMetaService; 
    
    @Resource
    private IsNodeService isNodeService;
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(IndexMetaWriteImageListener.class);

    @Override
    public void onApplicationEvent(IndexMetaWriteImageEvent event) {
        LOGGER.info(String.format(
                LogInfoConstants.INFO_PATTERN_ON_LISTENER_BEGIN,
                "IndexMetaWriteImageListener.onApplicationEvent", event));
        if (event == null || event.getIdxMeta() == null) {
            LOGGER.info(String.format(
                    LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                    "IndexMetaWriteImageListener.onApplicationEvent", event));
            return;
            //throw new IllegalArgumentException();
        }else {
            IndexMeta idxMeta=event.getIdxMeta();
            Node currNode=this.isNodeService.getCurrentNode();
            String idxMetaFileBase=currNode.getIndexBaseDir()+idxMeta.getIndexMetaFileDirPath();
            File idxMetaFileDir=new File(idxMetaFileBase);
            if(!FileUtils.isEmptyDir(idxMetaFileDir)){                
                Iterator<IndexShard> it=idxMeta.getIdxShardList().iterator();                
                while(it.hasNext()){
                    IndexShard idxShard=it.next();
                    File shardFile=new File(idxMetaFileBase+idxShard.getShardName());
                    if(FileUtils.isEmptyDir(shardFile)){
                        it.remove();
                    }
                }
                
                try {
                    this.idxMetaService.saveIndexMetaLocally(idxMeta);
                } catch (Exception e) {
                    LOGGER.error("Exception occur while saving idxMeta to local with idxMetaId:"+idxMeta.getIndexMetaId()+"", e);
                }
            }
        }
        
        LOGGER.info(String.format(
                LogInfoConstants.INFO_PATTERN_ON_LISTENER_END,
                "IndexMetaWriteImageListener.onApplicationEvent", event));

    }

}
