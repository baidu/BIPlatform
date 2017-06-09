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
package com.baidu.rigel.biplatform.ma.file.serv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 
 * 文件服务状态检查http请求支持，主要为了配合ps做服务状态检查
 * 
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class FileServerStatusCheckedHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    /**
     * 请求url
     */
    private String url;
    
    /**
     * 
     * @param url
     *            FileServerStatusCheckedHandler
     */
    public FileServerStatusCheckedHandler(String url) {
        super();
        this.url = url;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (msg.getUri().contains(url)) {
            ctx.write("FileServer status : ok");
        }
    }
    
}
