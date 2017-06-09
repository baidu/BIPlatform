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
package com.baidu.rigel.biplatform.tesseract.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * 文件相关util
 * 
 * @author lijin
 *
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    
    /**
     * RESULT
     */
    public static final String RESULT = "RESULT";
    
    /**
     * FAIL
     */
    public static final String FAIL = "FAIL";
    
    /**
     * SUCC
     */
    public static final String SUCC = "SUCC";
    
    /**
     * MSG
     */
    public static final String MSG = "MSG";
    
    /**
     * 请求参数：data
     */
    public static final String DATA = "DATA";
    
    /**
     * 请求参数：replace
     */
    public static final String REPLACE = "REPLACE";
    
    /**
     * 请求参数：dir
     */
    public static final String DIR = "DIR";
    
    /**
     * FILE_COPY_PREFIX
     */
    public static final String FILE_COPY_PREFIX = "indexcopy";
    
    /**
     * 
     * getDiskSize 计算指定目录文件的磁盘大小
     * 
     * @param path
     *            path
     * @return long
     */
    public static long getDiskSize(String path) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "getDiskSize",
            "[path:" + path + "]"));
        if (StringUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        long result = getTotalSizeOfFilesInDir(file);
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "getDiskSize",
            "[path:" + path + "]"));
        return result;
    }
    
    /**
     * 
     * getTotalSizeOfFilesInDir 计算指定文件的磁盘大小
     * 
     * @param file
     *            文件
     * @return long
     */
    private static long getTotalSizeOfFilesInDir(final File file) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "getTotalSizeOfFilesInDir", "[File:" + file + "]"));
        if (file.isFile()) {
            return file.length();
        }
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null) {
            for (final File child : children) {
                total += getTotalSizeOfFilesInDir(child);
            }
        }
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "getTotalSizeOfFilesInDir", "[File:" + file + "]"));
        return total;
    }
    
    /**
     * 删除目标文件
     * @param target 待删除的文件
     */
    public static void deleteFile(File target) {
        if (target.isFile() && target.exists()) {
            target.delete();
            LOGGER.warn("delete file:"+target.getAbsolutePath());
            return;
        }
        File[] fileArr = target.listFiles();
        if(fileArr!=null && fileArr.length>0){
            for (File file : fileArr) {
                deleteFile(file);

            }
        }
        if(target.exists()){
            target.delete();
        }
        
        
        
    }
    
    /**
     * 从原文件读取内容，并写入新文件，在执行此方法前，已对原文件和目标文件是否存在进行了判断
     * 
     * @param oldFile
     *            原文件
     * @param newFile
     *            新文件
     * @return
     * @throws IOException
     */
    public static boolean copyFile(File oldFile, File newFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(oldFile);
            fileOutputStream = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int len = 0;
            // 读取原文件内容，然后写入新文件
            while ((len = fileInputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
                fileOutputStream.flush();
            }
            return true;
        } catch (IOException e) {
        	LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
            	LOGGER.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 复制整个文件夹内容
     * 
     * @param oldPath
     *            String 原文件路径 如：c:/fqf
     * @param newPath
     *            String 复制后路径 如：f:/fqf/ff
     * @return boolean
     * @throws Exception
     */
    public static void copyFolder(String oldPath, String newPath) throws Exception {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "copyFolder",
            "[oldPath:" + oldPath + "][newPath:+" + newPath + "]"));
        try {
            
            File newPathFile=new File(newPath);
            newPathFile.mkdirs();
            
            File oldPathFile = new File(oldPath);
            String[] file = oldPathFile.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + File.separator
                            + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {
                    // 如果是子文件夹
                    copyFolder(oldPath + File.separator + file[i], newPath + File.separator
                        + file[i]);
                }
            }
        } catch (Exception e) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "copyFolder", "[oldPath:" + oldPath + "][newPath:+" + newPath + "]"));
            throw e;
            
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "copyFolder",
            "[oldPath:" + oldPath + "][newPath:+" + newPath + "]"));
        
    }
    
    /**
     * 
     * write
     * @param filePath 写入路径
     * @param content 内容
     * @param replace 是否替换
     * @return boolean
     */
    public static boolean write(String filePath, byte[] content, boolean replace) {
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "write",
            "[filePath:" + filePath + "][content:" + content + "][replace:+" + replace + "]"));
        
        boolean result = false;
        
        if (StringUtils.isEmpty(filePath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "Can not process empty filePath: " + filePath));
            return result;
        }
        
        if (content == null || content.length == 0) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "Can not process empty file content: " + content));
            return result;
        }
        
        File file = new File(filePath);
        // 新文件已经存在，不进行覆盖
        if (file.exists() && !replace) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "File already exists: " + filePath));
            return result;
        }
        // 如果文件不存在，首先进行创建
        if (!file.exists()) {
            if (!createFile(filePath)) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                    "Create new file fail: " + filePath));
                return result;
            }
        }
        // 写入文件内容
        if (!writeFile(file, content)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "Write file fail: " + filePath));
            return result;
        }
        result = true;
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "write", "[filePath:"
            + filePath + "][content:" + content + "][replace:+" + replace + "]"));
        
        return result;
    }
    
    /**
     * 在本地创建新文件
     * 
     * @param filePath
     *            需要创建的文件
     */
    public static boolean createFile(String filePath) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "createFile",
            "[filePath:" + filePath + "]"));
        boolean result = false;
        if (StringUtils.isEmpty(filePath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "Can not process empty filePath: " + filePath));
            return result;
        }
        try {
            
            File file = new File(filePath);
            // 创建文件未目录
            if (filePath.endsWith("/")) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                    "filePath: " + filePath + " is a directory"));
                return result;
            }
            int pos = filePath.lastIndexOf(File.separator);
            // 路径包括文件名和文件夹名，先创建文件夹，之后创建文件
            String dir = filePath.substring(0, pos);
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            result = file.createNewFile();
        } catch (IOException e) {
            
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "IOException"));
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "createFile",
            "[filePath:" + filePath + "]"));
        return result;
    }
    
    /**
     * 将content内容写入本地文件
     * 
     * @param file
     *            需要写入的文件
     * @param content
     *            写入文件内容
     * @param code
     *            编码方式
     */
    public static boolean writeFile(File file, byte[] content) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "writeFile",
            "[file:" + file + "][content:" + content + "]"));
        boolean result = false;
        if (file == null || content == null || content.length == 0) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "Can not process empty file or content: " + "[file:" + file + "][content:"
                    + content + "]"));
            return result;
        }
        
        FileOutputStream fileOutputStream = null;
        try {
            
            fileOutputStream = new FileOutputStream(file);
            // 写入本地
            fileOutputStream.write(content);
            fileOutputStream.flush();
            result = true;
        } catch (IOException e) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                "IOException"));
            LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FILEPROCESS_ERROR,
                        "IOException"));
                    LOGGER.error(e.getMessage(), e);
                    
                    return false;
                }
            }
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "writeFile", "[file:"
            + file + "][content:" + content + "]"));
        return result;
    }
    
    
    /**
     * readFile
     * 
     * @param filePath 读取的文件路径
     * @return byte[]
     */
    public static byte[] readFile(String filePath) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "readFile",
            "[filePath:" + filePath + "]"));
        if (StringUtils.isEmpty(filePath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION, "readFile",
                "[filePath:" + filePath + "]"));
            throw new IllegalArgumentException();
            
        }
        File file = new File(filePath);
        
        FileInputStream fin = null;
        FileChannel fcin = null;
        ByteBuffer rbuffer = ByteBuffer.allocate(TesseractConstant.FILE_BLOCK_SIZE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        if (file.exists()) {
            try {
                fin = new FileInputStream(file);
                fcin = fin.getChannel();
                while (fcin.read(rbuffer) != -1) {
                    bos.write(rbuffer.array());
                }
            } catch (Exception e) {
                LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                    "readFile", "[filePath:" + filePath + "]"), e);
            } finally {
                try {
                    if (fin != null) {
                        fin.close();
                    }
                    if (fcin != null) {
                        fcin.close();
                    }
                    bos.close();
                } catch (Exception e) {
                    LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                        "readFile", "[filePath:" + filePath + "]"), e);
                }
                
            }
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "readFile",
            "[filePath:" + filePath + "]"));
        return bos.toByteArray();
    }
    
    /**
     * Perform file compression.
     * 
     * @param inFileName
     *            Name of the file to be compressed
     * @throws IOException
     */
    public static String doCompressFile(String inFileName,String outFileName) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "doCompressFile",
            "[inFileName:" + inFileName + "]"));
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        GzipCompressorOutputStream gzOut = null;
        TarArchiveOutputStream tOut = null;
        if (StringUtils.isEmpty(inFileName)) {
            throw new IllegalArgumentException();
        }
        String compressedFileName = outFileName;
        
        FileInputStream fi = null;
        BufferedInputStream sourceStream = null;
        
        try {
            
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_COMPRESS_PROCESS,
                "Creating the GZIP output stream"));
            
            /** Step: 1 ---> create a TarArchiveOutputStream object. **/
            fOut = new FileOutputStream(new File(compressedFileName));
            bOut = new BufferedOutputStream(fOut);
            gzOut = new GzipCompressorOutputStream(bOut);
            tOut = new TarArchiveOutputStream(gzOut);
            
            /**
             * Step: 2 --->Open the source data and get a list of files from
             * given directory.
             */
            File source = new File(inFileName);
            if (!source.exists()) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_COMPRESS_ERROR,
                    "File not found. " + inFileName));
                return null;
            }
            File[] files = null;
            if (source.isDirectory()) {
                files = source.listFiles();
            } else {
                files = new File[1];
                files[0] = source;
            }
            
            for (int i = 0; i < files.length; i++) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_COMPRESS_PROCESS,
                    "Adding File:"
                        + source.getParentFile().toURI().relativize(files[i].toURI()).getPath()));
                /**
                 * Step: 3 ---> Create a tar entry for each file that is read.
                 */
                /**
                 * relativize is used to to add a file to a tar, without
                 * including the entire path from root.
                 */
                
                TarArchiveEntry entry = new TarArchiveEntry(files[i], source.getParentFile()
                        .toURI().relativize(files[i].toURI()).getPath());
                /**
                 * Step: 4 ---> Put the tar entry using putArchiveEntry.
                 */
                tOut.putArchiveEntry(entry);
                
                /**
                 * Step: 5 ---> Write the data to the tar file and close the
                 * input stream.
                 */
                
                fi = new FileInputStream(files[i]);
                sourceStream = new BufferedInputStream(fi, TesseractConstant.FILE_BLOCK_SIZE);
                int count;
                byte[] data = new byte[TesseractConstant.FILE_BLOCK_SIZE];
                while ((count = sourceStream.read(data, 0, TesseractConstant.FILE_BLOCK_SIZE)) != -1) {
                    tOut.write(data, 0, count);
                }
                
                sourceStream.close();
                
                /**
                 * Step: 6 --->close the archive entry.
                 */
                
                tOut.closeArchiveEntry();
                
            }
            
            /**
             * Step: 7 --->close the output stream.
             */
            
            tOut.close();
            
        } catch (IOException e) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_COMPRESS_ERROR, "IOException"));
            LOGGER.error(e.getMessage(), e);
            throw e;
            
        } finally {
            try {
                fOut.close();
                bOut.close();
                gzOut.close();
                tOut.close();
                fi.close();
                sourceStream.close();
            } catch (IOException e) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_COMPRESS_ERROR,
                    "IOException occur when closing fd"));
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
            
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "doCompressFile",
            "[inFileName:" + inFileName + "][compressedFileName:" + compressedFileName + "]"));
        
        return compressedFileName;
    }
    
    /**
     * Uncompress the incoming file.
     * 
     * @param inFileName
     *            Name of the file to be uncompressed
     * @param outFileName
     *            Name of target
     */
    public static String doUncompressFile(String inFileName, String outFileName) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "doUncompressFile",
            "[inFileName:" + inFileName + "]"));
        if (StringUtils.isEmpty(inFileName)) {
            throw new IllegalArgumentException();
        }
        String decompressedFileName = outFileName;
        
        File inFile = new File(inFileName);
        inFile.setReadOnly();
        if (StringUtils.isEmpty(decompressedFileName)) {
            // not specified outFileName
            StringBuilder sb = new StringBuilder();
            sb.append(inFile.getParentFile().getAbsolutePath());
            sb.append(File.separator);
            // sb.append(inFile.getName().substring(0,
            // inFile.getName().indexOf(".")));
            // sb.append(File.separator);
            decompressedFileName = sb.toString();
        }
        File outFile = new File(decompressedFileName);
        //清理解压目录
        if(outFile.exists() && outFile.isDirectory()){
        	LOGGER.info("############################################################################");
        	LOGGER.info("#############OUTFile:"+outFile.getAbsolutePath()+"###########################");
        	FileUtils.deleteFile(outFile);
        }
        
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        /**
         * create a TarArchiveInputStream object.
         */
        
        FileInputStream fin = null;
        BufferedInputStream in = null;
        GzipCompressorInputStream gzIn = null;
        TarArchiveInputStream tarIn = null;
        
        FileOutputStream fos = null;
        BufferedOutputStream dest = null;
        
        TarArchiveEntry entry = null;
        
        try {
            fin = new FileInputStream(inFile);
            in = new BufferedInputStream(fin);
            gzIn = new GzipCompressorInputStream(in);
            tarIn = new TarArchiveInputStream(gzIn);
            
            /**
             * Read the tar entries using the getNextEntry method
             */
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_DECOMPRESS_PROCESS,
                    "Extracting File:" + entry.getName()));
                
                if (entry.isDirectory()) {
                    /**
                     * If the entry is a directory, create the directory.
                     */
                    File f = new File(decompressedFileName + entry.getName());
                    f.mkdirs();
                } else {
                    /**
                     * If the entry is a file,write the decompressed file to the
                     * disk and close destination stream.
                     */
                    int count;
                    byte[] data = new byte[TesseractConstant.FILE_BLOCK_SIZE];
                    String fileName = decompressedFileName
                            + entry.getName().substring(
                                    entry.getName().indexOf(
                                        TesseractConstant.DECOMPRESSION_FILENAME_SPLITTER) + 1);
                    
                    fos = new FileOutputStream(new File(fileName));
                    dest = new BufferedOutputStream(fos, TesseractConstant.FILE_BLOCK_SIZE);
                    while ((count = tarIn.read(data, 0, TesseractConstant.FILE_BLOCK_SIZE)) != -1) {
                        dest.write(data, 0, count);
                        
                    }
                    
                    dest.close();
                    
                }
                
            }
            
            /**
             * Close the input stream
             */
            
            tarIn.close();
        } catch (IOException e) {
            LOGGER.info(String
                    .format(LogInfoConstants.INFO_PATTERN_DECOMPRESS_ERROR, "IOException"));
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            
            try {
                fin.close();
                in.close();
                gzIn.close();
                tarIn.close();
                fos.close();
                dest.close();
            } catch (IOException e) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_DECOMPRESS_ERROR,
                    "IOException occur when closing fd"));
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
            
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "doUncompressFile",
            "[inFileName:" + inFileName + "]"));
        return decompressedFileName;
    }
    
    /**
     * 判断当前dir是否为空目录
     * @param dir
     * @return boolean
     */
    public static boolean isEmptyDir(File dir){
    	boolean result=false;
    	if(dir==null || !dir.exists() || !dir.isDirectory() || ArrayUtils.isEmpty(dir.listFiles())){
    		result=true;
    	}
    	return result;
    }
    
    /**
     * 判断给定的文件后缀在当前文件目录下是否存在
     * @param dir 文件目录
     * @param fileSuffix 文件后缀
     * @return boolean
     */
    public static boolean isExistGivingFileSuffix(File dir,String fileSuffix){
    	boolean result=false;
    	if(!isEmptyDir(dir) && !StringUtils.isEmpty(fileSuffix)){
    		File[] files=dir.listFiles(new LocalImageFilenameFilter(fileSuffix));
    		if(!ArrayUtils.isEmpty(files)){
    			result=true;
    		}
    	}
    	
    	return result;
    }
    
    public static class LocalImageFilenameFilter implements FilenameFilter{
    	
    	private String fileSuffix;
    	
		public LocalImageFilenameFilter(String fileSuffix) {
			super();
			this.fileSuffix = fileSuffix;
		}

		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String name) {
			if(name.indexOf(fileSuffix)==-1){
				return false;
			}
			if(name.lastIndexOf(fileSuffix) == name.indexOf(fileSuffix)){
				return true;
			}
			return false;
		}
    	
    }
    
}
