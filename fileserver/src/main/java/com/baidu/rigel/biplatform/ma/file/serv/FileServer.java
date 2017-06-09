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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ma.common.file.protocol.Request;
import com.baidu.rigel.biplatform.ma.common.file.protocol.Response;
import com.baidu.rigel.biplatform.ma.common.file.protocol.ResponseStatus;
import com.baidu.rigel.biplatform.ma.file.serv.service.FileLocation;
import com.baidu.rigel.biplatform.ma.file.serv.service.LocalFileOperationService;
import com.baidu.rigel.biplatform.ma.file.serv.service.impl.LocalFileOperationServiceImpl;
import com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils;
import com.google.common.base.Preconditions;

/**
 * 
 * 文件服务器server端， 用于提供对文件服务器的文件以及目录的写操作
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public class FileServer extends ChannelHandlerAdapter {
    

    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileServer.class);
    
    /**
     * 请求参数：target
     */
    private static final String TARGET = "target";
    
    /**
     * 请求参数：data
     */
    private static final String DATA = "data";
    
    /**
     * 请求参数：replace
     */
    private static final String REPLACE = "replace";
    
    /**
     * 请求参数：src
     */
    private static final String SRC = "src";
    
    /**
     * 请求参数：dir
     */
    private static final String DIR = "dir";
    
    /**
     * 文件存储路径
     */
    private static FileLocation fileLocation;
    
    /**
     * 本地文件服务提供者
     */
    private static LocalFileOperationService service = null;
    
    /**
     * file server root directory property key
     */
    private static final String ROOT_DIR_KEY = "biplatform.fileserver.rootdir";
    
    /**
     * file server port property key
     */
    private static final String PORT_NUM_KEY = "biplatform.fileserver.port";
    
    /**
     * 
     * 程序入口，用于启动文件server
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
//        if (args.length != 1) {
//            LOGGER.error("can not get enough parameters for starting file server");
//            printUsage();
//            System.exit(-1);
//        }
        
        FileInputStream fis = null;
        Properties properties = new Properties();
        try {
	        if (args.length <= 0) {
	        	String classLocation = FileServer.class.getProtectionDomain()
	                    .getCodeSource().getLocation().toString().replace("file:/", "");
	            final File configFile = new File(classLocation + "/fileserver.conf");
	            if (configFile.exists()) {
	                fis = new FileInputStream(configFile);
	                properties.load(fis);
	            }
	        } else {
				if (StringUtils.isNotEmpty(args[0])) {
					fis = new FileInputStream(new File(args[0]));
					properties.load(fis);
				} else {
					printUsage();
					throw new RuntimeException(
							"can't find correct file server configuration file!");
				}
	        }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        int port = -1;
        try {
            port = Integer.valueOf(properties.getProperty(PORT_NUM_KEY));
        } catch (NumberFormatException e) {
            LOGGER.error("parameter is not correct, [port = {}]", args[0]);
            System.exit(-1);
        }
        
        String location = properties.getProperty(ROOT_DIR_KEY);
        if (location.contains("$")) {
        	String systemProp = location.substring(location.indexOf("{") + 1, location.indexOf("}"));
        	Preconditions.checkArgument(System.getProperties().get(systemProp) != null, 
        			String.format("没有找到%s的变量", String.format("${%s}", systemProp)));
        	location = StringUtils.replace(location, String.format("${%s}", systemProp), 
        			System.getProperties().get(systemProp).toString().split(";")[0]);
        }
        if (StringUtils.isEmpty(location)) {
            LOGGER.error("the location can not be empty");
            System.exit(-1);
        }
        
        File f = new File(location);
        if (!f.exists() && !f.mkdirs()) {
            LOGGER.error("invalidation location [{}] please verify the input", args[1]);
            System.exit(-1);
        }
        startServer(location, port);
    }
    
    private static void startServer(String location, int port) {
        fileLocation = new FileLocation(location);
        service = new LocalFileOperationServiceImpl(fileLocation);
        EventLoopGroup bossGroup = new NioEventLoopGroup(10);
        EventLoopGroup workGroup = new NioEventLoopGroup(50);
        try {
            ServerBootstrap strap = new ServerBootstrap();
            strap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        // 对象序列化解码器
                        channel.pipeline().addLast(
                            new ObjectDecoder(ClassResolvers
                                .weakCachingConcurrentResolver(FileServer.class.getClassLoader())));
                        channel.pipeline().addLast(new ObjectEncoder());
//                        channel.pipeline().addLast("HeartBeatHandler", new HeartBeatRespHandler());
                        channel.pipeline().addLast(new FileServer());
                    }
                });
            ChannelFuture future = strap.bind(port).sync();
            LOGGER.info("start file server successfully");
            LOGGER.info("port : " + port);
            LOGGER.info("location : " + location);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error("can not start file server with [port : {}] and fileLocation {{}}", port,
                location);
            printUsage();
            System.exit(-1);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
    
    /**
     * 打印提示信息
     */
    private static void printUsage() {
        LOGGER.info("=====================================================");
        LOGGER.info("=  Usage: java -jar <jar file> <config_file_location>");
        LOGGER.info("=  eg: java -jar <jar file> /tmp/config.properties   ");
        LOGGER.info("=====================================================");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("get message from client " + msg);
        Request req = (Request) msg;
        // 本地文件操作服务
        switch (req.getCommand()) {
            case COPY:
                doCopy(ctx, req);
                break;
            case FILE_ATTRIBUTES:
                doGetFileAttribuets(ctx, req);
                break;
            case LS:
                doLs(ctx, req);
                break;
            case MKDIR:
                doMkDir(ctx, req);
                break;
            case MKDIRS:
                doMkDirs(ctx, req);
                break;
            case MV:
                doMv(ctx, req);
                break;
            case RM:
                doRm(ctx, req);
                break;
            case WRITE:
                doWrite(ctx, req);
                break;
            case READ:
                doRead(ctx, req);
                break;
            default:
                Response rs = new Response(ResponseStatus.UNKNOW, "未知请求", null);
                ctx.writeAndFlush(rs);
                return;
        }
    }
    
    private void doRead(ChannelHandlerContext ctx, Request req) {
        String dir = (String) req.getParams().get(DIR);
        Map<String, Object> result = service.read(dir);
        response(ctx, result);
    }

    /**
     * 执行写文件命令
     * 
     * @param ctx
     * @param req
     */
    private void doWrite(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        
        if (!params.containsKey(REPLACE)) {
            response = new Response(ResponseStatus.FAIL,
                    "request must content parameter with name replace", null);
            ctx.writeAndFlush(response);
            return;
        }
        
        if (!params.containsKey(DATA)) {
            response = new Response(ResponseStatus.FAIL,
                    "request must content parameter with name data", null);
            ctx.writeAndFlush(response);
            return;
        }
        
        String path = (String) params.get(DIR);
        boolean replace = (boolean) params.get(REPLACE);
        byte[] content = (byte[]) params.get(DATA);
        Map<String, Object> rs = service.write(getAbsPath(path), content, replace);
        response(ctx, rs);
        
    }
    
    /**
     * 执行删除命令
     * 
     * @param ctx
     * @param req
     */
    private void doRm(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String path = (String) params.get(DIR);
        Map<String, Object> rs = service.rm(getAbsPath(path));
        response(ctx, rs);
    }
    
    /**
     * 执行移动命令
     * 
     * @param ctx
     * @param req
     */
    private void doMv(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInputs(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String srcFile = (String) params.get(SRC);
        String targetFile = (String) params.get(TARGET);
        boolean replace = (boolean) params.get(REPLACE);
        Map<String, Object> rs = service.mv(getAbsPath(srcFile), getAbsPath(targetFile), replace);
        response(ctx, rs);
        
    }
    
    /**
     * 检查多个输入参数
     * 
     * @param ctx
     * @param params
     */
    private Response checkInputs(ChannelHandlerContext ctx, Map<String, Object> params) {
        if (params == null) {
            return new Response(ResponseStatus.FAIL, "bad reqeust", null);
        }
        
        if (!params.containsKey(SRC)) {
            return new Response(ResponseStatus.FAIL,
                    "request must content parameter with name src", null);
        }
        
        if (!params.containsKey(TARGET)) {
            return new Response(ResponseStatus.FAIL,
                    "request must content parameter with name target", null);
        }
        
        if (!params.containsKey(REPLACE)) {
            return new Response(ResponseStatus.FAIL,
                    "request must content parameter with name replace", null);
        }
        return null;
    }
    
    /**
     * 创建多级目录
     * 
     * @param ctx
     * @param req
     */
    private void doMkDirs(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String path = (String) params.get(DIR);
        Map<String, Object> rs = service.mkdirs(getAbsPath(path));
        response(ctx, rs);
    }
    
    /**
     * 创建目录
     * 
     * @param ctx
     * @param req
     */
    private void doMkDir(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String path = (String) params.get(DIR);
        Map<String, Object> rs = service.mkdir(getAbsPath(path));
        response(ctx, rs);
    }
    
    /**
     * 检查输入路径是否存在
     * 
     * @param ctx
     * @param params
     * @return
     */
    private Response checkInput(ChannelHandlerContext ctx, Map<String, Object> params) {
        if (params == null) {
            return new Response(ResponseStatus.FAIL, "bad reqeust", null);
        }
        
        if (!params.containsKey(DIR)) {
            return new Response(ResponseStatus.FAIL,
                    "request must content parameter with name dir", null);
        }
        return null;
    }
    
    /**
     * 浏览文件列表
     * 
     * @param ctx
     * @param req
     */
    private void doLs(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String path = (String) params.get(DIR);
        Map<String, Object> rs = service.ls(getAbsPath(path));
        response(ctx, rs);
    }
    
    /**
     * 执行获取文件属性操作
     * 
     * @param ctx
     * @param req
     */
    private void doGetFileAttribuets(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInput(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String path = (String) params.get(DIR);
        Map<String, Object> rs = service.getFileAttributes(getAbsPath(path));
        response(ctx, rs);
    }
    
    /**
     * 获取文件所在绝对路径
     * 
     * @param path
     * @return
     */
    private String getAbsPath(String path) {
        return fileLocation.value() + "/" + path;
    }
    
    /**
     * 执行拷贝操作
     * 
     * @param ctx
     * @param req
     */
    private void doCopy(ChannelHandlerContext ctx, Request req) {
        Map<String, Object> params = req.getParams();
        Response response = checkInputs(ctx, params);
        if (response != null) {
            ctx.writeAndFlush(response);
            return;
        }
        String srcFile = (String) params.get(SRC);
        String targetFile = (String) params.get(TARGET);
        boolean replace = (boolean) params.get(REPLACE);
        Map<String, Object> rs = service.copy(getAbsPath(srcFile), getAbsPath(targetFile), replace);
        response(ctx, rs);
    }
    
    /**
     * 发送响应到客户端
     * 
     * @param ctx
     * @param rs
     */
    private void response(ChannelHandlerContext ctx, Map<String, Object> rs) {
        if (rs.get(LocalFileOperationUtils.RESULT).equals(LocalFileOperationUtils.FAIL)) {
            Response response = new Response(ResponseStatus.FAIL,
                    (String) rs.get(LocalFileOperationUtils.MSG), rs);
            ctx.writeAndFlush(response);
        } else {
            Response response = new Response(ResponseStatus.SUCCESS,
                    (String) rs.get(LocalFileOperationUtils.MSG), rs);
            ctx.writeAndFlush(response);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }
    
}
